    /*
     * Created on 26.09.2017
     *
     */
    package de.swingempire.fx.scene.control.edit;
    
    import java.util.function.Function;

import de.swingempire.fx.scene.control.edit.TableEditSO.Building;
import de.swingempire.fx.scene.control.edit.TableEditSO.EditingStringCell;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
    
/**
 * https://stackoverflow.com/q/46396423/203657
 */
public class TableEditSO extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
        // root.setCenter(new TableExample());
        root.setCenter(getContent());
    }

    private Parent getContent() {
        TableView<Building> tableView = new TableView<>();
        tableView.setEditable(true);

        // adds a new blank row to table;
        Button add = new Button("add");
        add.setOnAction(e -> {
            Building newBuilding = new Building();
            // modifications to the items will cancel an edit
            System.out.println("before adding: " + tableView.getEditingCell());
            tableView.getItems().add(0, newBuilding);
            tableView.edit(0, tableView.getColumns().get(0));

            // I tried focusing and selecting the row
            // but does not help.
            tableView.getFocusModel().focus(0);
            tableView.getSelectionModel().select(0);

        });

        Button edit = new Button("edit");
        edit.setOnAction(e -> {
            int size = tableView.getItems().size();
            if (size < 2)
                return;
            TablePosition pos = tableView.getEditingCell();
            int last = size - 1;
            if (pos != null) {
                int row = pos.getRow();
                if (row == last) {
                    // make certain we switch editing to another row
                    last--;
                }
            }
            System.out.println("before starting edit on : " + last);
            // starting edit on another item will cancel an edit
            tableView.edit(last, tableView.getColumns().get(0));
        });

        Building building = new Building();
        building.setName("Building 100");

        Function<Building, StringProperty> property = Building::nameProperty;

        TableColumn<Building, String> column = new TableColumn<>("name");
        column.setEditable(true);
        column.setCellValueFactory(
                cellData -> property.apply(cellData.getValue()));
        column.setCellFactory(p -> new EditingStringCell<>());

        tableView.getColumns().add(column);
        tableView.setItems(FXCollections.observableArrayList(building));

        VBox box = new VBox(10, add, edit, tableView);
        return box;
    }

    // don't extend without functional reason
    public class TableExample extends VBox {
        public TableExample() {
            TableView<Building> tableView = new TableView<>();
            tableView.setEditable(true);

            // adds a new blank row to table;
            Button add = new Button("add");
            add.setOnAction(e -> {
                Building newBuilding = new Building();
                System.out.println(
                        "before adding: " + tableView.getEditingCell());

                tableView.getItems().add(0, newBuilding);
                System.out
                        .println("after adding: " + tableView.getEditingCell());
                tableView.edit(0, tableView.getColumns().get(0));
                System.out.println(
                        "after starting: " + tableView.getEditingCell());

                // I tried focusing and selecting the row
                // but does not help.
                tableView.getFocusModel().focus(0);
                tableView.getSelectionModel().select(0);

            });

            getChildren().addAll(add, tableView);

            Building building = new Building();
            building.setName("Building 100");

            Function<Building, StringProperty> property = Building::nameProperty;

            TableColumn<Building, String> column = new TableColumn<>("name");
            column.setEditable(true);
            column.setCellValueFactory(
                    cellData -> property.apply(cellData.getValue()));
            column.setCellFactory(p -> new EditingStringCell<>());

            tableView.getColumns().add(column);
            tableView.setItems(FXCollections.observableArrayList(building));
        }
    }

    public static class EditingStringCell<S, T>
            extends TableCell<Building, String> {
        static int id;

        private int myId;

        private TextField textField;

        public EditingStringCell() {
            // just to see re-use of cells
            myId = id++;
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            // not needed, it's true by default
            // setEditable(true);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : item);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEditing()) {
                System.out.println(
                        "start edit " + getIndex() + " on id: " + myId);
                updateEditControl(getItem());

            }
        }

        private void updateEditControl(String item) {
            if (textField == null) {
                textField = new TextField();
                // always use the highest abstract available
                // so use action instead of keyEvent
                textField.setOnAction(this::handleAction);
                // textField.addEventHandler(KeyEvent.KEY_PRESSED,
                // this::handleKeyPressed);
                setGraphic(textField);
            }
            textField.setText(item == null ? "" : (item));
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        private void handleAction(ActionEvent ae) {
            commitEdit(textField.getText());

        }

        // whenever pressing enter on a new "blank" row
        // cancelEdit() gets called. But if you try again
        // it works.
        private void handleKeyPressed(KeyEvent e) {
            if (e.getCode() == KeyCode.ENTER) {
                commitEdit(textField.getText());
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            System.out.println("cancel edit " + getIndex() + " on id: " + myId);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);
            System.out.println("commited: " + newValue + " index: " + getIndex()
                    + " on id: " + myId);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    public class Building {
        private final StringProperty name = new SimpleStringProperty();

        public StringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}