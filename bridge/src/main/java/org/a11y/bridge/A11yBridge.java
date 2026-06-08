package org.a11y.bridge;

import org.a11y.bridge.atspi.A11yBusIface;
import org.a11y.bridge.atspi.AccessibleIface;
import org.a11y.bridge.atspi.AccessibleIface.AccessibleRef;
import org.a11y.bridge.atspi.SocketIface;
import org.a11y.bridge.atspi.SocketIface.PlugRef;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.types.UInt32;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class A11yBridge {

    private static final A11yBridge INSTANCE = new A11yBridge();

    static final String ROOT_PATH = "/org/a11y/atspi/accessible/root";
    static final String NULL_PATH = "/org/a11y/atspi/null";

    private DBusConnection a11yBus;
    private String busName;
    private final AtomicInteger nodeIdCounter = new AtomicInteger(1);
    private final Map<AccessibleContext, AccessibleNode> nodes = new ConcurrentHashMap<>();
    private final List<AccessibleContext> externalTopLevels = Collections.synchronizedList(new ArrayList<>());
    private RootAccessibleNode rootNode;
    private final ExecutorService eventExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "a11y-events");
        t.setDaemon(true);
        return t;
    });

    private A11yBridge() {}

    public static A11yBridge getInstance() {
        return INSTANCE;
    }

    private volatile boolean started = false;

    public void start() {
        if (started) return;
        started = true;
        Thread thread = new Thread(this::init, "a11y-bridge-init");
        thread.setDaemon(true);
        thread.start();
    }

    public boolean isReady() {
        return a11yBus != null;
    }

    public void awaitReady(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (!isReady() && System.currentTimeMillis() < deadline) {
            try { Thread.sleep(50); } catch (InterruptedException e) { break; }
        }
    }

    private volatile boolean deferEmbed = false;

    public void setDeferEmbed(boolean defer) {
        this.deferEmbed = defer;
    }

    private void init() {
        try {
            String address = discoverBusAddress();
            if (address == null) {
                System.err.println("java-a11y-bridge: AT-SPI2 bus not available");
                return;
            }

            a11yBus = DBusConnectionBuilder.forAddress(address).build();
            busName = a11yBus.getUniqueName();

            rootNode = new RootAccessibleNode(this);
            a11yBus.exportObject(ROOT_PATH, rootNode);

            System.out.println("java-a11y-bridge: connected to AT-SPI2 bus as " + busName);

            if (!deferEmbed) {
                embed();
            }
            installEventListeners();
        } catch (Exception e) {
            System.err.println("java-a11y-bridge: failed to initialize");
            e.printStackTrace();
        }
    }

    public void embed() {
        if (a11yBus == null) return;
        try {
            SocketIface registry = a11yBus.getRemoteObject(
                "org.a11y.atspi.Registry",
                "/org/a11y/atspi/accessible/root",
                SocketIface.class
            );
            registry.Embed(new SocketIface.PlugRef(busName, ROOT_PATH));
            System.out.println("java-a11y-bridge: registered with AT-SPI2 registry");
        } catch (Exception e) {
            System.err.println("java-a11y-bridge: embed failed: " + e.getMessage());
        }
    }

    private String discoverBusAddress() {
        String envAddr = System.getenv("AT_SPI_BUS_ADDRESS");
        if (envAddr != null && !envAddr.isEmpty()) {
            return envAddr;
        }

        try (DBusConnection session = DBusConnectionBuilder.forSessionBus().build()) {
            A11yBusIface a11yBus = session.getRemoteObject(
                "org.a11y.Bus",
                "/org/a11y/bus",
                A11yBusIface.class
            );
            return a11yBus.GetAddress();
        } catch (Exception e) {
            return null;
        }
    }

    private void installEventListeners() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addAWTEventListener(this::handleAWTEvent,
            AWTEvent.WINDOW_EVENT_MASK |
            AWTEvent.FOCUS_EVENT_MASK |
            AWTEvent.CONTAINER_EVENT_MASK);
    }

    private void handleAWTEvent(AWTEvent e) {
        if (e instanceof WindowEvent we) {
            switch (we.getID()) {
                case WindowEvent.WINDOW_OPENED -> {
                    Window win = we.getWindow();
                    if (win instanceof Accessible accessible) {
                        getOrCreateNode(accessible.getAccessibleContext());
                    }
                }
                case WindowEvent.WINDOW_ACTIVATED -> {
                    if (we.getWindow() instanceof Accessible a)
                        eventExecutor.submit(() -> emitWindowEvent(a.getAccessibleContext(), "Activate"));
                }
                case WindowEvent.WINDOW_DEACTIVATED -> {
                    if (we.getWindow() instanceof Accessible a)
                        eventExecutor.submit(() -> emitWindowEvent(a.getAccessibleContext(), "Deactivate"));
                }
            }
        } else if (e instanceof FocusEvent fe) {
            if (fe.getID() == FocusEvent.FOCUS_GAINED && fe.getSource() instanceof Accessible accessible) {
                AccessibleContext ac = accessible.getAccessibleContext();
                AccessibleNode node = getOrCreateNode(ac);
                eventExecutor.submit(() -> emitFocusEvent(node));
            }
        }
    }

    private final java.beans.PropertyChangeListener propertyChangeListener = evt -> {
        eventExecutor.submit(() -> handlePropertyChange(evt));
    };

    private void handlePropertyChange(java.beans.PropertyChangeEvent evt) {
        Object source = evt.getSource();
        AccessibleContext ac;
        if (source instanceof AccessibleContext ctx) {
            ac = ctx;
        } else if (source instanceof Accessible a) {
            ac = a.getAccessibleContext();
        } else {
            return;
        }

        AccessibleNode node = getOrCreateNode(ac);
        if (node == null) return;

        String prop = evt.getPropertyName();
        try {
            if (AccessibleContext.ACCESSIBLE_STATE_PROPERTY.equals(prop)) {
                Object newVal = evt.getNewValue();
                Object oldVal = evt.getOldValue();
                javax.accessibility.AccessibleState state;
                boolean value;
                if (newVal != null) {
                    state = (javax.accessibility.AccessibleState) newVal;
                    value = true;
                } else {
                    state = (javax.accessibility.AccessibleState) oldVal;
                    value = false;
                }
                int bit = StateMapping.toBit(state);
                if (bit >= 0) {
                    emitStateChanged(node, state.toDisplayString(java.util.Locale.US)
                        .toLowerCase().replace(' ', '-'), value);
                }
                if (newVal == javax.accessibility.AccessibleState.FOCUSED) {
                    emitFocusEvent(node);
                }
            } else if (AccessibleContext.ACCESSIBLE_NAME_PROPERTY.equals(prop)) {
                emitPropertyChange(node, "accessible-name", evt.getNewValue());
            } else if (AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY.equals(prop)) {
                emitPropertyChange(node, "accessible-description", evt.getNewValue());
            } else if (AccessibleContext.ACCESSIBLE_VALUE_PROPERTY.equals(prop)) {
                emitPropertyChange(node, "accessible-value", evt.getNewValue());
            } else if (AccessibleContext.ACCESSIBLE_CARET_PROPERTY.equals(prop)) {
                if (evt.getNewValue() instanceof Integer pos) {
                    emitTextCaretMoved(node, pos);
                }
            } else if (AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY.equals(prop)) {
                emitSelectionChanged(node);
            } else if (AccessibleContext.ACCESSIBLE_CHILD_PROPERTY.equals(prop)) {
                if (evt.getOldValue() == null && evt.getNewValue() instanceof Accessible child) {
                    AccessibleNode childNode = getOrCreateNode(child.getAccessibleContext());
                    emitChildrenChanged(node, "add", child.getAccessibleContext().getAccessibleIndexInParent());
                } else if (evt.getNewValue() == null && evt.getOldValue() instanceof Accessible child) {
                    emitChildrenChanged(node, "remove", child.getAccessibleContext().getAccessibleIndexInParent());
                }
            }
        } catch (Exception ex) {
            // don't let event handling crash the app
        }
    }

    // --- Event emission helpers ---

    private static final Map<String, org.freedesktop.dbus.types.Variant<?>> EMPTY_PROPS =
        Collections.emptyMap();

    private void emitFocusEvent(AccessibleNode node) {
        if (node == null || a11yBus == null) return;
        try {
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.FocusEvents.Focus(
                node.getObjectPath(), "", 0, 0,
                new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitStateChanged(AccessibleNode node, String stateName, boolean value) {
        if (a11yBus == null) return;
        try {
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.ObjectEvents.StateChanged(
                node.getObjectPath(), stateName, value ? 1 : 0, 0,
                new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitPropertyChange(AccessibleNode node, String property, Object newValue) {
        if (a11yBus == null) return;
        try {
            String val = newValue != null ? newValue.toString() : "";
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.ObjectEvents.PropertyChange(
                node.getObjectPath(), property, 0, 0,
                new org.freedesktop.dbus.types.Variant<>(val), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitTextCaretMoved(AccessibleNode node, int position) {
        if (a11yBus == null) return;
        try {
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.ObjectEvents.TextCaretMoved(
                node.getObjectPath(), "", position, 0,
                new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitSelectionChanged(AccessibleNode node) {
        if (a11yBus == null) return;
        try {
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.ObjectEvents.SelectionChanged(
                node.getObjectPath(), "", 0, 0,
                new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitChildrenChanged(AccessibleNode node, String type, int index) {
        if (a11yBus == null) return;
        try {
            a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.ObjectEvents.ChildrenChanged(
                node.getObjectPath(), type, index, 0,
                new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
        } catch (Exception e) { /* ignore */ }
    }

    private void emitWindowEvent(AccessibleContext ac, String type) {
        if (a11yBus == null) return;
        AccessibleNode node = getOrCreateNode(ac);
        if (node == null) return;
        try {
            if ("Activate".equals(type)) {
                a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.WindowEvents.Activate(
                    node.getObjectPath(), "", 0, 0,
                    new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
            } else {
                a11yBus.sendMessage(new org.a11y.bridge.atspi.EventSignals.WindowEvents.Deactivate(
                    node.getObjectPath(), "", 0, 0,
                    new org.freedesktop.dbus.types.Variant<>(0), EMPTY_PROPS));
            }
        } catch (Exception e) { /* ignore */ }
    }

    public AccessibleNode getOrCreateNode(AccessibleContext ac) {
        if (ac == null) return null;
        return nodes.computeIfAbsent(ac, ctx -> {
            int id = nodeIdCounter.getAndIncrement();
            AccessibleNode node = new AccessibleNode(this, ctx, id);
            String path = "/org/a11y/atspi/accessible/" + id;
            try {
                a11yBus.exportObject(path, node);
            } catch (Exception ex) {
                System.err.println("java-a11y-bridge: failed to export " + path + ": " + ex.getMessage());
            }
            eventExecutor.submit(() -> {
                SwingThreadUtil.runOnEDT(() -> {
                    try {
                        ctx.addPropertyChangeListener(propertyChangeListener);
                    } catch (Exception ignored) {}
                });
            });
            return node;
        });
    }

    public void removeNode(AccessibleContext ac) {
        AccessibleNode node = nodes.remove(ac);
        if (node != null) {
            String path = "/org/a11y/atspi/accessible/" + node.getId();
            try {
                a11yBus.unExportObject(path);
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    public void registerTopLevel(AccessibleContext ac) {
        externalTopLevels.add(ac);
        getOrCreateNode(ac);
    }

    public void unregisterTopLevel(AccessibleContext ac) {
        externalTopLevels.remove(ac);
        removeNode(ac);
    }

    List<AccessibleContext> getExternalTopLevels() {
        return externalTopLevels;
    }

    public String getBusName() {
        return busName;
    }

    public DBusConnection getBus() {
        return a11yBus;
    }

    public AccessibleRef makeRef(AccessibleContext ac) {
        if (ac == null) {
            return new AccessibleRef(busName, NULL_PATH);
        }
        AccessibleNode node = getOrCreateNode(ac);
        return new AccessibleRef(busName, "/org/a11y/atspi/accessible/" + node.getId());
    }

    public AccessibleRef getRootRef() {
        return new AccessibleRef(busName, ROOT_PATH);
    }
}
