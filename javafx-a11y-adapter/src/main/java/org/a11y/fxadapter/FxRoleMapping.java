package org.a11y.fxadapter;

import javafx.scene.AccessibleRole;
import java.util.EnumMap;

public final class FxRoleMapping {

    private static final EnumMap<AccessibleRole, javax.accessibility.AccessibleRole> ROLE_MAP =
        new EnumMap<>(AccessibleRole.class);

    static {
        ROLE_MAP.put(AccessibleRole.BUTTON, javax.accessibility.AccessibleRole.PUSH_BUTTON);
        ROLE_MAP.put(AccessibleRole.CHECK_BOX, javax.accessibility.AccessibleRole.CHECK_BOX);
        ROLE_MAP.put(AccessibleRole.CHECK_MENU_ITEM, javax.accessibility.AccessibleRole.CHECK_BOX);
        ROLE_MAP.put(AccessibleRole.COMBO_BOX, javax.accessibility.AccessibleRole.COMBO_BOX);
        ROLE_MAP.put(AccessibleRole.CONTEXT_MENU, javax.accessibility.AccessibleRole.POPUP_MENU);
        ROLE_MAP.put(AccessibleRole.DATE_PICKER, javax.accessibility.AccessibleRole.DATE_EDITOR);
        ROLE_MAP.put(AccessibleRole.DECREMENT_BUTTON, javax.accessibility.AccessibleRole.PUSH_BUTTON);
        ROLE_MAP.put(AccessibleRole.HYPERLINK, javax.accessibility.AccessibleRole.HYPERLINK);
        ROLE_MAP.put(AccessibleRole.INCREMENT_BUTTON, javax.accessibility.AccessibleRole.PUSH_BUTTON);
        ROLE_MAP.put(AccessibleRole.IMAGE_VIEW, javax.accessibility.AccessibleRole.ICON);
        ROLE_MAP.put(AccessibleRole.LIST_VIEW, javax.accessibility.AccessibleRole.LIST);
        ROLE_MAP.put(AccessibleRole.LIST_ITEM, javax.accessibility.AccessibleRole.LIST_ITEM);
        ROLE_MAP.put(AccessibleRole.MENU, javax.accessibility.AccessibleRole.MENU);
        ROLE_MAP.put(AccessibleRole.MENU_BAR, javax.accessibility.AccessibleRole.MENU_BAR);
        ROLE_MAP.put(AccessibleRole.MENU_BUTTON, javax.accessibility.AccessibleRole.PUSH_BUTTON);
        ROLE_MAP.put(AccessibleRole.MENU_ITEM, javax.accessibility.AccessibleRole.MENU_ITEM);
        ROLE_MAP.put(AccessibleRole.NODE, javax.accessibility.AccessibleRole.PANEL);
        ROLE_MAP.put(AccessibleRole.PAGE_ITEM, javax.accessibility.AccessibleRole.PAGE_TAB);
        ROLE_MAP.put(AccessibleRole.PAGINATION, javax.accessibility.AccessibleRole.PAGE_TAB_LIST);
        ROLE_MAP.put(AccessibleRole.PARENT, javax.accessibility.AccessibleRole.PANEL);
        ROLE_MAP.put(AccessibleRole.PASSWORD_FIELD, javax.accessibility.AccessibleRole.PASSWORD_TEXT);
        ROLE_MAP.put(AccessibleRole.PROGRESS_INDICATOR, javax.accessibility.AccessibleRole.PROGRESS_BAR);
        ROLE_MAP.put(AccessibleRole.RADIO_BUTTON, javax.accessibility.AccessibleRole.RADIO_BUTTON);
        ROLE_MAP.put(AccessibleRole.RADIO_MENU_ITEM, javax.accessibility.AccessibleRole.RADIO_BUTTON);
        ROLE_MAP.put(AccessibleRole.SLIDER, javax.accessibility.AccessibleRole.SLIDER);
        ROLE_MAP.put(AccessibleRole.SPINNER, javax.accessibility.AccessibleRole.SPIN_BOX);
        ROLE_MAP.put(AccessibleRole.TEXT, javax.accessibility.AccessibleRole.LABEL);
        ROLE_MAP.put(AccessibleRole.TEXT_AREA, javax.accessibility.AccessibleRole.TEXT);
        ROLE_MAP.put(AccessibleRole.TEXT_FIELD, javax.accessibility.AccessibleRole.TEXT);
        ROLE_MAP.put(AccessibleRole.TOGGLE_BUTTON, javax.accessibility.AccessibleRole.TOGGLE_BUTTON);
        ROLE_MAP.put(AccessibleRole.TOOLTIP, javax.accessibility.AccessibleRole.TOOL_TIP);
        ROLE_MAP.put(AccessibleRole.SCROLL_BAR, javax.accessibility.AccessibleRole.SCROLL_BAR);
        ROLE_MAP.put(AccessibleRole.SCROLL_PANE, javax.accessibility.AccessibleRole.SCROLL_PANE);
        ROLE_MAP.put(AccessibleRole.SPLIT_MENU_BUTTON, javax.accessibility.AccessibleRole.PUSH_BUTTON);
        ROLE_MAP.put(AccessibleRole.TAB_ITEM, javax.accessibility.AccessibleRole.PAGE_TAB);
        ROLE_MAP.put(AccessibleRole.TAB_PANE, javax.accessibility.AccessibleRole.PAGE_TAB_LIST);
        ROLE_MAP.put(AccessibleRole.TABLE_CELL, javax.accessibility.AccessibleRole.LABEL);
        ROLE_MAP.put(AccessibleRole.TABLE_COLUMN, javax.accessibility.AccessibleRole.COLUMN_HEADER);
        ROLE_MAP.put(AccessibleRole.TABLE_ROW, javax.accessibility.AccessibleRole.LIST_ITEM);
        ROLE_MAP.put(AccessibleRole.TABLE_VIEW, javax.accessibility.AccessibleRole.TABLE);
        ROLE_MAP.put(AccessibleRole.THUMB, javax.accessibility.AccessibleRole.SLIDER);
        ROLE_MAP.put(AccessibleRole.TITLED_PANE, javax.accessibility.AccessibleRole.PANEL);
        ROLE_MAP.put(AccessibleRole.TOOL_BAR, javax.accessibility.AccessibleRole.TOOL_BAR);
        ROLE_MAP.put(AccessibleRole.TREE_ITEM, javax.accessibility.AccessibleRole.LABEL);
        ROLE_MAP.put(AccessibleRole.CHECK_BOX_TREE_ITEM, javax.accessibility.AccessibleRole.CHECK_BOX);
        ROLE_MAP.put(AccessibleRole.TREE_TABLE_CELL, javax.accessibility.AccessibleRole.LABEL);
        ROLE_MAP.put(AccessibleRole.TREE_TABLE_ROW, javax.accessibility.AccessibleRole.LIST_ITEM);
        ROLE_MAP.put(AccessibleRole.TREE_TABLE_VIEW, javax.accessibility.AccessibleRole.TABLE);
        ROLE_MAP.put(AccessibleRole.TREE_VIEW, javax.accessibility.AccessibleRole.TREE);
    }

    private FxRoleMapping() {}

    public static javax.accessibility.AccessibleRole map(AccessibleRole fxRole) {
        if (fxRole == null) return javax.accessibility.AccessibleRole.UNKNOWN;
        return ROLE_MAP.getOrDefault(fxRole, javax.accessibility.AccessibleRole.PANEL);
    }
}
