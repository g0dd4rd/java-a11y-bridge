package org.a11y.fxadapter;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleValue;
import java.awt.*;
import java.util.Locale;

public class FxAccessibleContext extends AccessibleContext
        implements AccessibleComponent, javax.accessibility.AccessibleAction,
                   javax.accessibility.AccessibleText, AccessibleValue {

    private final Node node;
    private final FxAccessible self;
    private final FxAccessible parent;

    FxAccessibleContext(Node node, FxAccessible self, FxAccessible parent) {
        this.node = node;
        this.self = self;
        this.parent = parent;

        String name = queryString(AccessibleAttribute.TEXT);
        if (name != null) setAccessibleName(name);

        String help = queryString(AccessibleAttribute.HELP);
        if (help != null) setAccessibleDescription(help);
    }

    Node getNode() {
        return node;
    }

    // --- AccessibleContext core ---

    @Override
    public AccessibleRole getAccessibleRole() {
        return FxRoleMapping.map(node.getAccessibleRole());
    }

    @Override
    public AccessibleStateSet getAccessibleStateSet() {
        AccessibleStateSet states = new AccessibleStateSet();
        if (Boolean.TRUE.equals(query(AccessibleAttribute.FOCUSED)))
            states.add(AccessibleState.FOCUSED);
        if (!Boolean.TRUE.equals(query(AccessibleAttribute.DISABLED))) {
            states.add(AccessibleState.ENABLED);
            states.add(AccessibleState.FOCUSABLE);
        }
        if (Boolean.TRUE.equals(query(AccessibleAttribute.VISIBLE))) {
            states.add(AccessibleState.VISIBLE);
            states.add(AccessibleState.SHOWING);
        }
        if (Boolean.TRUE.equals(query(AccessibleAttribute.EXPANDED)))
            states.add(AccessibleState.EXPANDED);
        if (Boolean.TRUE.equals(query(AccessibleAttribute.SELECTED)))
            states.add(AccessibleState.SELECTED);
        if (Boolean.TRUE.equals(query(AccessibleAttribute.EDITABLE)))
            states.add(AccessibleState.EDITABLE);

        javafx.scene.AccessibleRole fxRole = node.getAccessibleRole();
        if (fxRole == javafx.scene.AccessibleRole.TEXT_AREA)
            states.add(AccessibleState.MULTI_LINE);
        if (fxRole == javafx.scene.AccessibleRole.TEXT_FIELD ||
            fxRole == javafx.scene.AccessibleRole.PASSWORD_FIELD)
            states.add(AccessibleState.SINGLE_LINE);

        AccessibleAttribute.ToggleState toggle =
            (AccessibleAttribute.ToggleState) query(AccessibleAttribute.TOGGLE_STATE);
        if (toggle == AccessibleAttribute.ToggleState.CHECKED)
            states.add(AccessibleState.CHECKED);

        return states;
    }

    @Override
    public int getAccessibleIndexInParent() {
        if (node.getParent() == null) return 0;
        ObservableList<Node> siblings = node.getParent().getChildrenUnmodifiable();
        return siblings.indexOf(node);
    }

    @Override
    public int getAccessibleChildrenCount() {
        if (node instanceof Parent p) {
            return p.getChildrenUnmodifiable().size();
        }
        return 0;
    }

    @Override
    public Accessible getAccessibleChild(int i) {
        if (node instanceof Parent p) {
            ObservableList<Node> children = p.getChildrenUnmodifiable();
            if (i >= 0 && i < children.size()) {
                return FxA11yAdapter.getInstance().getOrCreateAccessible(children.get(i), self);
            }
        }
        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Accessible getAccessibleParent() {
        return parent;
    }

    @Override
    public String getAccessibleName() {
        String name = queryString(AccessibleAttribute.TEXT);
        if (name != null) return name;
        return super.getAccessibleName();
    }

    @Override
    public String getAccessibleDescription() {
        String help = queryString(AccessibleAttribute.HELP);
        if (help != null) return help;
        return super.getAccessibleDescription();
    }

    // --- Capability accessors ---

    @Override
    public AccessibleComponent getAccessibleComponent() {
        return this;
    }

    @Override
    public javax.accessibility.AccessibleAction getAccessibleAction() {
        javafx.scene.AccessibleRole role = node.getAccessibleRole();
        if (role == javafx.scene.AccessibleRole.BUTTON ||
            role == javafx.scene.AccessibleRole.TOGGLE_BUTTON ||
            role == javafx.scene.AccessibleRole.CHECK_BOX ||
            role == javafx.scene.AccessibleRole.RADIO_BUTTON ||
            role == javafx.scene.AccessibleRole.HYPERLINK ||
            role == javafx.scene.AccessibleRole.MENU_ITEM ||
            role == javafx.scene.AccessibleRole.MENU_BUTTON ||
            role == javafx.scene.AccessibleRole.MENU ||
            role == javafx.scene.AccessibleRole.CHECK_MENU_ITEM ||
            role == javafx.scene.AccessibleRole.RADIO_MENU_ITEM ||
            role == javafx.scene.AccessibleRole.SPLIT_MENU_BUTTON) {
            return this;
        }
        return null;
    }

    @Override
    public javax.accessibility.AccessibleText getAccessibleText() {
        javafx.scene.AccessibleRole role = node.getAccessibleRole();
        if (role == javafx.scene.AccessibleRole.TEXT_FIELD ||
            role == javafx.scene.AccessibleRole.TEXT_AREA ||
            role == javafx.scene.AccessibleRole.PASSWORD_FIELD) {
            return this;
        }
        return null;
    }

    @Override
    public AccessibleValue getAccessibleValue() {
        javafx.scene.AccessibleRole role = node.getAccessibleRole();
        if (role == javafx.scene.AccessibleRole.SLIDER ||
            role == javafx.scene.AccessibleRole.PROGRESS_INDICATOR ||
            role == javafx.scene.AccessibleRole.SCROLL_BAR ||
            role == javafx.scene.AccessibleRole.SPINNER) {
            return this;
        }
        return null;
    }

    // --- AccessibleComponent ---

    @Override
    public Color getBackground() { return Color.WHITE; }
    @Override
    public void setBackground(Color c) {}
    @Override
    public Color getForeground() { return Color.BLACK; }
    @Override
    public void setForeground(Color c) {}
    @Override
    public Cursor getCursor() { return Cursor.getDefaultCursor(); }
    @Override
    public void setCursor(Cursor cursor) {}
    @Override
    public Font getFont() { return new Font("Dialog", Font.PLAIN, 12); }
    @Override
    public void setFont(Font f) {}
    @Override
    public FontMetrics getFontMetrics(Font f) { return null; }
    @Override
    public boolean isEnabled() { return !Boolean.TRUE.equals(query(AccessibleAttribute.DISABLED)); }
    @Override
    public void setEnabled(boolean b) {}
    @Override
    public boolean isVisible() { return Boolean.TRUE.equals(query(AccessibleAttribute.VISIBLE)); }
    @Override
    public void setVisible(boolean b) {}
    @Override
    public boolean isShowing() { return isVisible(); }
    @Override
    public boolean contains(Point p) {
        Rectangle bounds = getBounds();
        return bounds != null && bounds.contains(p);
    }

    @Override
    public Point getLocationOnScreen() {
        Bounds b = (Bounds) query(AccessibleAttribute.BOUNDS);
        if (b != null) return new Point((int) b.getMinX(), (int) b.getMinY());
        return new Point(0, 0);
    }

    @Override
    public Point getLocation() {
        return getLocationOnScreen();
    }

    @Override
    public void setLocation(Point p) {}

    @Override
    public Rectangle getBounds() {
        Bounds b = (Bounds) query(AccessibleAttribute.BOUNDS);
        if (b != null) return new Rectangle(
            (int) b.getMinX(), (int) b.getMinY(),
            (int) b.getWidth(), (int) b.getHeight());
        return new Rectangle(0, 0, 0, 0);
    }

    @Override
    public void setBounds(Rectangle r) {}

    @Override
    public Dimension getSize() {
        Rectangle r = getBounds();
        return new Dimension(r.width, r.height);
    }

    @Override
    public void setSize(Dimension d) {}

    @Override
    public Accessible getAccessibleAt(Point p) { return null; }

    @Override
    public boolean isFocusTraversable() { return node.isFocusTraversable(); }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> node.executeAccessibleAction(javafx.scene.AccessibleAction.REQUEST_FOCUS));
    }

    @Override
    public void addFocusListener(java.awt.event.FocusListener l) {}
    @Override
    public void removeFocusListener(java.awt.event.FocusListener l) {}

    // --- AccessibleAction ---

    @Override
    public int getAccessibleActionCount() {
        return 1;
    }

    @Override
    public String getAccessibleActionDescription(int i) {
        if (i == 0) {
            javafx.scene.AccessibleRole role = node.getAccessibleRole();
            if (role == javafx.scene.AccessibleRole.CHECK_BOX ||
                role == javafx.scene.AccessibleRole.TOGGLE_BUTTON ||
                role == javafx.scene.AccessibleRole.RADIO_BUTTON) return "toggle";
            if (role == javafx.scene.AccessibleRole.MENU ||
                role == javafx.scene.AccessibleRole.MENU_ITEM) return "activate";
            return "click";
        }
        return "";
    }

    @Override
    public boolean doAccessibleAction(int i) {
        if (i == 0) {
            Platform.runLater(() -> node.executeAccessibleAction(javafx.scene.AccessibleAction.FIRE));
            return true;
        }
        return false;
    }

    // --- AccessibleText ---

    @Override
    public int getIndexAtPoint(Point p) { return -1; }

    @Override
    public Rectangle getCharacterBounds(int i) { return null; }

    @Override
    public int getCharCount() {
        String text = queryString(AccessibleAttribute.TEXT);
        return text != null ? text.length() : 0;
    }

    @Override
    public int getCaretPosition() {
        Integer pos = (Integer) query(AccessibleAttribute.CARET_OFFSET);
        return pos != null ? pos : 0;
    }

    @Override
    public String getAtIndex(int part, int index) {
        String text = queryString(AccessibleAttribute.TEXT);
        if (text == null || index < 0 || index >= text.length()) return null;
        if (part == javax.accessibility.AccessibleText.CHARACTER) {
            return String.valueOf(text.charAt(index));
        } else if (part == javax.accessibility.AccessibleText.WORD) {
            int start = index;
            while (start > 0 && !Character.isWhitespace(text.charAt(start - 1))) start--;
            int end = index;
            while (end < text.length() && !Character.isWhitespace(text.charAt(end))) end++;
            return text.substring(start, end);
        } else if (part == javax.accessibility.AccessibleText.SENTENCE) {
            return text;
        }
        return null;
    }

    @Override
    public String getAfterIndex(int part, int index) {
        String text = queryString(AccessibleAttribute.TEXT);
        if (text == null || index + 1 >= text.length()) return null;
        return getAtIndex(part, index + 1);
    }

    @Override
    public String getBeforeIndex(int part, int index) {
        String text = queryString(AccessibleAttribute.TEXT);
        if (text == null || index <= 0) return null;
        return getAtIndex(part, index - 1);
    }

    @Override
    public javax.swing.text.AttributeSet getCharacterAttribute(int i) { return null; }

    @Override
    public int getSelectionStart() {
        Integer start = (Integer) query(AccessibleAttribute.SELECTION_START);
        return start != null ? start : 0;
    }

    @Override
    public int getSelectionEnd() {
        Integer end = (Integer) query(AccessibleAttribute.SELECTION_END);
        return end != null ? end : 0;
    }

    @Override
    public String getSelectedText() {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        if (start == end) return null;
        String text = queryString(AccessibleAttribute.TEXT);
        if (text == null) return null;
        return text.substring(Math.min(start, text.length()), Math.min(end, text.length()));
    }

    // --- AccessibleValue ---

    @Override
    public Number getCurrentAccessibleValue() {
        Double val = (Double) query(AccessibleAttribute.VALUE);
        return val != null ? val : 0.0;
    }

    @Override
    public boolean setCurrentAccessibleValue(Number n) {
        Platform.runLater(() -> node.executeAccessibleAction(javafx.scene.AccessibleAction.SET_VALUE, n.doubleValue()));
        return true;
    }

    @Override
    public Number getMinimumAccessibleValue() {
        Double val = (Double) query(AccessibleAttribute.MIN_VALUE);
        return val != null ? val : 0.0;
    }

    @Override
    public Number getMaximumAccessibleValue() {
        Double val = (Double) query(AccessibleAttribute.MAX_VALUE);
        return val != null ? val : 100.0;
    }

    // --- Query helpers ---

    private Object query(AccessibleAttribute attr, Object... params) {
        try {
            if (Platform.isFxApplicationThread()) {
                return node.queryAccessibleAttribute(attr, params);
            }
            var future = new java.util.concurrent.CompletableFuture<Object>();
            Platform.runLater(() -> {
                try {
                    future.complete(node.queryAccessibleAttribute(attr, params));
                } catch (Exception e) {
                    future.complete(null);
                }
            });
            return future.get(1, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    private String queryString(AccessibleAttribute attr) {
        Object val = query(attr);
        return val instanceof String s ? s : null;
    }
}
