package org.a11y.bridge;

import javax.accessibility.AccessibleRole;
import java.util.HashMap;
import java.util.Map;

public final class RoleMapping {

    // Values from atspi-constants.h AtspiRole enum (0-indexed)
    private static final Map<AccessibleRole, Integer> ROLE_MAP = new HashMap<>();

    static {
        ROLE_MAP.put(AccessibleRole.ALERT, 2);
        ROLE_MAP.put(AccessibleRole.CANVAS, 6);
        ROLE_MAP.put(AccessibleRole.CHECK_BOX, 7);
        ROLE_MAP.put(AccessibleRole.COLOR_CHOOSER, 9);
        ROLE_MAP.put(AccessibleRole.COLUMN_HEADER, 10);
        ROLE_MAP.put(AccessibleRole.COMBO_BOX, 11);
        ROLE_MAP.put(AccessibleRole.DATE_EDITOR, 12);
        ROLE_MAP.put(AccessibleRole.DESKTOP_ICON, 13);
        ROLE_MAP.put(AccessibleRole.DESKTOP_PANE, 14);
        ROLE_MAP.put(AccessibleRole.DIALOG, 16);
        ROLE_MAP.put(AccessibleRole.DIRECTORY_PANE, 17);
        ROLE_MAP.put(AccessibleRole.FILE_CHOOSER, 19);
        ROLE_MAP.put(AccessibleRole.FILLER, 20);
        ROLE_MAP.put(AccessibleRole.FONT_CHOOSER, 22);
        ROLE_MAP.put(AccessibleRole.FRAME, 23);
        ROLE_MAP.put(AccessibleRole.GLASS_PANE, 24);
        ROLE_MAP.put(AccessibleRole.HTML_CONTAINER, 25);
        ROLE_MAP.put(AccessibleRole.HYPERLINK, 88); // ATSPI_ROLE_LINK
        ROLE_MAP.put(AccessibleRole.ICON, 26);
        ROLE_MAP.put(AccessibleRole.INTERNAL_FRAME, 28);
        ROLE_MAP.put(AccessibleRole.LABEL, 29);
        ROLE_MAP.put(AccessibleRole.LAYERED_PANE, 30);
        ROLE_MAP.put(AccessibleRole.LIST, 31);
        ROLE_MAP.put(AccessibleRole.LIST_ITEM, 32);
        ROLE_MAP.put(AccessibleRole.MENU, 33);
        ROLE_MAP.put(AccessibleRole.MENU_BAR, 34);
        ROLE_MAP.put(AccessibleRole.MENU_ITEM, 35);
        ROLE_MAP.put(AccessibleRole.OPTION_PANE, 36);
        ROLE_MAP.put(AccessibleRole.PAGE_TAB, 37);
        ROLE_MAP.put(AccessibleRole.PAGE_TAB_LIST, 38);
        ROLE_MAP.put(AccessibleRole.PANEL, 39);
        ROLE_MAP.put(AccessibleRole.PASSWORD_TEXT, 40);
        ROLE_MAP.put(AccessibleRole.POPUP_MENU, 41);
        ROLE_MAP.put(AccessibleRole.PROGRESS_BAR, 42);
        ROLE_MAP.put(AccessibleRole.PUSH_BUTTON, 43);
        ROLE_MAP.put(AccessibleRole.RADIO_BUTTON, 44);
        ROLE_MAP.put(AccessibleRole.ROOT_PANE, 46);
        ROLE_MAP.put(AccessibleRole.ROW_HEADER, 47);
        ROLE_MAP.put(AccessibleRole.SCROLL_BAR, 48);
        ROLE_MAP.put(AccessibleRole.SCROLL_PANE, 49);
        ROLE_MAP.put(AccessibleRole.SEPARATOR, 50);
        ROLE_MAP.put(AccessibleRole.SLIDER, 51);
        ROLE_MAP.put(AccessibleRole.SPIN_BOX, 52);
        ROLE_MAP.put(AccessibleRole.SPLIT_PANE, 53);
        ROLE_MAP.put(AccessibleRole.STATUS_BAR, 54);
        ROLE_MAP.put(AccessibleRole.TABLE, 55);
        ROLE_MAP.put(AccessibleRole.TEXT, 61);
        ROLE_MAP.put(AccessibleRole.TOGGLE_BUTTON, 62);
        ROLE_MAP.put(AccessibleRole.TOOL_BAR, 63);
        ROLE_MAP.put(AccessibleRole.TOOL_TIP, 64);
        ROLE_MAP.put(AccessibleRole.TREE, 65);
        ROLE_MAP.put(AccessibleRole.VIEWPORT, 68);
        ROLE_MAP.put(AccessibleRole.WINDOW, 69);
        ROLE_MAP.put(AccessibleRole.EDITBAR, 77);
        ROLE_MAP.put(AccessibleRole.GROUP_BOX, 39);
        ROLE_MAP.put(AccessibleRole.HEADER, 71);
        ROLE_MAP.put(AccessibleRole.FOOTER, 72);
        ROLE_MAP.put(AccessibleRole.PARAGRAPH, 73);
        ROLE_MAP.put(AccessibleRole.RULER, 74);
    }

    private static final int ROLE_UNKNOWN = 67;

    private RoleMapping() {}

    public static int toAtspi(AccessibleRole role) {
        if (role == null) return ROLE_UNKNOWN;
        return ROLE_MAP.getOrDefault(role, ROLE_UNKNOWN);
    }

    public static String toRoleName(AccessibleRole role) {
        if (role == null) return "unknown";
        return role.toDisplayString(java.util.Locale.US).toLowerCase().replace(' ', '-');
    }
}
