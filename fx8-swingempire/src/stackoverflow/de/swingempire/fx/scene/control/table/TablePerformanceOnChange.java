/*
 * Created on 21.10.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * https://stackoverflow.com/q/58476480/203657
 * Performance degradation of TableView with constantly updated content
 * bottleneck (as reported by OP, result of profiling) is VirtualFlow layoutChildren which
 * calls setCellIndex which calls applyCss for all visible row, irrespective
 * of what the change is and where it happens.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePerformanceOnChange extends Application {
    private BorderPane border;
    private TableView<Item> tableView;
    private Button addItemToTv = new Button("Add");
    private Button replaceFirst = new Button("Replace first");
    private Button replaceLast = new Button("Replace last");
    private Button fullReplace = new Button("Full replace");

    private int counter = 0;

    protected Map<Integer, TableRow<Item>> rowsByIndex = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {

        constructTv();
        setButtonActions();
        Scene s = constructScene();

        primaryStage.setScene(s);
        primaryStage.show();
    }

    private Scene constructScene() {
        border = new BorderPane();
        border.setCenter(tableView);
        border.setTop(new HBox(addItemToTv, replaceFirst, replaceLast, fullReplace));

//        ScrollPane scp = new ScrollPane();
//        scp.setFitToHeight(true);
//        scp.setFitToWidth(true);
//        scp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scp.setContent(border);

        return new Scene(border);
    }

    private void setButtonActions() {
        addItemToTv.setOnAction(e -> {
            tableView.getItems().add(new Item("bla"));
            resetCounter();
//            Platform.runLater(this::resetCounter);
        });
        replaceFirst.setOnAction(e -> {
            tableView.getItems().set(0, new Item("blub"));
            resetCounter();
//            Platform.runLater(this::resetCounter);
        });
        fullReplace.setOnAction(e -> {
            ArrayList<Item> items = new ArrayList<>(tableView.getItems());
            tableView.getItems().setAll(items);
            resetCounter();
//            Platform.runLater(this::resetCounter);
        });
        replaceLast.setOnAction(e -> {
            tableView.getItems().set(tableView.getItems().size()-1, new Item("blub"));
            resetCounter();
//            Platform.runLater(this::resetCounter);
        });
    }


    private void resetCounter() {
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(500), (ee) -> {
            System.out.println(counter + " row calls");
            counter = 0;
        }));
        tl.play();

    }

    private Node constructTv() {
        tableView = new TableView<>();
        //tableView.setFixedCellSize(20);
        for(int i = 0; i<10; i++){
            TableColumn<Item, String> col = new TableColumn<>("col " + i);
            col.setCellValueFactory(param -> {
                return new SimpleStringProperty(param.getValue().getString());
            });
            tableView.getColumns().add(col);
        }

        for (int i = 0; i < 30 ; i++) {
            tableView.getItems().add(new Item("bla"));
        }

        tableView.setRowFactory(param -> {
            TableRow<Item> row = new TableRow<>();
            row.itemProperty().addListener((observable, oldValue, newValue) -> {
                    rowsByIndex.put(row.getIndex(), row);
//                    System.err.println("row change " + row.getIndex());
                    counter++;
                    Platform.runLater(() -> {
                });
            });
            return row;
        });

        return tableView;
    }


    public static void main(String[] args) {
        launch(args);
    }

    class Item {
        private String string;

        public Item(String s) {
            string = s;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }


}
