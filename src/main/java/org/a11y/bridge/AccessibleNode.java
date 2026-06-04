package org.a11y.bridge;

import org.a11y.bridge.atspi.AccessibleIface;
import org.a11y.bridge.atspi.ActionIface;
import org.a11y.bridge.atspi.ComponentIface;
import org.a11y.bridge.atspi.ImageIface;
import org.a11y.bridge.atspi.SelectionIface;
import org.a11y.bridge.atspi.TableIface;
import org.a11y.bridge.atspi.TextIface;
import org.a11y.bridge.atspi.ValueIface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

import javax.accessibility.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AccessibleNode implements AccessibleIface, ComponentIface, ActionIface,
        ValueIface, SelectionIface, TextIface, ImageIface, TableIface, Properties {

    private final A11yBridge bridge;
    private final AccessibleContext ac;
    private final int id;

    AccessibleNode(A11yBridge bridge, AccessibleContext ac, int id) {
        this.bridge = bridge;
        this.ac = ac;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public AccessibleContext getAccessibleContext() {
        return ac;
    }

    // --- Properties interface (D-Bus properties) ---

    @Override
    @SuppressWarnings("unchecked")
    public <A> A Get(String interfaceName, String propertyName) {
        try {
            Variant<?> result = getProperty(interfaceName, propertyName);
            if (result != null) return (A) result;
        } catch (Exception e) {
            System.err.println("java-a11y-bridge: Get(" + interfaceName + ", " + propertyName
                + ") failed on node " + id + ": " + e);
        }
        return (A) new Variant<>("");
    }

    @Override
    public <A> void Set(String interfaceName, String propertyName, A value) {
    }

    @Override
    public Map<String, Variant<?>> GetAll(String interfaceName) {
        try {
            return switch (interfaceName) {
                case "org.a11y.atspi.Accessible" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    props.put("Name", new Variant<>(getName()));
                    props.put("Description", new Variant<>(getDescription()));
                    props.put("Parent", new Variant<>(getParent()));
                    props.put("ChildCount", new Variant<>(getChildCount()));
                    props.put("Locale", new Variant<>(getLocale()));
                    props.put("AccessibleId", new Variant<>(String.valueOf(id)));
                    props.put("HelpText", new Variant<>(""));
                    yield props;
                }
                case "org.a11y.atspi.Action" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    int n = GetNActions();
                    props.put("NActions", new Variant<>(n));
                    yield props;
                }
                case "org.a11y.atspi.Value" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    props.put("CurrentValue", new Variant<>(GetCurrentValue()));
                    props.put("MinimumValue", new Variant<>(GetMinimumValue()));
                    props.put("MaximumValue", new Variant<>(GetMaximumValue()));
                    props.put("MinimumIncrement", new Variant<>(GetMinimumIncrement()));
                    yield props;
                }
                case "org.a11y.atspi.Text" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    props.put("CharacterCount", new Variant<>(getTextCharacterCount()));
                    props.put("CaretOffset", new Variant<>(getTextCaretOffset()));
                    yield props;
                }
                case "org.a11y.atspi.Table" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    props.put("NRows", new Variant<>(getTableRowCount()));
                    props.put("NColumns", new Variant<>(getTableColumnCount()));
                    props.put("NSelectedRows", new Variant<>(0));
                    props.put("NSelectedColumns", new Variant<>(0));
                    yield props;
                }
                case "org.a11y.atspi.Component" -> {
                    Map<String, Variant<?>> props = new LinkedHashMap<>();
                    yield props;
                }
                default -> Collections.emptyMap();
            };
        } catch (Exception e) {
            System.err.println("java-a11y-bridge: GetAll(" + interfaceName + ") failed on node " + id + ": " + e);
        }
        return Collections.emptyMap();
    }

    private Variant<?> getProperty(String iface, String name) {
        return switch (iface) {
            case "org.a11y.atspi.Accessible" -> switch (name) {
                case "Name" -> new Variant<>(getName());
                case "Description" -> new Variant<>(getDescription());
                case "Parent" -> new Variant<>(getParent());
                case "ChildCount" -> new Variant<>(getChildCount());
                case "Locale" -> new Variant<>(getLocale());
                case "AccessibleId" -> new Variant<>(String.valueOf(id));
                case "HelpText" -> new Variant<>("");
                default -> new Variant<>("");
            };
            case "org.a11y.atspi.Action" -> switch (name) {
                case "NActions" -> new Variant<>(GetNActions());
                default -> new Variant<>("");
            };
            case "org.a11y.atspi.Value" -> switch (name) {
                case "CurrentValue" -> new Variant<>(GetCurrentValue());
                case "MinimumValue" -> new Variant<>(GetMinimumValue());
                case "MaximumValue" -> new Variant<>(GetMaximumValue());
                case "MinimumIncrement" -> new Variant<>(GetMinimumIncrement());
                default -> new Variant<>("");
            };
            case "org.a11y.atspi.Text" -> switch (name) {
                case "CharacterCount" -> new Variant<>(getTextCharacterCount());
                case "CaretOffset" -> new Variant<>(getTextCaretOffset());
                default -> new Variant<>("");
            };
            case "org.a11y.atspi.Table" -> switch (name) {
                case "NRows" -> new Variant<>(getTableRowCount());
                case "NColumns" -> new Variant<>(getTableColumnCount());
                case "NSelectedRows" -> new Variant<>(0);
                case "NSelectedColumns" -> new Variant<>(0);
                case "Caption" -> new Variant<>(new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
                case "Summary" -> new Variant<>(new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
                default -> new Variant<>(0);
            };
            case "org.a11y.atspi.Image" -> switch (name) {
                case "ImageDescription" -> new Variant<>(GetImageDescription());
                case "ImageLocale" -> new Variant<>(getLocale());
                default -> new Variant<>("");
            };
            default -> new Variant<>("");
        };
    }


    private String getName() {
        String name = SwingThreadUtil.callOnEDT(ac::getAccessibleName, null);
        return name != null ? name : "";
    }

    private String getDescription() {
        String desc = SwingThreadUtil.callOnEDT(ac::getAccessibleDescription, null);
        return desc != null ? desc : "";
    }

    private AccessibleRef getParent() {
        return SwingThreadUtil.callOnEDT(() -> {
            Accessible parent = ac.getAccessibleParent();
            if (parent == null) return bridge.getRootRef();
            return bridge.makeRef(parent.getAccessibleContext());
        }, bridge.getRootRef());
    }

    private int getChildCount() {
        return SwingThreadUtil.callOnEDT(ac::getAccessibleChildrenCount, 0);
    }

    private String getLocale() {
        return SwingThreadUtil.callOnEDT(
            () -> ac.getLocale().toString(),
            Locale.getDefault().toString()
        );
    }

    // --- AccessibleIface methods ---

    @Override
    public List<AccessibleRef> GetChildren() {
        return SwingThreadUtil.callOnEDT(() -> {
            int count = ac.getAccessibleChildrenCount();
            List<AccessibleRef> children = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                Accessible child = ac.getAccessibleChild(i);
                if (child != null) {
                    children.add(bridge.makeRef(child.getAccessibleContext()));
                }
            }
            return children;
        }, Collections.emptyList());
    }

    @Override
    public AccessibleRef GetChildAtIndex(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            if (index < 0 || index >= ac.getAccessibleChildrenCount()) {
                return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
            }
            Accessible child = ac.getAccessibleChild(index);
            if (child == null) {
                return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
            }
            return bridge.makeRef(child.getAccessibleContext());
        }, new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
    }

    @Override
    public AccessibleRef GetApplication() {
        return bridge.getRootRef();
    }

    @Override
    public int GetIndexInParent() {
        return SwingThreadUtil.callOnEDT(ac::getAccessibleIndexInParent, -1);
    }

    @Override
    public List<RelationEntry> GetRelationSet() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleRelationSet relSet = ac.getAccessibleRelationSet();
            if (relSet == null || relSet.size() == 0) {
                return Collections.<RelationEntry>emptyList();
            }
            List<RelationEntry> result = new ArrayList<>();
            for (AccessibleRelation rel : relSet.toArray()) {
                int type = RelationMapping.toAtspi(rel.getKey());
                List<AccessibleRef> targets = new ArrayList<>();
                for (Object target : rel.getTarget()) {
                    if (target instanceof Accessible a) {
                        targets.add(bridge.makeRef(a.getAccessibleContext()));
                    }
                }
                if (!targets.isEmpty()) {
                    result.add(new RelationEntry(new UInt32(type), targets));
                }
            }
            return result;
        }, Collections.emptyList());
    }

    @Override
    public UInt32 GetRole() {
        return SwingThreadUtil.callOnEDT(
            () -> new UInt32(RoleMapping.toAtspi(ac.getAccessibleRole())),
            new UInt32(66)
        );
    }

    @Override
    public String GetRoleName() {
        return SwingThreadUtil.callOnEDT(
            () -> RoleMapping.toRoleName(ac.getAccessibleRole()),
            "unknown"
        );
    }

    @Override
    public String GetLocalizedRoleName() {
        return GetRoleName();
    }

    @Override
    public List<UInt32> GetState() {
        return SwingThreadUtil.callOnEDT(
            () -> StateMapping.toAtspi(ac.getAccessibleStateSet()),
            List.of(new UInt32(0), new UInt32(0))
        );
    }

    @Override
    public Map<String, String> GetAttributes() {
        return Map.of("toolkit", "Java Swing");
    }

    @Override
    public List<String> GetInterfaces() {
        return SwingThreadUtil.callOnEDT(() -> {
            List<String> ifaces = new ArrayList<>();
            ifaces.add("org.a11y.atspi.Accessible");
            if (ac.getAccessibleComponent() != null) ifaces.add("org.a11y.atspi.Component");
            if (ac.getAccessibleAction() != null) ifaces.add("org.a11y.atspi.Action");
            if (ac.getAccessibleText() != null) ifaces.add("org.a11y.atspi.Text");
            if (ac.getAccessibleEditableText() != null) ifaces.add("org.a11y.atspi.EditableText");
            if (ac.getAccessibleValue() != null) ifaces.add("org.a11y.atspi.Value");
            if (ac.getAccessibleSelection() != null) ifaces.add("org.a11y.atspi.Selection");
            if (ac.getAccessibleTable() != null) ifaces.add("org.a11y.atspi.Table");
            if (ac.getAccessibleIcon() != null && ac.getAccessibleIcon().length > 0) ifaces.add("org.a11y.atspi.Image");
            return ifaces;
        }, List.of("org.a11y.atspi.Accessible"));
    }

    // --- ComponentIface ---

    @Override
    public boolean Contains(int x, int y, UInt32 coordType) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleComponent comp = ac.getAccessibleComponent();
            if (comp == null) return false;
            Point p = adjustCoords(comp, x, y, coordType);
            return comp.contains(p);
        }, false);
    }

    @Override
    public Extents GetExtents(UInt32 coordType) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleComponent comp = ac.getAccessibleComponent();
            if (comp == null) return new Extents(0, 0, 0, 0);
            Rectangle bounds = comp.getBounds();
            if (coordType.intValue() == 0) { // screen coords
                Point screenPos = getScreenPosition(comp);
                return new Extents(screenPos.x, screenPos.y, bounds.width, bounds.height);
            }
            return new Extents(bounds.x, bounds.y, bounds.width, bounds.height);
        }, new Extents(0, 0, 0, 0));
    }

    @Override
    public UInt32 GetLayer() {
        return new UInt32(0); // ATSPI_LAYER_WIDGET
    }

    @Override
    public short GetMDIZOrder() {
        return 0;
    }

    @Override
    public boolean GrabFocus() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleComponent comp = ac.getAccessibleComponent();
            if (comp != null) {
                comp.requestFocus();
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public double GetAlpha() {
        return 1.0;
    }

    @Override
    public boolean ScrollTo(UInt32 type) {
        return false;
    }

    @Override
    public boolean ScrollToPoint(UInt32 coordType, int x, int y) {
        return false;
    }

    private Point getScreenPosition(AccessibleComponent comp) {
        try {
            Point loc = comp.getLocationOnScreen();
            return loc != null ? loc : new Point(0, 0);
        } catch (Exception e) {
            return comp.getLocation();
        }
    }

    private Point adjustCoords(AccessibleComponent comp, int x, int y, UInt32 coordType) {
        if (coordType.intValue() == 0) { // screen coords
            Point screen = getScreenPosition(comp);
            return new Point(x - screen.x, y - screen.y);
        }
        return new Point(x, y);
    }

    // --- ActionIface ---

    @Override
    public int GetNActions() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleAction action = ac.getAccessibleAction();
            return action != null ? action.getAccessibleActionCount() : 0;
        }, 0);
    }

    @Override
    public String GetActionName(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleAction action = ac.getAccessibleAction();
            if (action != null && index >= 0 && index < action.getAccessibleActionCount()) {
                String desc = action.getAccessibleActionDescription(index);
                return desc != null ? desc : "";
            }
            return "";
        }, "");
    }

    @Override
    public String GetActionDescription(int index) {
        return GetActionName(index);
    }

    @Override
    public String GetActionKeyBinding(int index) {
        return "";
    }

    @Override
    public String GetActionLocalizedName(int index) {
        return GetActionName(index);
    }

    @Override
    public boolean DoAction(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleAction action = ac.getAccessibleAction();
            if (action != null && index >= 0 && index < action.getAccessibleActionCount()) {
                return action.doAccessibleAction(index);
            }
            return false;
        }, false);
    }

    // --- ValueIface ---

    @Override
    public double GetCurrentValue() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleValue val = ac.getAccessibleValue();
            return val != null ? val.getCurrentAccessibleValue().doubleValue() : 0.0;
        }, 0.0);
    }

    @Override
    public double GetMinimumValue() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleValue val = ac.getAccessibleValue();
            return val != null ? val.getMinimumAccessibleValue().doubleValue() : 0.0;
        }, 0.0);
    }

    @Override
    public double GetMaximumValue() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleValue val = ac.getAccessibleValue();
            return val != null ? val.getMaximumAccessibleValue().doubleValue() : 0.0;
        }, 0.0);
    }

    @Override
    public double GetMinimumIncrement() {
        return 1.0;
    }

    @Override
    public boolean SetCurrentValue(double value) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleValue val = ac.getAccessibleValue();
            if (val != null) {
                return val.setCurrentAccessibleValue(value);
            }
            return false;
        }, false);
    }

    // --- SelectionIface ---

    @Override
    public int GetNSelectedChildren() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            return sel != null ? sel.getAccessibleSelectionCount() : 0;
        }, 0);
    }

    @Override
    public AccessibleRef GetSelectedChild(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            if (sel != null && index >= 0 && index < sel.getAccessibleSelectionCount()) {
                Accessible child = sel.getAccessibleSelection(index);
                if (child != null) {
                    return bridge.makeRef(child.getAccessibleContext());
                }
            }
            return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
        }, new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
    }

    @Override
    public boolean SelectChild(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            if (sel != null) {
                sel.addAccessibleSelection(index);
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public boolean DeselectSelectedChild(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            if (sel != null) {
                sel.removeAccessibleSelection(index);
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public boolean DeselectChild(int index) {
        return DeselectSelectedChild(index);
    }

    @Override
    public boolean IsChildSelected(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            return sel != null && sel.isAccessibleChildSelected(index);
        }, false);
    }

    @Override
    public boolean SelectAll() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            if (sel != null) {
                sel.selectAllAccessibleSelection();
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public boolean ClearSelection() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleSelection sel = ac.getAccessibleSelection();
            if (sel != null) {
                sel.clearAccessibleSelection();
                return true;
            }
            return false;
        }, false);
    }

    // --- Table helpers ---

    private int getTableRowCount() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            return table != null ? table.getAccessibleRowCount() : 0;
        }, 0);
    }

    private int getTableColumnCount() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            return table != null ? table.getAccessibleColumnCount() : 0;
        }, 0);
    }

    // --- TextIface ---

    private int getTextCharacterCount() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            return text != null ? text.getCharCount() : 0;
        }, 0);
    }

    private int getTextCaretOffset() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            return text != null ? text.getCaretPosition() : -1;
        }, -1);
    }

    @Override
    public String GetText(int startOffset, int endOffset) {
        String result = SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            if (text == null) return "";
            int count = text.getCharCount();
            int end = (endOffset == -1 || endOffset > count) ? count : endOffset;
            int start = Math.max(0, Math.min(startOffset, end));
            if (start >= end) return "";
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                String ch = text.getAtIndex(AccessibleText.CHARACTER, i);
                if (ch != null) sb.append(ch);
            }
            return sb.toString();
        }, "");
        return result;
    }

    @Override
    public boolean SetCaretOffset(int offset) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            if (text instanceof AccessibleEditableText et) {
                et.selectText(offset, offset);
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public String GetStringAtOffset(int offset, UInt32 granularity) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            if (text == null) return "";
            int part = switch (granularity.intValue()) {
                case 0 -> AccessibleText.CHARACTER;
                case 1 -> AccessibleText.WORD;
                case 2, 3 -> AccessibleText.SENTENCE;
                default -> AccessibleText.CHARACTER;
            };
            String result = text.getAtIndex(part, offset);
            return result != null ? result : "";
        }, "");
    }

    @Override
    public int GetCharacterAtOffset(int offset) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            if (text == null) return 0;
            String ch = text.getAtIndex(AccessibleText.CHARACTER, offset);
            return (ch != null && !ch.isEmpty()) ? ch.codePointAt(0) : 0;
        }, 0);
    }

    @Override
    public Map<String, String> GetAttributes(int offset) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> GetDefaultAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public int GetNSelections() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleText text = ac.getAccessibleText();
            if (text == null) return 0;
            return text.getSelectedText() != null ? 1 : 0;
        }, 0);
    }

    @Override
    public boolean AddSelection(int startOffset, int endOffset) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleEditableText et = ac.getAccessibleEditableText();
            if (et != null) {
                et.selectText(startOffset, endOffset);
                return true;
            }
            return false;
        }, false);
    }

    @Override
    public boolean RemoveSelection(int selectionNum) {
        return false;
    }

    @Override
    public boolean SetSelection(int selectionNum, int startOffset, int endOffset) {
        return AddSelection(startOffset, endOffset);
    }

    @Override
    public boolean ScrollSubstringTo(int startOffset, int endOffset, UInt32 type) {
        return false;
    }

    // --- ImageIface ---

    @Override
    public String GetImageDescription() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleIcon[] icons = ac.getAccessibleIcon();
            if (icons != null && icons.length > 0) {
                String desc = icons[0].getAccessibleIconDescription();
                return desc != null ? desc : "";
            }
            return "";
        }, "");
    }

    @Override
    public String GetImageLocale() {
        return getLocale();
    }

    // --- TableIface ---

    @Override
    public AccessibleRef GetAccessibleAt(int row, int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                Accessible cell = table.getAccessibleAt(row, column);
                if (cell != null) return bridge.makeRef(cell.getAccessibleContext());
            }
            return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
        }, new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
    }

    @Override
    public int GetIndexAt(int row, int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                int cols = table.getAccessibleColumnCount();
                return row * cols + column;
            }
            return -1;
        }, -1);
    }

    @Override
    public int GetRowAtIndex(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null && table.getAccessibleColumnCount() > 0) {
                return index / table.getAccessibleColumnCount();
            }
            return -1;
        }, -1);
    }

    @Override
    public int GetColumnAtIndex(int index) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null && table.getAccessibleColumnCount() > 0) {
                return index % table.getAccessibleColumnCount();
            }
            return -1;
        }, -1);
    }

    @Override
    public String GetRowDescription(int row) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                Accessible desc = table.getAccessibleRowDescription(row);
                if (desc != null) {
                    String name = desc.getAccessibleContext().getAccessibleName();
                    return name != null ? name : "";
                }
            }
            return "";
        }, "");
    }

    @Override
    public String GetColumnDescription(int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                Accessible desc = table.getAccessibleColumnDescription(column);
                if (desc != null) {
                    String name = desc.getAccessibleContext().getAccessibleName();
                    return name != null ? name : "";
                }
            }
            return "";
        }, "");
    }

    @Override
    public int GetRowExtentAt(int row, int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) return table.getAccessibleRowExtentAt(row, column);
            return 1;
        }, 1);
    }

    @Override
    public int GetColumnExtentAt(int row, int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) return table.getAccessibleColumnExtentAt(row, column);
            return 1;
        }, 1);
    }

    @Override
    public AccessibleRef GetRowHeader(int row) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                AccessibleTable header = table.getAccessibleRowHeader();
                if (header != null) {
                    Accessible cell = header.getAccessibleAt(row, 0);
                    if (cell != null) return bridge.makeRef(cell.getAccessibleContext());
                }
            }
            return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
        }, new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
    }

    @Override
    public AccessibleRef GetColumnHeader(int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                AccessibleTable header = table.getAccessibleColumnHeader();
                if (header != null) {
                    Accessible cell = header.getAccessibleAt(0, column);
                    if (cell != null) return bridge.makeRef(cell.getAccessibleContext());
                }
            }
            return new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH);
        }, new AccessibleRef(bridge.getBusName(), A11yBridge.NULL_PATH));
    }

    @Override
    public List<Integer> GetSelectedRows() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                int[] rows = table.getSelectedAccessibleRows();
                if (rows != null) {
                    List<Integer> result = new ArrayList<>(rows.length);
                    for (int r : rows) result.add(r);
                    return result;
                }
            }
            return Collections.<Integer>emptyList();
        }, Collections.emptyList());
    }

    @Override
    public List<Integer> GetSelectedColumns() {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            if (table != null) {
                int[] cols = table.getSelectedAccessibleColumns();
                if (cols != null) {
                    List<Integer> result = new ArrayList<>(cols.length);
                    for (int c : cols) result.add(c);
                    return result;
                }
            }
            return Collections.<Integer>emptyList();
        }, Collections.emptyList());
    }

    @Override
    public boolean IsRowSelected(int row) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            return table != null && table.isAccessibleRowSelected(row);
        }, false);
    }

    @Override
    public boolean IsColumnSelected(int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            return table != null && table.isAccessibleColumnSelected(column);
        }, false);
    }

    @Override
    public boolean IsSelected(int row, int column) {
        return SwingThreadUtil.callOnEDT(() -> {
            AccessibleTable table = ac.getAccessibleTable();
            return table != null && table.isAccessibleSelected(row, column);
        }, false);
    }

    @Override
    public boolean AddRowSelection(int row) {
        return false;
    }

    @Override
    public boolean AddColumnSelection(int column) {
        return false;
    }

    @Override
    public boolean RemoveRowSelection(int row) {
        return false;
    }

    @Override
    public boolean RemoveColumnSelection(int column) {
        return false;
    }

    @Override
    public String getObjectPath() {
        return "/org/a11y/atspi/accessible/" + id;
    }
}
