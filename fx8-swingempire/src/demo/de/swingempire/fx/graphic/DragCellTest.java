/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.graphic;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 */
public class DragCellTest extends Application {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage stage) throws Exception {
        TableRow row = new TableRow();
        row.setSkin(new TableRowSkin(row));
        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(flowPane, 500, 500);
        stage.setScene(scene);
        double circleRadius = 20;
        // TableCellBehavior b;
        // Circle circle = new Circle(circleRadius);
        TableCell tableCell = new TableCell() {

            @Override
            protected void updateItem(Object arg0, boolean arg1) {
                super.updateItem(arg0, arg1);
                setText("detached TableCell");
            }

        };
        tableCell.updateTableView(new TableView());
        tableCell.setMinWidth(100);
        // needed, otherwise not shown
        tableCell.updateIndex(-1);
        tableCell.setOnMouseDragged(createHandler(circleRadius, tableCell));

        Callback rowFactory = e -> {
            TableRow tableRow = new TableRow() {
                @Override
                protected void updateItem(Object arg0, boolean arg1) {
                    super.updateItem(arg0, arg1);
                    setText("detached TableRow");
                    LOG.info("getting here?" + getText());
                }

                @Override
                public void updateIndex(int arg0) {
                    super.updateIndex(arg0);
                    updateItem(null, true);
                }
                
                
            };
            return tableRow;

        };
        /*
         * TableRow not working at all, not even showing throwing up in
         * constructor of its skin
         */
        TableView tableForTableRow = new TableView();
//        tableForTableRow.setRowFactory(rowFactory);
        TableRow tableRow = (TableRow) rowFactory.call(null);
//        tableRow.updateTableView(tableForTableRow);
        tableRow.setMinWidth(100);
        tableRow.updateIndex(-1);
        tableRow.setOnMouseDragged(createHandler(circleRadius, tableRow));

        /**
         * ListCell can live standalone.
         */
        ListCell listCell = new ListCell() {
            @Override
            protected void updateItem(Object arg0, boolean arg1) {
                super.updateItem(arg0, arg1);
                setText("detached ListCell");
            }

        };
        listCell.updateListView(new ListView());
        listCell.setMinWidth(100);
        // not needed, all fine
        // listCell.updateIndex(-1);
        listCell.setOnMouseDragged(createHandler(circleRadius, listCell));

        TreeCell treeCell = new TreeCell() {

            @Override
            protected void updateItem(Object arg0, boolean arg1) {
                super.updateItem(arg0, arg1);
                setText("detached TreeCell");
            }

        };
        treeCell.updateTreeView(new TreeView());
        treeCell.setMinWidth(100);
        treeCell.setOnMouseDragged(createHandler(circleRadius, treeCell));

        flowPane.getChildren().addAll(
                listCell
                , tableCell
                , tableRow
                , treeCell);

        LOG.info("list/table/tree opacity: " + listCell.getOpacity() + " / "
                + tableCell.getOpacity() + " / " + treeCell.getOpacity());

        stage.show();
    }

    /**
     * @param circleRadius
     * @param listCell
     * @return
     */
    protected EventHandler<? super MouseEvent> createHandler(
            double circleRadius, Node listCell) {
        return mouseEvent -> {
//            circle.relocate(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            listCell.setTranslateX(mouseEvent.getSceneX() - circleRadius);
            listCell.setTranslateY(mouseEvent.getSceneY() - circleRadius);
        };
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DragCellTest.class.getName());
}