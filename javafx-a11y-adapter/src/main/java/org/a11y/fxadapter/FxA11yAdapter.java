package org.a11y.fxadapter;

import javafx.application.Platform;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FxA11yAdapter {

    private static final FxA11yAdapter INSTANCE = new FxA11yAdapter();

    private final Map<Node, FxAccessible> nodeMap = new ConcurrentHashMap<>();
    private final Set<Scene> registeredScenes = ConcurrentHashMap.newKeySet();
    private final Set<Window> registeredWindows = ConcurrentHashMap.newKeySet();
    private boolean initialized = false;
    private boolean embedded = false;
    private boolean windowListenerInstalled = false;

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

        Thread watchThread = new Thread(() -> {
            A11yBridge bridge = A11yBridge.getInstance();
            bridge.setDeferEmbed(true);
            bridge.start();
            bridge.awaitReady(10000);
            if (!bridge.isReady()) return;

            for (int i = 0; i < 60; i++) {
                try {
                    Platform.runLater(() -> {});
                    break;
                } catch (IllegalStateException e) {
                    try { Thread.sleep(500); } catch (InterruptedException ex) { return; }
                }
            }

            Platform.runLater(this::attachToWindows);
        }, "fx-a11y-watcher");
        watchThread.setDaemon(true);
        watchThread.start();
    }

    private void attachToWindows() {
        if (windowListenerInstalled) return;
        windowListenerInstalled = true;

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
        if (!registeredWindows.add(window)) return;

        Scene scene = window.getScene();
        if (scene != null) {
            walkScene(scene);
        }
        window.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) walkScene(newScene);
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

    private void walkScene(Scene scene) {
        if (!registeredScenes.add(scene)) return;

        Parent root = scene.getRoot();
        if (root == null) return;

        FxAccessible rootAcc = getOrCreateAccessible(root, null);
        A11yBridge.getInstance().registerTopLevel(rootAcc.getAccessibleContext());
        walkNode(root, null);

        if (!embedded) {
            embedded = true;
            A11yBridge.getInstance().embed();
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
            A11yBridge.getInstance().getOrCreateNode(acc.getAccessibleContext());
            return acc;
        });
    }

    private void installPropertyListeners(Node node, FxAccessible acc) {
        AccessibleContext ctx = acc.getAccessibleContext();

        node.focusedProperty().addListener((obs, was, is) -> {
            firePropertyChange(ctx, AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                is ? null : AccessibleState.FOCUSED,
                is ? AccessibleState.FOCUSED : null);
        });

        node.visibleProperty().addListener((obs, was, is) -> {
            firePropertyChange(ctx, AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                is ? null : AccessibleState.VISIBLE,
                is ? AccessibleState.VISIBLE : null);
        });

        node.disabledProperty().addListener((obs, was, is) -> {
            firePropertyChange(ctx, AccessibleContext.ACCESSIBLE_STATE_PROPERTY,
                is ? AccessibleState.ENABLED : null,
                is ? null : AccessibleState.ENABLED);
        });
    }

    private void firePropertyChange(AccessibleContext ctx, String property,
                                     Object oldVal, Object newVal) {
        ctx.firePropertyChange(property, oldVal, newVal);
    }
}
