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
        System.out.println("java-a11y-bridge: A11yProvider.activate() called");

        boolean javafxPresent = false;
        try {
            Class.forName("javafx.stage.Window");
            javafxPresent = true;
        } catch (ClassNotFoundException ignored) {}

        System.out.println("java-a11y-bridge: JavaFX detected = " + javafxPresent);

        A11yBridge bridge = A11yBridge.getInstance();

        if (javafxPresent) {
            bridge.setDeferEmbed(true);
            bridge.start();
            startFxAdapter();
        } else {
            bridge.start();
        }
    }

    private void startFxAdapter() {
        try {
            Class<?> adapterClass = Class.forName("org.a11y.fxadapter.FxA11yAdapter");
            adapterClass.getMethod("initialize").invoke(null);
        } catch (ClassNotFoundException e) {
            // adapter JAR not on classpath — JavaFX a11y won't work but Swing still does
        } catch (Exception e) {
            System.err.println("java-a11y-bridge: failed to start JavaFX adapter: " + e);
        }
    }
}
