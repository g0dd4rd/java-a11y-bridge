package org.a11y.fxadapter;

import java.lang.instrument.Instrumentation;

public class FxA11yAgent {

    public static void premain(String args, Instrumentation inst) {
        FxA11yAdapter.initialize();
    }
}
