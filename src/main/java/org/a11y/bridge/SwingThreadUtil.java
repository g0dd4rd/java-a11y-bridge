package org.a11y.bridge;

import javax.swing.SwingUtilities;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public final class SwingThreadUtil {

    private SwingThreadUtil() {}

    public static <T> T callOnEDT(Callable<T> fn, T defaultValue) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return fn.call();
            } catch (Exception e) {
                return defaultValue;
            }
        }
        RunnableFuture<T> task = new FutureTask<>(fn);
        SwingUtilities.invokeLater(task);
        try {
            return task.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void runOnEDT(Runnable fn) {
        if (SwingUtilities.isEventDispatchThread()) {
            fn.run();
        } else {
            SwingUtilities.invokeLater(fn);
        }
    }
}
