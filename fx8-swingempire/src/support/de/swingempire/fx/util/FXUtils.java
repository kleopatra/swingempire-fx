/*
 * Created on 29.01.2014
 *
 */
package de.swingempire.fx.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;

import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.tk.Toolkit;

import de.swingempire.fx.scene.control.selection.AnchoredSelectionModel;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.util.Callback;

/**
 * Collection of static utility methods (mostly for debugging), should
 * be version-independent and useable by version-dependent sources.
 * 
 * No longer true: version should be 9+.
 * <p>
 * 
 * 
 * unused threading code copied from jfxtras
 * https://github.com/JFXtras/jfxtras/blob/8.0/jfxtras-test-support/src/main/java/jfxtras/test/TestUtil.java
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FXUtils {

    public final static String ANCHOR_KEY = "anchor";

// ------------------ logging
    
    /**
     * Returns the fx inputLogger configured to the given level with
     * default consoleHandler.
     * 
     * accessing and configuring internal logging
     * from: https://stackoverflow.com/q/20815048/203657
     *
     * @return
     */
    public static Logger getInputLogger(Level level) {
        return getInputLogger(level, null);
    }
    
    public static Logger getInputLogger(Level level, Formatter formatter) {
        return getLogger("javafx.scene.input", level, formatter);
        
    }
    /**
     * Returns the fx focusLogger configured to the given level with
     * default consoleHandler.
     * @return
     */
    public static Logger getFocusLogger(Level level) {
        return getLogger("javafx.scene.focus", level);
    }


    /**
     * @param name
     * @param level
     * @return
     */
    public static Logger getLogger(String name, Level level) {
        return getLogger(name, level, null);
    }


    /**
     * @param name
     * @param level the level to set for the consoleHandler
     * @param formatter
     * @return
     */
    public static Logger getLogger(String name, Level level,
            Formatter formatter) {
        getBaseLogger("");
        ConsoleHandler consoleHandler = new ConsoleHandler();
        if (formatter != null) {
            consoleHandler.setFormatter(formatter);
        }
        consoleHandler.setLevel(level);
        Logger input = getBaseLogger(name);
        input.setUseParentHandlers(false);
        input.addHandler(consoleHandler);
        return input;
    }

    /**
     * Returns a logger for the given name with Level.ALL
     */
    public static Logger getBaseLogger(String name) {
        Logger rootLogger = Logger.getLogger(name);
        rootLogger.setLevel(Level.ALL);
        return rootLogger;
    }
    
    
// -------------- reflection: BEWARE - don't use for production!
 

    /**
     * Utility method to hack around 
     * https://bugs.openjdk.java.net/browse/JDK-8209788
     * left/right keys dont move caret in textField if popup is displayed.
     * <p>
     * 
     * This method adds a changeListener to the combo's showingProperty,
     * which on first showing removes the traversal mappings  and removes itself.
     * @param cb the combo with a listView as popupContent 
     * 
     * @see #removeTraversalMappings(ComboBox)
     */
    public static <T> void removeTraversalMappingsInShownListener(ComboBox<T> cb) {
        cb.showingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable,
                    Boolean ov, Boolean nv) {
                if (nv) {
                    removeTraversalMappings(cb);
                    observable.removeListener(this);
                }
                
            } 
        });    
    }

    /**
     * Utility method to hack around 
     * https://bugs.openjdk.java.net/browse/JDK-8209788
     * left/right keys dont move caret in textField if popup is displayed.
     * <p>
     * 
     * This method sets an onShown handler that removes the traversalBindings
     * when first shown then nulls the handler.
     * 
     * @param cb the combo with a listView as popupContent 
     * @see #removeTraversalMappings(ComboBox)
     */
    public static <T> void removeTraversalMappingsOnShown(ComboBox<T> cb) {
        cb.setOnShown(e -> {
            removeTraversalMappings(cb);
            cb.setOnShown(null);
        });
    }
    
    /**
     * Utility method to hack around 
     * https://bugs.openjdk.java.net/browse/JDK-8209788
     * left/right keys dont move caret in textField if popup is displayed.
     * <p>
     * 
     * The hack is to remove all traversal mappings from the behavior's
     * inputMap. Note: this method assumes that all skins are instantiated.
     * 
     * @param cb the combo with a listView as popupContent 
     */
    public static <T> void removeTraversalMappings(ComboBox<T> cb) {
        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
        ListView<?> list = (ListView<?>) skin.getPopupContent();
        ListViewSkin<?> listSkin = (ListViewSkin<?>) list.getSkin();
        // reflective access to behavior
        ListViewBehavior<?> listBehavior = (ListViewBehavior<?>) FXUtils.invokeGetFieldValue(
                ListViewSkin.class, listSkin, "behavior");
        InputMap<?> map = listBehavior.getInputMap();
        map.getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings());
    }

    /**
     * This is a hack around InputMap not cleaning up internals on removing mappings.
     * We remove MousePressed/MouseReleased/MouseDragged mappings from the internal map.
     * <p>
     * 
     * Not needed in "later" versions of fx9, the bug is fixed.
     * 
     * @param inputMap
     */
//    public static void cleanupInputMap(InputMap<?> inputMap, EventType... types) {
//        Map eventTypeMappings = (Map) invokeGetFieldValue(InputMap.class, inputMap, "eventTypeMappings");
//        for (EventType eventType : types) {
//            eventTypeMappings.remove(eventType);
//        }
//    }

    
    /**
     * Reflectively access hidden field's value.
     * 
     * @param declaringClass the declaring class
     * @param target the instance to look up
     * @param name the field name
     * @return value of the field or null if something happened
     */
    public static Object invokeGetFieldValue(Class declaringClass, Object target, String name) {
        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Reflectively access hidden method's value without parameters.
     * 
     * @param declaringClass the declaring class
     * @param target the instance to look up
     * @param name the method name
     * @return value of the field or null if something happened
     */
    public static Object invokeGetMethodValue(Class declaringClass, Object target, String name) {
        try {
            Method field = declaringClass.getDeclaredMethod(name);
            field.setAccessible(true);
            return field.invoke(target);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Reflectively access method without paramters.
     * @param declaringClass the declaring class
     * @param target the instance to look up
     * @param name the method name
     */
    public static void invokeMethod(Class declaringClass, Object target, String name) {
        try {
            Method method = declaringClass.getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(target);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * Reflectively access hidden method value with a single parameter.
     * 
     * @param declaringClass the declaring class
     * @param target the instance to look up
     * @param name the field name
     * @return value of the field or null if something happened
     */
    public static Object invokeGetMethodValue(Class declaringClass, Object target, String name, Class paramType, Object param) {
        try {
            Method method = declaringClass.getDeclaredMethod(name, paramType);
            method.setAccessible(true);
            return method.invoke(target, param);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Invokes a multi-parameter method on target and returns its result.
     * 
     * @param declaringClass the declaring class
     * @param target the instance to look up
     * @param name the field name
     * @param paramTypes the types of the parameters
     * @param paramValues the values of the parameter (note: same order as types is assumed)
     * @return the return value of the method or null if anything happened 
     */
    public static Object invokeGetMethodValue(Class declaringClass, Object target, String name, Class[] paramTypes, Object[] paramValues) {
        try {
            Method method = declaringClass.getDeclaredMethod(name, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, paramValues);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
        
    }
    public static void invokeSetFieldValue(Class<?> declaringClass, Object target, String name, Object value) {
        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
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
    
    /**
     * Note: resets the change before processing, but not after!
     * @param change
     */
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

    /**
     * doesnt compile in fx11 - robot moved?
     * ----
     * Get current mouse location. <p>
     * 
     * Copied from efxclipse:
     * http://git.eclipse.org/
     * c/efxclipse/org.eclipse.efxclipse.git/tree/experimental
     * /swt/org.eclipse.fx.runtime.swt/src/ org/eclipse/swt/widgets/Display.java
     * 
     * @return
     */
//    public Point2D getCursorLocation() {
//        Robot r = null;
//        try {
//            r = com.sun.glass.ui.Application.GetApplication().createRobot();
//            return new Point2D(r.getMouseX(), r.getMouseY());
//        } finally {
//            if (r != null) {
//                r.destroy();
//            }
//        }
//    }

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
