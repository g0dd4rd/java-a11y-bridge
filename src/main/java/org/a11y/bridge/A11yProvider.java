package org.a11y.bridge;

import javax.accessibility.AccessibilityProvider;

public class A11yProvider extends AccessibilityProvider {

    private static final String NAME = "org.a11y.bridge.A11yBridge";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void activate() {
        A11yBridge.getInstance().start();
    }
}
