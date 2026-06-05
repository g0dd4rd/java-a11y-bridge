package org.a11y.fxadapter;

import javafx.scene.Node;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;

public class FxAccessible implements Accessible {

    private final FxAccessibleContext context;

    public FxAccessible(Node node, FxAccessible parent) {
        this.context = new FxAccessibleContext(node, this, parent);
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        return context;
    }

    public Node getNode() {
        return context.getNode();
    }
}
