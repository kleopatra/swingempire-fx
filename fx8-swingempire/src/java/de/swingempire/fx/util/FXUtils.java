/*
 * Created on 29.01.2014
 *
 */
package de.swingempire.fx.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collector;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import com.sun.javafx.tk.Toolkit;

import de.swingempire.fx.scene.control.selection.AnchoredSelectionModel;

/**
 * Collection of static utility methods (mostly for debugging)
 * 
 * unused threading code copied from jfxtras
 * https://github.com/JFXtras/jfxtras/blob/8.0/jfxtras-test-support/src/main/java/jfxtras/test/TestUtil.java
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FXUtils {

    public final static String ANCHOR_KEY = "anchor";

// -------------- aggregates
    
    public static <T>  Collector<T, ?, ObservableList<T>> toObservableList() {
        return Collector.of((Supplier<ObservableList<T>>) FXCollections::observableArrayList,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                });
    }
    
    public static <T>  Collector<T, ?, ObservableList<T>> toObservableList(Callback<T, Observable[]> extractor) {
        return Collector.of((Supplier<ObservableList<T>>) () -> FXCollections.observableArrayList(extractor),
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                });
    }
    
    public static <T>  Collector<T, ?, ObservableList<T>> toObservableList(ObservableList<T> target) {
        return Collector.of((Supplier<ObservableList<T>>) () -> target,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                });
    }
    
// ------------- various, not necessarily fx-related
    
    public static String version() {
        return version(false);
    }
    
    public static String version(boolean withOS) {
        String version = System.getProperty("java.version")+ "-" + System.getProperty("java.vm.version");
        return withOS ? version + " (" + System.getProperty("os.arch") + ")" : version;
    }
//--------------- list change

    public enum ChangeType {
        PERMUTATED(Change::wasPermutated),
        UPDATED(Change::wasUpdated),
        REPLACED(Change::wasReplaced),
        ADDED(c -> c.wasAdded() && !c.wasRemoved()),
        REMOVED(c -> c.wasRemoved() && !c.wasAdded()),;
        
        Predicate<Change> predicate;
        
        private ChangeType(Predicate<Change> p) {
            this.predicate = p;
        }
        
        public boolean test(Change c) {
            return predicate.test(c);
        }
    }
    
    public static int getAddedSize(Change c) {
        c.reset();
        int size = 0;
        while (c.next()) {
            size += c.getAddedSize();
        }
        return size;
    }
    
    public static int getRemovedSize(Change c) {
        c.reset();
        int size = 0;
        while (c.next()) {
            size += c.getRemovedSize();
        }
        return size;
    }
    
    
    public static int getChangeCount(Change c) {
        c.reset();
        int count = 0;
        while (c.next()) {
            count++;
        }
        return count;
    }
    
    public static int getChangeCount(Change c, ChangeType type) {
        c.reset();
        int count = 0;
        while (c.next()) {
            if (type.test(c))
                count++;
        }
        return count;
    }
    
    /**
     * Returns true if the content of the list was completely changed.
     * @param c
     * @return
     */
    public static boolean wasAllChanged(Change c) {
        return getAddedSize(c) == c.getList().size() ;
    }
    
    public static boolean wasSingleReplaced(Change c) {
        if (getChangeCount(c) != 1) return false;
        c.reset();
        c.next();
        return c.wasReplaced();
    }
    
    public static boolean wasSingleRemoved(Change c) {
        if (getChangeCount(c) != 1) return false;
        c.reset();
        c.next();
        return c.wasRemoved() && !c.wasAdded();
    }
    
    public static boolean wasSingleAdded(Change c) {
        if (getChangeCount(c) != 1) return false;
        c.reset();
        c.next();
        return c.wasAdded() && !c.wasRemoved();
    }
    
    public static boolean wasSinglePermutated(Change c) {
        if (getChangeCount(c) != 1) return false;
        c.reset();
        c.next();
        return c.wasPermutated();
    }
    
    public static boolean wasSingleUpdated(Change c) {
        if (getChangeCount(c) != 1) return false;
        c.reset();
        c.next();
        return c.wasUpdated();
    }
    
    
//-------------------anchor    
    
    //---------------------- end copy
    
    public static <T> void prettyPrint(Change<? extends T> change) {
        StringBuilder sb = new StringBuilder("Change event data on list: " + change.getList());
        sb.append("\n " + change.getClass() + "\n " + change);
        int i = 0;
        change.reset();
        while (change.next()) {
            sb.append("\n\tcursor = ").append(i++).append("\n");
    
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
    
            if (kind.equals("permutated")) {
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
        System.out.println(sb.toString());
    }

    public static class PrintingListChangeListener implements ListChangeListener {
        String source;
        int counter;
        public PrintingListChangeListener() {
        }
        
        public PrintingListChangeListener(String message, ObservableList<?> list) {
            list.addListener(this);
            source = message;
        }
        @Override
        public void onChanged(Change change) {
            System.out.println("Change #" + counter++ + " on " + source + "\nlist = " + change.getList());
            prettyPrint(change);
        }
    }

    public static int getAnchorIndex(ListView<?> view) {
        if (view.getSelectionModel() instanceof AnchoredSelectionModel) {
            return ((AnchoredSelectionModel) view.getSelectionModel()).getAnchorIndex();
        }
        Object anchor = view.getProperties().get(ANCHOR_KEY);
        return anchor != null ? (int) anchor : -1;
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
    
    private FXUtils() {
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FXUtils.class.getName());
}
