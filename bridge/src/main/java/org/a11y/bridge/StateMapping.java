package org.a11y.bridge;

import org.freedesktop.dbus.types.UInt32;

import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StateMapping {

    // Values from atspi-constants.h AtspiStateType enum (0-indexed)
    private static final Map<AccessibleState, Integer> STATE_MAP = new HashMap<>();

    static {
        STATE_MAP.put(AccessibleState.ACTIVE, 1);
        STATE_MAP.put(AccessibleState.ARMED, 2);
        STATE_MAP.put(AccessibleState.BUSY, 3);
        STATE_MAP.put(AccessibleState.CHECKED, 4);
        // 5 = COLLAPSED (handled via EXPANDED logic)
        // 6 = DEFUNCT
        STATE_MAP.put(AccessibleState.EDITABLE, 7);
        STATE_MAP.put(AccessibleState.ENABLED, 8);
        STATE_MAP.put(AccessibleState.EXPANDABLE, 9);
        STATE_MAP.put(AccessibleState.EXPANDED, 10);
        STATE_MAP.put(AccessibleState.FOCUSABLE, 11);
        STATE_MAP.put(AccessibleState.FOCUSED, 12);
        // 13 = HAS_TOOLTIP
        STATE_MAP.put(AccessibleState.HORIZONTAL, 14);
        STATE_MAP.put(AccessibleState.ICONIFIED, 15);
        STATE_MAP.put(AccessibleState.MODAL, 16);
        STATE_MAP.put(AccessibleState.MULTI_LINE, 17);
        STATE_MAP.put(AccessibleState.MULTISELECTABLE, 18);
        STATE_MAP.put(AccessibleState.OPAQUE, 19);
        STATE_MAP.put(AccessibleState.PRESSED, 20);
        STATE_MAP.put(AccessibleState.RESIZABLE, 21);
        STATE_MAP.put(AccessibleState.SELECTABLE, 22);
        STATE_MAP.put(AccessibleState.SELECTED, 23);
        // 24 = SENSITIVE
        STATE_MAP.put(AccessibleState.SHOWING, 25);
        STATE_MAP.put(AccessibleState.SINGLE_LINE, 26);
        // 27 = STALE
        STATE_MAP.put(AccessibleState.TRANSIENT, 28);
        STATE_MAP.put(AccessibleState.VERTICAL, 29);
        STATE_MAP.put(AccessibleState.VISIBLE, 30);
        STATE_MAP.put(AccessibleState.MANAGES_DESCENDANTS, 31);
        STATE_MAP.put(AccessibleState.INDETERMINATE, 32);
        // 33 = REQUIRED
        STATE_MAP.put(AccessibleState.TRUNCATED, 34);
    }

    private StateMapping() {}

    public static List<UInt32> toAtspi(AccessibleStateSet stateSet) {
        long bits = 0;
        if (stateSet != null) {
            for (AccessibleState state : stateSet.toArray()) {
                Integer bit = STATE_MAP.get(state);
                if (bit != null) {
                    bits |= (1L << bit);
                }
            }
        }
        return List.of(
            new UInt32((int) (bits & 0xFFFFFFFFL)),
            new UInt32((int) ((bits >>> 32) & 0xFFFFFFFFL))
        );
    }

    public static int toBit(AccessibleState state) {
        Integer bit = STATE_MAP.get(state);
        return bit != null ? bit : -1;
    }
}
