/*
 * Created on 13.11.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.swingempire.fx.scene.control.cell.TableFlicker.NameCell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58836786/203657
 * 
 * flicker on hover - looks like a bug in cell re-use, table.refresh manually
 * seems to help ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableFlicker extends Application {

    private TableView<TableData> table = new TableView<>();
    private ObservableList<TableData> data = FXCollections.observableArrayList();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        scene.getStylesheets().add(getClass().getResource("tableflicker.css").toExternalForm());

        stage.setWidth(600);
        stage.setHeight(600);

        TableColumn<TableData, TableData> oneColumn = new TableColumn<>("One");
        oneColumn.setMinWidth(100);
        TableColumn<TableData, String> twoColumn = new TableColumn<>("Two");
        twoColumn.setMinWidth(100);
        TableColumn<TableData, String> threeColumn = new TableColumn<>("Three");
        threeColumn.setMinWidth(100);
        TableColumn<TableData, String> fourColumn = new TableColumn<>("Four");
        fourColumn.setMinWidth(100);

        table.setEditable(false);
        table.setPrefWidth(1100);
        table.setMaxWidth(1100);
        table.setItems(data);
        table.getColumns().addAll(oneColumn, twoColumn, threeColumn, fourColumn);
        table.setFixedCellSize(25.0);
        table.getStyleClass().add("customtable");
        table.getSelectionModel().setCellSelectionEnabled(false);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        oneColumn.setCellValueFactory((param) -> new SimpleObjectProperty<>(param.getValue()));
        oneColumn.setCellFactory((param) -> new NameCell());
        twoColumn.setCellValueFactory(new PropertyValueFactory<>("twoColumn"));
        twoColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getTwoColumn()));
        threeColumn.setCellValueFactory(new PropertyValueFactory<>("threeColumn"));
        threeColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getThreeColumn()));
        fourColumn.setCellValueFactory(new PropertyValueFactory<>("fourColumn"));
        fourColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper(p.getValue().getFourColumn()));

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        HBox hBox = new HBox(table);
        HBox.setHgrow(table, Priority.ALWAYS);
        ((Group) scene.getRoot()).getChildren().addAll(hBox);
        stage.setScene(scene);
        stage.show();

        //generate some random table data
        Task task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                Thread.sleep(100);
                for (int i = 0; i < 100000; i++) {
                    updateTableData();
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
        // PauseTransition pauseTransition = new PauseTransition(new Duration(100));
        // pauseTransition.setOnFinished(f -> updateTableData(););
        // pauseTransition.play();

    }


    private void updateTableData() {
        List<TableData> list = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            int rand = random.nextInt(20);
            list.add(new TableData("Manufacturer" + i, "User " + i, "value" + rand, "desc" + rand));
        }
        Platform.runLater(() -> {
            data.setAll(list);
            table.refresh();
        });
    }


    class NameCell extends TableCell<TableData, TableData> {

        public NameCell() {
            super();
        }

        @Override
        protected void updateItem(TableData item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                if (item.getOneColumn() != null) {
                    setText(item.getOneColumn());
                }
            } else {
                setText(null);
            }
        }
    }


    class TableData implements Serializable {

        private static final long serialVersionUID = 1L;
        private String oneColumn;
        private String twoColumn;
        private String threeColumn;
        private String fourColumn;


        public TableData(String oneColumn, String twoColumn, String threeColumn, String fourColumn) {
            this.oneColumn = oneColumn;
            this.twoColumn = twoColumn;
            this.threeColumn = threeColumn;
            this.fourColumn = fourColumn;
        }


        public String getOneColumn() {
            return oneColumn;
        }

        public String getTwoColumn() {
            return twoColumn;
        }

        public String getThreeColumn() {
            return threeColumn;
        }

        public String getFourColumn() {
            return fourColumn;
        }

    }

}

