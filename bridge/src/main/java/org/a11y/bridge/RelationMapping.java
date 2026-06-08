package org.a11y.bridge;

import javax.accessibility.AccessibleRelation;
import java.util.HashMap;
import java.util.Map;

public final class RelationMapping {

    private static final Map<String, Integer> RELATION_MAP = new HashMap<>();

    static {
        RELATION_MAP.put(AccessibleRelation.LABEL_FOR, 3);
        RELATION_MAP.put(AccessibleRelation.LABELED_BY, 2);
        RELATION_MAP.put(AccessibleRelation.MEMBER_OF, 4);
        RELATION_MAP.put(AccessibleRelation.CONTROLLER_FOR, 5);
        RELATION_MAP.put(AccessibleRelation.CONTROLLED_BY, 6);
        RELATION_MAP.put(AccessibleRelation.FLOWS_TO, 8);
        RELATION_MAP.put(AccessibleRelation.FLOWS_FROM, 9);
        RELATION_MAP.put(AccessibleRelation.SUBWINDOW_OF, 10);
        RELATION_MAP.put(AccessibleRelation.PARENT_WINDOW_OF, 11);
        RELATION_MAP.put(AccessibleRelation.EMBEDS, 12);
        RELATION_MAP.put(AccessibleRelation.EMBEDDED_BY, 13);
        RELATION_MAP.put(AccessibleRelation.CHILD_NODE_OF, 7);
    }

    private RelationMapping() {}

    public static int toAtspi(String relationKey) {
        return RELATION_MAP.getOrDefault(relationKey, 0);
    }
}
