/*
 * Created on 10.12.2019
 *
 */
package de.swingempire.fx.event;

import java.util.LinkedList;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/59264352/203657
 */
public class DragCellOutsideStage extends Application {

    public static final DataFormat LIST_DATA_FORMAT = new DataFormat("A nice format");
    public static final String CONTAINER_DEFAULT_STYLE = "-fx-border-color: indigo;\n" +
                                                    "    -fx-border-style: dotted;\n" +
                                                    "    -fx-background-color: #d1d1d1;" +
                                                    "    -fx-border-width: 3px;";
    public static final String CONTAINER_HIGHLIGHT_STYLE = "-fx-border-color: orange;\n" +
                                                        "    -fx-border-width: 5px;\n" +
                                                        "    -fx-border-style: solid;" +
                                                        "     -fx-background-color: gold";

    private Label dragLabel;



    @Override
    public void start(Stage stage) {
        // show transparent stage first
//        Pane transparentRoot = new Pane();
//        transparentRoot.setStyle("-fx-background-color: transparent");
//        Scene transparentScene = new Scene(transparentRoot, 1600, 1600);
////        transparentScene.setFill(Color.TRANSPARENT);
//        transparentRoot.prefWidthProperty().bind(transparentScene.widthProperty());
//        transparentRoot.prefHeightProperty().bind(transparentScene.heightProperty());
//
//        // *** this won't work ***
//        transparentRoot.setOnDragOver(event -> System.out.println("Drag over"));
//
//        Stage transparentStage = new Stage(StageStyle.TRANSPARENT);
//        transparentStage.setScene(transparentScene);
//        transparentStage.show();

        BorderPane root = new BorderPane();

        initUI(root);

        Scene scene = new Scene(root);

        scene.setOnDragOver(e ->  {
        });
        scene.setOnDragExited(e -> {
            
        });
        scene.setOnMouseDragged(e -> {
            MouseDragEvent p;
            System.out.println("from scene: " + e);
            
        });
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            
//            if (transparentStage !=null) transparentStage.close();
        }
        );
        stage.show();
    }


    private void initUI (BorderPane root) {
        ListView<String> listView = new ListView<>();
        listView.setPrefWidth(300);
        listView.setPrefHeight(400);
        listView.setCellFactory(new CellFactory());

        LinkedList<String> items = new LinkedList<>();
        items.add("Eat crunchies for breakfast");
        items.add("Feed the pigeons");
        items.add("Remember to breath");
        items.add("Stare at the clouds");
        items.add("Call grandma and ask her about health problems");

        listView.getItems().addAll(items);

        Label label = new Label("Things I have to do today:");
        label.setStyle("-fx-font-size: 14px");
        label.setPadding(new Insets(5, 5, 5, 5));
        VBox vBox = new VBox(label, listView);

        BorderPane dragContainer = new BorderPane();
        dragContainer.setPrefHeight(420);
        dragContainer.setPrefWidth(420);
        dragContainer.setStyle(CONTAINER_DEFAULT_STYLE);

        dragLabel = new Label("Drag things here! You won't regret it!");
        dragLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: indigo;");
        dragContainer.setCenter(dragLabel);

        setupDragListener(dragContainer);

        root.setPadding(new Insets(15, 15, 15, 15));
        root.setLeft(vBox);
        root.setRight(dragContainer);
    }


    private void setupDragListener (Pane container) {
        container.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Object content = event.getDragboard().getContent(LIST_DATA_FORMAT);

                if (!(content instanceof String)) return;

                event.acceptTransferModes(TransferMode.ANY);
            }
        });

        container.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                String content = (String) event.getDragboard().getContent(LIST_DATA_FORMAT);
                dragLabel.setText(content);
            }
        });

        container.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                container.setStyle(CONTAINER_HIGHLIGHT_STYLE);
            }
        });

        container.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                container.setStyle(CONTAINER_DEFAULT_STYLE);
            }
        });

        container.setOnDragDone(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                container.setStyle(CONTAINER_DEFAULT_STYLE);
            }
        });
    }


    class CellFactory implements Callback<ListView<String>, ListCell<String>> {

        @Override
        public ListCell<String> call(ListView<String> listview) {
            final ListCell<String> cell = new ListCell<>() {
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item,  empty);
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            // original
//            cell.setOnDragDetected((MouseEvent event) -> dragDetected(event, cell));
            
            cell.setOnDragDetected(e -> {
                System.out.println("in detected cell: " + e);
//                cell.startFullDrag();
                dragDetected(e, cell);
            });
            
            cell.setOnMouseDragged(e -> {
                
                System.out.println("in dragged cell: " + e);
            });
            
            cell.setOnMousePressed(e -> {
                
                System.out.println("in pressed cell: " + e);
                cell.setMouseTransparent(true);
                
            });
            cell.setOnMouseReleased(e -> {
                System.out.println("in released cell: " + e);
                Stage stage = createStage(e, cell);
                System.out.println("creating and showing");
                if (stage != null) {
                    stage.show();
                }
                cell.setMouseTransparent(false);
            });
            
            return cell;
        }


        private void dragDetected(MouseEvent event, ListCell<String> cell) {
            if (cell == null) return;

            DragEvent ev;
            Dragboard db = cell.startDragAndDrop(TransferMode.ANY);
            cell.startFullDrag();
            ClipboardContent content = new ClipboardContent();
            content.put(LIST_DATA_FORMAT, cell.getText());
            db.setContent(content);
            db.setDragView(cell.snapshot(null, null));
            event.consume();
        }
        
        private Stage createStage(MouseEvent e, Labeled button2) {
            double xScreen = e.getScreenX();
            double yScreen = e.getScreenY();
            Window bWindow = (Stage) button2.getScene().getWindow();
            if (xScreen < bWindow.getX() || yScreen < bWindow.getY()) {
                Stage stage = new Stage();
                stage.setX(xScreen - 50);
                stage.setY(yScreen - 50);
                BorderPane content = new BorderPane(new Button(button2.getText()));
                stage.setScene(new Scene(content)); 
                return stage;
            }
            return null;
        }

    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

