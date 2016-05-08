/*
 * Created on 09.06.2014
 *
 */
package de.swingempire.fx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.comboboxx.ComboBoxX;
import de.swingempire.fx.scene.control.selection.ListViewAnchored;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugUtils {

    public static String widthInfo(Control node) {
        String size = "width - pref/min/max \n   " + 
                node.getWidth() + " - " + node.prefWidth(-1) + 
                " / " + node.minWidth(-1) + " / " + node.maxWidth(-1);
        return size;
    }
    public static class ListXFacade<T> extends ListViewAnchored<T> implements
            Facade<T, ListViewAnchored<T>, MultipleSelectionModel<T>> {

        @Override
        public ListViewAnchored<T> getControl() {
            return this;
        }

    }

    public static class ListFacade<T> extends ListView<T> implements
            Facade<T, ListView<T>, MultipleSelectionModel<T>> {

        @Override
        public ListView<T> getControl() {
            return this;
        }
    }

    public static class TableFacade<T> extends TableView<T> implements
            Facade<T, TableView<T>, MultipleSelectionModel<T>> {

        @Override
        public TableView<T> getControl() {
            return this;
        }

    }

    /**
     * Common api that the TestEntry can manage.
     */
    public static interface Facade<T, V extends Control, S extends SelectionModel<T>> {
        V getControl();

        MultipleSelectionModel getSelectionModel();

        default int getSelectedIndex() {
            return getSelectionModel() != null ? getSelectionModel()
                    .getSelectedIndex() : -1;
        }

        ObservableList<T> getItems();

        void setItems(ObservableList<T> items);

        FocusModel getFocusModel();
    }

    public static void printSelectionState(ComboBox<?> choice) {
        String choiceClass = choice.getClass().getSimpleName();
        LOG.info(choiceClass + ": index/selectedItem/value/display "
                + choice.getSelectionModel().getSelectedIndex() + " / "
                + choice.getSelectionModel().getSelectedItem() + " / "
                + choice.getValue() + " / " + getDisplayText(choice));
    }

    public static void printSelectionState(ComboBoxX<?> choice) {
        String choiceClass = choice.getClass().getSimpleName();
        LOG.info(choiceClass + ": index/selectedItem/value/display "
                + choice.getSelectionModel().getSelectedIndex() + " / "
                + choice.getSelectionModel().getSelectedItem() + " / "
                + choice.getValue() + " / " + getDisplayText(choice)
                );
    }

    public static void printSelectionState(String message, Facade<?, ?, ?> choice) {
        String choiceClass = message + " " + choice.getClass().getSimpleName() ;
        LOG.info(choiceClass + "\n index/selectedItem/focus/focusItem "
                + choice.getSelectionModel().getSelectedIndex() + " / "
                + choice.getSelectionModel().getSelectedItem() + " / "
                + choice.getFocusModel().getFocusedIndex() + " / "
                + choice.getFocusModel().getFocusedItem());
    }
    
    public static void printSelectionState(ListView<?> choice) {
        String choiceClass = choice.getClass().getSimpleName();
        LOG.info(choiceClass + ": index/selectedItem/focus/focusItem "
                + choice.getSelectionModel().getSelectedIndex() + " / "
                + choice.getSelectionModel().getSelectedItem() + " / "
                + choice.getFocusModel().getFocusedIndex() + " / "
                + choice.getFocusModel().getFocusedItem());
    }
    
    public static void printSelectionState(TreeView<?> choice) {
        String choiceClass = choice.getClass().getSimpleName();
        LOG.info(choiceClass + ": index/selectedItem/focus/focusItem "
                + choice.getSelectionModel().getSelectedIndex() + " / "
                + choice.getSelectionModel().getSelectedItem() + " / "
                + choice.getFocusModel().getFocusedIndex() + " / "
                + choice.getFocusModel().getFocusedItem());
    }

    /**
     * PENDING JW: direct access to ComboBoxBaseSkin - factor into patch!
     * @param view
     * @return
     */
   public static String getDisplayText(Control view) {
        Skin skin = view.getSkin();
        if (!(skin instanceof ComboBoxBaseSkin))
            return null;
        Node node = ((ComboBoxBaseSkin) view.getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            return ((ListCell) node).getText();
        }
        return null;
    }

//---------------- event
    
    public static void printSourceTarget(Event event) {
        String es = null;
        if (event != null) {
            es = "source/target for " + event.getEventType()
                + "\n    " + getClazz(event.getSource())
                + "\n    " + getClazz(event.getTarget());
        }
        LOG.info(es);
    }
    
    public static String getClazz(Object instance) {
        return instance != null ? instance.getClass().getSimpleName() : "none";
    }
//------------- layout/bounds    
    @FunctionalInterface
    public interface AddBounds {
        void addBounds(Parent parent, Node node);
    }

    /**
     * just for fun: playing with method references.
     */
    public enum BoundsType implements AddBounds {
        LOCAL(DebugUtils::addBoundsInLocal), 
        INPARENT(DebugUtils::addBoundsInParent), 
        LAYOUT(DebugUtils::addLayoutBounds);

        private final AddBounds delegate;

        private BoundsType(AddBounds delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addBounds(Parent parent, Node node) {
            delegate.addBounds(parent, node);
        }
    }

    public static void printBounds(Node node) {
        String className = node.getClass().getName();
        String result = className  
                + "\n   " + "local:  " + node.getBoundsInLocal()
                + "\n   " + "parent: " + node.getBoundsInParent()
                + "\n   " + "layout: " + node.getLayoutBounds()
                ;
        LOG.info(result );
    }

    public static void printLocalTo(Node node) {
        Bounds local = node.getBoundsInLocal();
        String toTransform = "\n" +"local bounds " + local 
                + "\n   " + "to parent: " + node.localToParent(local)
                + "\n   " + "to scene:  " + node.localToScene(local)
                + "\n   " + "to screen: " + node.localToScreen(local);
        LOG.info(toTransform);
    }
    
    public static void printContextLocation(ContextMenuEvent event) {
        String location = "contextMenu on " + event.getSource().getClass().getName()
                + "\n   " + "x/y:       " + event.getX() + " / " + event.getY()
                + "\n   " + "sceneX/Y:  " + event.getSceneX() + " / " + event.getSceneY()
                + "\n   " + "screenX/Y: " + event.getScreenX() + " / " + event.getScreenY()
        ;
        LOG.info(location);
    }
    public static void addAllBounds(Parent parent, Node node) {
        // PENDING JW: see no way to use a method reference if the method
        // needs parameters
        // Arrays.asList(BoundsType.values()).stream().forEach(BoundsType::addBounds(parent,
        // node);
        // use a stream plus lambda
        Arrays.asList(BoundsType.values()).stream()
                .forEach(type -> type.addBounds(parent, node));
        // normal for each
        // for (BoundsType type : BoundsType.values()) {
        // addBounds(parent, node, type);
        // }
    }

    public static void addBounds(Parent parent, Node node, BoundsType type) {
        type.addBounds(parent, node);
    }

    /**
     * Adds a transparent rectangle with red Border to 
     * visualize the boundsInParen of the given node
     * @param parent
     * @param node
     */
    public static void addBoundsInParent(Parent parent, Node node) {
        Bounds bounds = node.getBoundsInParent();
        Color strokePaint = Color.RED;
        addBounds(parent, bounds, strokePaint);
    }

    /**
     * Adds a transparent rectangle with blue Border to 
     * visualize the boundsInLocal of the given node
     * @param parent
     * @param node
     */
    public static void addBoundsInLocal(Parent parent, Node node) {
        Bounds bounds = node.getBoundsInLocal();
        Color strokePaint = Color.BLUE;
        addBounds(parent, bounds, strokePaint);
    }

    /**
     * Adds a transparent rectangle with green Border to 
     * visualize the layoutBounds of the given node
     * @param parent
     * @param node
     */
    public static void addLayoutBounds(Parent parent, Node node) {
        Bounds bounds = node.getLayoutBounds();
        Color strokePaint = Color.GREEN;
        addBounds(parent, bounds, strokePaint);
    }

    /**
     * Adds a transparent Rectangle with stroke color at bounds to the parent if
     * the parent is of type Pane or Group. Does nothing silently otherwise.
     * 
     * @param parent the parent to add the rectangle to
     * @param bounds the bounds of the rectangle to add
     * @param strokePaint the color of the rectangle.
     */
    public static void addBounds(Parent root, Bounds inParent, Color strokePaint) {
        Rectangle r = new Rectangle(inParent.getMinX(), inParent.getMinY(),
                inParent.getWidth(), inParent.getHeight());
        r.setStroke(strokePaint);
        r.setFill(Color.TRANSPARENT);
        if (root instanceof Pane) {
            ((Pane) root).getChildren().add(r);
        } else if (root instanceof Group) {
            ((Group) root).getChildren().add(r);
        }
    }

    // ------------------- getting at child of particular type
    // ------------------- pretty sure there is some support, can't find it
    public ScrollBar getScrollBar(Parent table) {
        List<Node> allChildren = getAllNodes(table);
        for (Node node : allChildren) {
            if (node instanceof ScrollBar) {
                if (((ScrollBar) node).getOrientation() == Orientation.HORIZONTAL) {
                    return (ScrollBar) node;
                }
            }
        }
        return null;

    }

    public static List<Node> getAllNodes(Parent root) {
        List<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, List<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent) node, nodes);
        }
    }

    private DebugUtils() {
    };

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DebugUtils.class
            .getName());
}
