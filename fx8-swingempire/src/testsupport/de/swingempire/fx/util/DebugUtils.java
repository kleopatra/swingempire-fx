/*
 * Created on 09.06.2014
 *
 */
package de.swingempire.fx.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.sun.javafx.scene.control.skin.ComboBoxBaseSkin;

import de.swingempire.fx.scene.control.comboboxx.ComboBoxX;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugUtils {

    public static void printSelectionState(ComboBox<?> choice) {
        LOG.info("index/selectedItem/value/display " + choice.getSelectionModel().getSelectedIndex()
                + " / " + choice.getSelectionModel().getSelectedItem() 
                + " / " + choice.getValue()
                + " / " + getDisplayText(choice)
                );
    }
    
    public static void printSelectionState(ComboBoxX<?> choice) {
        LOG.info("index/selectedItem/value/display " + choice.getSelectionModel().getSelectedIndex()
                + " / " + choice.getSelectionModel().getSelectedItem() 
                + " / " + choice.getValue()
                + " / " + getDisplayText(choice)
                );
    }
    
    public static String getDisplayText(Control view) {
        Skin skin = view.getSkin();
        if (!(skin instanceof ComboBoxBaseSkin)) return null;
        Node node = ((ComboBoxBaseSkin) view.getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            return ((ListCell) node).getText();
        } 
        return null;
    }

    
    @FunctionalInterface
    public interface AddBounds {
        void addBounds(Parent parent, Node node);
    }
    
    /** 
     * just for fun: playing with method references.
     */
    public enum BoundsType  implements AddBounds {
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
    
    public static void addAllBounds(Parent parent, Node node) {
        // PENDING JW: see no way to use a method reference if the method 
        // needs parameters
//        Arrays.asList(BoundsType.values()).stream().forEach(BoundsType::addBounds(parent, node);
        // use a stream plus lambda
        Arrays.asList(BoundsType.values()).stream().forEach(type -> type.addBounds(parent, node));
        // normal for each
//        for (BoundsType type : BoundsType.values()) {
//           addBounds(parent, node, type);
//        }
    }
    
    public static void addBounds(Parent parent, Node node, BoundsType type) {
        type.addBounds(parent, node);
    }
    
    public static void addBoundsInParent(Parent parent, Node node) {
        Bounds bounds = node.getBoundsInParent();
        Color strokePaint = Color.RED;
        addBounds(parent, bounds, strokePaint);
    }
    
    public static void addBoundsInLocal(Parent parent, Node node) {
        Bounds bounds = node.getBoundsInLocal();
        Color strokePaint = Color.BLUE;
        addBounds(parent, bounds, strokePaint);
    }
    
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

//------------------- getting at child of particular type
//------------------- pretty sure there is some support, can't find it    
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



    private DebugUtils(){};

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DebugUtils.class
            .getName());
}
