/*
 * Created on 15.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ListCellSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Same layout quirks as with table -> issue in VirtualFlow?
 * @author Jeanette Winzenburg, Berlin
 */
public class PlainListViewDebugRowLayoutWithOverlay extends Application {

    private static class MyListCellSkin<T> extends ListCellSkin<T> {

        ScrollBar hbar;
        StackPane overlay;
        
        public MyListCellSkin(ListCell<T> control) {
            super(control);
            overlay = new StackPane();
            overlay.getStyleClass().setAll("overlay");
        }
        
        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            layoutOverlay(x, y, w, h);
        }


         private void layoutOverlay(double x, double y, double w, double h) {
             ScrollBar scrollBar = getHorizontalScrollBar();
             if (scrollBar == null)  return;
             double hbarValue = scrollBar.getValue();
             Point2D hbarParent = getSkinnable().localToParent(hbarValue, 0);
             if (!getChildren().contains(overlay)) {
                 // children are cleared on change notification from columns, re-add
                 getChildren().add(overlay);
             }
             overlay.toFront();
             // resize/relocate with +/- a single pixel to hack around 
//             overlay.resize(firstColumn.getWidth() + 1, h);
//             overlay.relocate(hbarValue - 1, 0);
             overlay.resize(10, h);
             overlay.relocate(hbarValue, 0);

        }

        protected ScrollBar getHorizontalScrollBar() {
            if (hbar == null) {
                VirtualFlow flow = getVirtualFlow();
                if (flow != null) {
                    hbar = (ScrollBar) FXUtils.invokeGetMethodValue(VirtualFlow.class, flow, "getHbar");
                    registerChangeListener(hbar.valueProperty(), e -> getSkinnable().requestLayout());
                }
            }
            return hbar;
        }
        
        // copied from TableRowSkinBase
        protected VirtualFlow getVirtualFlow() {
            Parent node = getSkinnable();
            while (node != null) {
                if (node instanceof VirtualFlow) {
                    return (VirtualFlow) node;
                }
                node = node.getParent();
            }
            return null;
        }
       
    }
    private static class MyListCell<T> extends ListCell<T> {

        @Override public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item instanceof Node) {
                setText(null);
                Node currentNode = getGraphic();
                Node newNode = (Node) item;
                if (currentNode == null || ! currentNode.equals(newNode)) {
                    setGraphic(newNode);
                }
            } else {
                /**
                 * This label is used if the item associated with this cell is to be
                 * represented as a String. While we will lazily instantiate it
                 * we never clear it, being more afraid of object churn than a minor
                 * "leak" (which will not become a "major" leak).
                 */
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new MyListCellSkin<>(this);
        }

    }

    private ScrollBar hbar;
    private Label valueLabel = new Label();

    private Parent createContent() {
        ListView<String> list = new ListView<>();
        list.setCellFactory(c -> new MyListCell<>());
        list.getItems().addAll("sommmmmmmmmmmmmmmmmmmmmmmmmme very loooooooooooooooooongish");
        
        double delta = 0.05;
        Button up = new Button("+");
        up.setOnAction(e -> {
            installHbar(list);
            double value = hbar.getValue();
            hbar.setValue(value + delta);
        });
        Button down = new Button("-");
        down.setOnAction(e -> {
            installHbar(list);
            double value = hbar.getValue();
            hbar.setValue(value - delta);
        });
        

        BorderPane content = new BorderPane(list);
        content.setBottom(new HBox(10, up, valueLabel, down));
        return content;
    }

    /**
     * @param table
     */
    protected void installHbar(Node table) {
        if (hbar == null) {
            hbar = getHbar(table);
            valueLabel.textProperty().bind(hbar.valueProperty().asString());
        }
    }


    public static ScrollBar getHorizontalScrollBar(VirtualFlow flow) {
        return (ScrollBar) FXUtils.invokeGetMethodValue(VirtualFlow.class, flow, "getHbar");
    }
    

    public static VirtualFlow getVirtualFlow(Node node) {
        return (VirtualFlow) node.lookup("VirtualFlow");
//        while (node != null) {
//            if (node instanceof VirtualFlow) {
//                return (VirtualFlow) node;
//            }
//            node = node.getParent();
//        }
//        return null;
    }
    
    public static ScrollBar getHbar(Node node) {
        VirtualFlow flow = getVirtualFlow(node);
        return getHorizontalScrollBar(flow);
    }
    

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("overlaycell.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PlainListViewDebugRowLayoutWithOverlay.class.getName());

}
