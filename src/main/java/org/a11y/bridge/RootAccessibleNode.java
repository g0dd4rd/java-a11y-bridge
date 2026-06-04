package org.a11y.bridge;

import org.a11y.bridge.atspi.AccessibleIface;
import org.a11y.bridge.atspi.ApplicationIface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import javax.accessibility.Accessible;
import java.awt.Window;
import java.util.*;

public class RootAccessibleNode implements AccessibleIface, ApplicationIface, Properties {

    private final A11yBridge bridge;

    RootAccessibleNode(A11yBridge bridge) {
        this.bridge = bridge;
    }

    // --- Properties interface ---

    @Override
    @SuppressWarnings("unchecked")
    public <A> A Get(String interfaceName, String propertyName) {
        return (A) switch (interfaceName) {
            case "org.a11y.atspi.Accessible" -> getAccessibleProperty(propertyName);
            case "org.a11y.atspi.Application" -> getApplicationProperty(propertyName);
            default -> throw new org.freedesktop.dbus.exceptions.DBusExecutionException(
                "Unknown interface: " + interfaceName);
        };
    }

    @Override
    public <A> void Set(String interfaceName, String propertyName, A value) {
    }

    @Override
    public Map<String, Variant<?>> GetAll(String interfaceName) {
        return switch (interfaceName) {
            case "org.a11y.atspi.Accessible" -> {
                Map<String, Variant<?>> props = new LinkedHashMap<>();
                props.put("Name", new Variant<>(getAppName()));
                props.put("Description", new Variant<>(""));
                props.put("Parent", new Variant<>(
                    new AccessibleRef("org.a11y.atspi.Registry", "/org/a11y/atspi/accessible/root")));
                props.put("ChildCount", new Variant<>(getTopLevelWindows().size()));
                props.put("Locale", new Variant<>(Locale.getDefault().toString()));
                props.put("AccessibleId", new Variant<>(""));
                props.put("HelpText", new Variant<>(""));
                yield props;
            }
            case "org.a11y.atspi.Application" -> {
                Map<String, Variant<?>> props = new LinkedHashMap<>();
                props.put("ToolkitName", new Variant<>("Java Swing"));
                props.put("Version", new Variant<>(System.getProperty("java.version", "unknown")));
                props.put("AtspiVersion", new Variant<>("2.1"));
                props.put("Id", new Variant<>(GetId()));
                yield props;
            }
            default -> Collections.emptyMap();
        };
    }

    private Variant<?> getAccessibleProperty(String name) {
        return switch (name) {
            case "Name" -> new Variant<>(getAppName());
            case "Description" -> new Variant<>("");
            case "Parent" -> new Variant<>(
                new AccessibleRef("org.a11y.atspi.Registry", "/org/a11y/atspi/accessible/root"));
            case "ChildCount" -> new Variant<>(getTopLevelWindows().size());
            case "Locale" -> new Variant<>(Locale.getDefault().toString());
            case "AccessibleId" -> new Variant<>("");
            case "HelpText" -> new Variant<>("");
            default -> throw new org.freedesktop.dbus.exceptions.DBusExecutionException(
                "Unknown property: " + name);
        };
    }

    private Variant<?> getApplicationProperty(String name) {
        return switch (name) {
            case "ToolkitName" -> new Variant<>("Java Swing");
            case "Version" -> new Variant<>(System.getProperty("java.version", "unknown"));
            case "AtspiVersion" -> new Variant<>("2.1");
            case "Id" -> new Variant<>(GetId());
            default -> throw new org.freedesktop.dbus.exceptions.DBusExecutionException(
                "Unknown property: " + name);
        };
    }

    // --- AccessibleIface methods ---

    @Override
    public List<AccessibleRef> GetChildren() {
        var windows = getTopLevelWindows();
        List<AccessibleRef> refs = new ArrayList<>(windows.size());
        for (Window w : windows) {
            refs.add(bridge.makeRef(((Accessible) w).getAccessibleContext()));
        }
        return refs;
    }

    @Override
    public AccessibleRef GetChildAtIndex(int index) {
        var windows = getTopLevelWindows();
        if (index < 0 || index >= windows.size()) {
            return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
        }
        return bridge.makeRef(((Accessible) windows.get(index)).getAccessibleContext());
    }

    @Override
    public AccessibleRef GetApplication() {
        return bridge.getRootRef();
    }

    @Override
    public int GetIndexInParent() {
        return -1;
    }

    @Override
    public List<RelationEntry> GetRelationSet() {
        return Collections.emptyList();
    }

    @Override
    public UInt32 GetRole() {
        return new UInt32(75); // ATSPI_ROLE_APPLICATION
    }

    @Override
    public String GetRoleName() {
        return "application";
    }

    @Override
    public String GetLocalizedRoleName() {
        return "application";
    }

    @Override
    public List<UInt32> GetState() {
        return List.of(new UInt32(0), new UInt32(0));
    }

    @Override
    public Map<String, String> GetAttributes() {
        return Map.of("toolkit", "Java Swing");
    }

    @Override
    public List<String> GetInterfaces() {
        return List.of(
            "org.a11y.atspi.Accessible",
            "org.a11y.atspi.Application"
        );
    }

    @Override
    public String getObjectPath() {
        return A11yBridge.ROOT_PATH;
    }

    // --- ApplicationIface methods ---

    @Override
    public String GetToolkitName() {
        return "Java Swing";
    }

    @Override
    public String GetVersion() {
        return "0.1.0";
    }

    @Override
    public String GetAtspiVersion() {
        return "2.1";
    }

    @Override
    public int GetId() {
        return (int) ProcessHandle.current().pid();
    }

    @Override
    public String GetLocale(int lctype) {
        return Locale.getDefault().toString();
    }

    // --- internals ---

    private String getAppName() {
        String name = System.getProperty("sun.java.command", "");
        int space = name.indexOf(' ');
        if (space > 0) name = name.substring(0, space);
        int dot = name.lastIndexOf('.');
        if (dot >= 0) name = name.substring(dot + 1);
        return name.isEmpty() ? "Java Application" : name;
    }

    private List<Window> getTopLevelWindows() {
        return SwingThreadUtil.callOnEDT(() -> {
            List<Window> result = new ArrayList<>();
            for (Window w : Window.getWindows()) {
                if (w.isShowing() && w instanceof Accessible) {
                    result.add(w);
                }
            }
            return result;
        }, Collections.emptyList());
    }
}
