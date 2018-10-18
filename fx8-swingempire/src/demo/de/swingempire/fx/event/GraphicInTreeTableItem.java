/*
 * Created on 17.08.2018
 *
 */
package de.swingempire.fx.event;

import java.util.Random;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TreeTableRowSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

/**
 * example from 
 * https://bugs.openjdk.java.net/browse/JDK-8190331
 * 
 * TreeTableView: graphic of TreeItem is not show/hidden correctly after collapsing/expanding tree
 * 
 * was referenced by Ajit as might be related, in my report of graphic in checkboxtreecell:
 * https://bugs.openjdk.java.net/browse/JDK-8209017
 * 
 * One issue:
 * - start with root expanded
 * - click to collapse root
 * - expected: all child row empty
 * - actual: graphics on child rows still showing
 * 
 * 
 * @see CheckBoxTreeCellWithGraphic
 * @author Jeanette Winzenburg, Berlin
 */
public class GraphicInTreeTableItem extends Application
{
    private static final Color CATEGORY_COLOR = new Color(1.0, 1.0, 0.7, 1.0);
    private static final Color TOPIC_COLOR = new Color(0.7, 1.0, 1.0, 1.0);
    private static final Random random = new Random();

    public static class XTreeTableRow<T> extends TreeTableRow<T> {

        @Override
        protected Skin<?> createDefaultSkin() {
            return new XTreeTableRowSkin<>(this);
        }
        
        
    }
    
    public static class XTreeTableRowSkin<T> extends TreeTableRowSkin<T> {

        /**
         * @param control
         */
        public XTreeTableRowSkin(TreeTableRow<T> control) {
            super(control);
            control.treeItemProperty().addListener((scr, ov, nv) -> {
                LOG.info("treeItem changed: " + ov + " / " + nv ); 
                if (nv == null) {
                    Node g = getTreeItemGraphicAlias();
                    LOG.info("graphicAlias? " + g + getChildren().contains(g));
                    getChildren().remove(g);
                    clearTreeItemGraphicAlias();
                }
            });
        }

        @Override
        protected void updateChildren() {
            if (getSkinnable().isEmpty()) {
            }
            super.updateChildren();
        }

        /**
         * @return
         */
        private void clearTreeItemGraphicAlias() {
            FXUtils.invokeSetFieldValue(TreeTableRowSkin.class, this, "graphic", null);
        }
        
        /**
         * @return
         */
        private Node getTreeItemGraphicAlias() {
            return (Node) FXUtils.invokeGetFieldValue(TreeTableRowSkin.class, this, "graphic");
        }
        
        
        
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        final TreeItem<String> rootItem = new TreeItem<>("Root");
        rootItem.setExpanded(true);
        rootItem.setGraphic(createCircle(new Color(1.0, 0.7, 1.0, 1.0)));
        for (int i = 0; i < 1; i++) {
            final TreeItem<String> categoryItem = new TreeItem<>("Category " + i);
            categoryItem.setGraphic(createCircle(CATEGORY_COLOR));
            if (i == 0) {
                categoryItem.getGraphic().parentProperty().addListener((scr, ov, nv) -> {
                    String ovText = "none";
                    if (ov instanceof TreeTableRow) {
                        ovText = String.valueOf(((TreeTableRow) ov).getTreeItem());
                    }
                    String nvText = "none";
                    if (nv instanceof TreeTableRow) {
                        TreeTableRow treeTableRow = (TreeTableRow) nv;
                        treeTableRow.treeItemProperty().addListener((src, oi, ni) -> {
                            LOG.info("item change on row: " + oi + " / " + ni);
                        });
                        nvText = String.valueOf(treeTableRow.getTreeItem());
                        
                        
                    }
                    LOG.info("parent: \n  " + ovText + "\n  " + nvText);
                });
            }
            rootItem.getChildren().add(categoryItem);
            for (int j = 0; j < 5; j++) {
                final TreeItem<String> topicItem = new TreeItem<>("Topic " + (i * 10 + j));
                topicItem.setGraphic(createCircle(TOPIC_COLOR));
                categoryItem.getChildren().add(topicItem);
            }
        }
        final TreeTableView<String> treeTableView = new TreeTableView<>(rootItem);
//        treeTableView.setRowFactory(cb -> new XTreeTableRow<>());
        
        final TreeTableColumn<String, String> column1 = new TreeTableColumn<>();
        column1.setText("Name");
        column1.setPrefWidth(300.0);
        column1.setCellValueFactory(features -> new ReadOnlyStringWrapper(features.getValue().getValue()));

        final TreeTableColumn<String, Number> column2 = new TreeTableColumn<>();
        column2.setText("Size");
        column2.setPrefWidth(100.0);
        column2.setCellValueFactory(features -> new ReadOnlyIntegerWrapper(random.nextInt(1000)));

        treeTableView.getColumns().addAll(column1, column2);

        final BorderPane borderPane = new BorderPane(treeTableView);
        final Scene scene = new Scene(borderPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private static Node createCircle(Color color) {
        final Circle circle = new Circle(4.0);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(0.5);
        return circle;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(GraphicInTreeTableItem.class.getName());
}