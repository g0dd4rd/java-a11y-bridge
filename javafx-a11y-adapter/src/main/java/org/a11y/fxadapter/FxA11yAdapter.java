package org.a11y.fxadapter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.a11y.bridge.A11yBridge;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FxA11yAdapter {

    private static final FxA11yAdapter INSTANCE = new FxA11yAdapter();

    private final Map<Node, FxAccessible> nodeMap = new ConcurrentHashMap<>();
    private boolean initialized = false;

    private FxA11yAdapter() {}

    public static FxA11yAdapter getInstance() {
        return INSTANCE;
    }

    public static void initialize() {
        INSTANCE.start();
    }

    private void start() {
        if (initialized) return;
        initialized = true;

        // Wait for JavaFX toolkit and windows in a background thread
        Thread watchThread = new Thread(() -> {
            A11yBridge bridge = A11yBridge.getInstance();
            bridge.setDeferEmbed(true);
            bridge.start();
            bridge.awaitReady(10000);
            if (!bridge.isReady()) return;

            // Wait for JavaFX toolkit to be available
            for (int i = 0; i < 60; i++) {
                try {
                    Platform.runLater(() -> {});
                    break;
                } catch (IllegalStateException e) {
                    try { Thread.sleep(500); } catch (InterruptedException ex) { return; }
                }
            }

            // Wait for at least one window to appear
            Platform.runLater(() -> waitForWindows());
        }, "fx-a11y-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    private void waitForWindows() {
        if (!Window.getWindows().isEmpty()) {
            scanWindows();
        } else {
            Window.getWindows().addListener((ListChangeListener<Window>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        Platform.runLater(this::scanWindows);
                    }
                }
            });
        }
    }

    private void scanWindows() {
        for (Window w : Window.getWindows()) {
            handleWindowAdded(w);
        }
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Window w : change.getAddedSubList()) {
                        handleWindowAdded(w);
                    }
                }
            }
        });
    }

    private void handleWindowAdded(Window window) {
        Scene scene = window.getScene();
        if (scene != null) {
            walkSceneAndRegister(scene);
        }
        window.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) walkSceneAndRegister(newScene);
        });

        if (window instanceof Stage stage) {
            stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (isFocused) {
                    Scene s = stage.getScene();
                    if (s != null && s.getRoot() != null) {
                        FxAccessible acc = nodeMap.get(s.getRoot());
                        if (acc != null) {
                            firePropertyChange(acc.getAccessibleContext(),
                                AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                                null, AccessibleState.ACTIVE);
                        }
                    }
                }
            });
        }
    }

    private void walkSceneAndRegister(Scene scene) {
        Parent root = scene.getRoot();
        if (root == null) {
            System.out.println("javafx-a11y-adapter: scene has no root");
            return;
        }
        System.out.println("javafx-a11y-adapter: walking scene, root=" + root.getClass().getSimpleName());
        FxAccessible rootAcc = getOrCreateAccessible(root, null);
        A11yBridge.getInstance().registerTopLevel(rootAcc.getAccessibleContext());
        walkNode(root, null);
        System.out.println("javafx-a11y-adapter: " + nodeMap.size() + " nodes registered");

        // Now embed with registry — ChildCount will be correct from the first query
        A11yBridge.getInstance().embed();

        // Debug: list all AT-SPI2 apps visible to see if ours is there
        try {
            var bus = A11yBridge.getInstance().getBus();
            String busName = A11yBridge.getInstance().getBusName();
            System.out.println("javafx-a11y-adapter: our bus name = " + busName);

            var regAccessible = bus.getRemoteObject(
                "org.a11y.atspi.Registry",
                "/org/a11y/atspi/accessible/root",
                org.a11y.bridge.atspi.AccessibleIface.class);
            var children = regAccessible.GetChildren();
            System.out.println("javafx-a11y-adapter: registry has " + children.size() + " apps:");
            for (var child : children) {
                System.out.println("  " + child.getBusName() + " " + child.getObjectPath());
            }
        } catch (Exception e) {
            System.out.println("javafx-a11y-adapter: registry query failed: " + e.getMessage());
        }
    }

    private void walkNode(Node node, FxAccessible parent) {
        FxAccessible acc = getOrCreateAccessible(node, parent);

        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                walkNode(child, acc);
            }

            p.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) change -> {
                while (change.next()) {
                    if (change.wasAdded()) {
                        for (Node added : change.getAddedSubList()) {
                            walkNode(added, acc);
                            firePropertyChange(acc.getAccessibleContext(),
                                AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                                null, getOrCreateAccessible(added, acc));
                        }
                    }
                    if (change.wasRemoved()) {
                        for (Node removed : change.getRemoved()) {
                            firePropertyChange(acc.getAccessibleContext(),
                                AccessibleContext.ACCESSIBLE_CHILD_PROPERTY,
                                nodeMap.get(removed), null);
                        }
                    }
                }
            });
        }

        installPropertyListeners(node, acc);
    }

    FxAccessible getOrCreateAccessible(Node node, FxAccessible parent) {
        return nodeMap.computeIfAbsent(node, n -> {
            FxAccessible acc = new FxAccessible(n, parent);
            A11yBridge bridge = A11yBridge.getInstance();
            bridge.getOrCreateNode(acc.getAccessibleContext());
            return acc;
        });
    }

    private void installPropertyListeners(Node node, FxAccessible acc) {
        AccessibleContext ctx = acc.getAccessibleContext();

        node.focusedProperty().addListener((obs, was, is) -> {
            if (is) {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.FOCUSED);
            } else {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    AccessibleState.FOCUSED, null);
            }
        });

        node.visibleProperty().addListener((obs, was, is) -> {
            if (is) {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.VISIBLE);
            } else {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    AccessibleState.VISIBLE, null);
            }
        });

        node.disabledProperty().addListener((obs, was, is) -> {
            if (!is) {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    null, AccessibleState.ENABLED);
            } else {
                firePropertyChange(ctx,
                    AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                    AccessibleState.ENABLED, null);
            }
        });
    }

    private void firePropertyChange(AccessibleContext ctx, String property,
                                     Object oldVal, Object newVal) {
        ctx.firePropertyChange(property, oldVal, newVal);
    }
}
