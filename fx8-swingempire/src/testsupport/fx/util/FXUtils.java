/*
 * Created on 29.01.2014
 *
 */
package fx.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;

import com.sun.javafx.tk.Toolkit;

/**
 * Collection of static utility methods (mostly for debugging)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FXUtils {

    private FXUtils() {
    }

//--------------- copied from TBee's TestUtils    
    static public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method also exist in PlatformUtil in commons, but we can't use that
     * here
     */
    static public void runAndWait(final Runnable runnable) {
        try {
            FutureTask future = new FutureTask(runnable, null);
            Platform.runLater(future);
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method also exist in PlatformUtil in commons, but we can't use that
     * here
     */
    static public <V> V runAndWait(final Callable<V> callable)
            throws InterruptedException, ExecutionException {
        FutureTask<V> future = new FutureTask<>(callable);
        Platform.runLater(future);
        return future.get();
    }

    /**
     * This method also exist in PlatformUtil in commons, but we can't use that
     * here
     */
    static public void waitForPaintPulse() {
        runAndWait(() -> {
            Toolkit.getToolkit().firePulse();
        });
    }

    /**
     *
     * @param r
     */
    static public void runThenWaitForPaintPulse(Runnable r) {
        runAndWait(r);
        waitForPaintPulse();
    }

    /**
     *
     * @param r
     * @return
     */
    static public <T> T runThenWaitForPaintPulse(Callable<T> r) {
        try {
            T t = runAndWait(r);
            waitForPaintPulse();
            return t;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

//---------------------- end copy
    
    public static <T> void prettyPrint(Change<? extends T> change) {
        StringBuilder sb = new StringBuilder("\tChange event data:\n");
        int i = 0;
        while (change.next()) {
            sb.append("\n " + change);
            sb.append("\t\tcursor = ").append(i++).append("\n");

            final String kind = change.wasPermutated() ? "permutated" : change
                    .wasReplaced() ? "replaced"
                    : change.wasRemoved() ? "removed"
                            : change.wasAdded() ? "added"
                                    : change.wasUpdated() ? "updated" : "none";
            sb.append("\t\tKind of change: ").append(kind).append("\n");

            sb.append("\t\tAffected range: [").append(change.getFrom())
                    .append(", ").append(change.getTo()).append("]\n");

            if (kind.equals("added") || kind.equals("replaced")) {
                sb.append("\t\tAdded size: ").append(change.getAddedSize())
                        .append("\n");
                sb.append("\t\tAdded sublist: ")
                        .append(change.getAddedSubList()).append("\n");
            }

            if (kind.equals("removed") || kind.equals("replaced")) {
                sb.append("\t\tRemoved size: ").append(change.getRemovedSize())
                        .append("\n");
                sb.append("\t\tRemoved: ").append(change.getRemoved())
                        .append("\n");
            }

            if (kind.equals("permutted")) {
                StringBuilder permutationStringBuilder = new StringBuilder("[");
                for (int k = change.getFrom(); k < change.getTo(); k++) {
                    permutationStringBuilder.append(k).append("->")
                            .append(change.getPermutation(k));
                    if (k < change.getTo() - 1) {
                        permutationStringBuilder.append(", ");
                    }
                }
                permutationStringBuilder.append("]");
                String permutation = permutationStringBuilder.toString();
                sb.append("\t\tPermutation: ").append(permutation).append("\n");
            }
        }
        LOG.info(sb.toString());
    };

    public static class MyListener implements ListChangeListener<String> {
        @Override
        public void onChanged(Change<? extends String> change) {
            System.out.println("\tlist = " + change.getList());
            prettyPrint(change);
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FXUtils.class.getName());
}
