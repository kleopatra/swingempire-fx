/*
 * Created on 10.02.2016
 *
 */
package de.swingempire.fx.scene.control.edit;

import de.swingempire.fx.scene.control.edit.TablePJamesAddRowAndEdit.EditingCellWithMenuEtc;
import javafx.application.Application;
import javafx.beans.value.ObservableValueBase;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Solution by James:
 * http://stackoverflow.com/a/35283330/203657
 * 
 * do layout in cell (button/menu action) 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePJamesAddRowAndEdit extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<String> table = new TableView<>();
        table.setEditable(true);

        TableColumn<String, String> column = new TableColumn<>("Data");
        column.setPrefWidth(150);
        table.getColumns().add(column);

        // use trivial wrapper for string data:
        column.setCellValueFactory(cellData -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return cellData.getValue();
            }
        });

        column.setCellFactory(col -> new EditingCellWithMenuEtc());

        column.setOnEditCommit(e -> 
            table.getItems().set(e.getTablePosition().getRow(), e.getNewValue()));

        for (int i = 1 ; i <= 20; i++) {
            table.getItems().add("Item "+i);
        }
        // blank for "add" button:
        table.getItems().add("");

        BorderPane root = new BorderPane(table);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

    }

    public static class EditingCellWithMenuEtc extends TableCell<String, String> {
        private TextField textField ;
        private Button button ;
        private ContextMenu contextMenu ;

        // The update relies on knowing both the item and the index
        // Since we don't know (or at least shouldn't rely on) the order
        // in which the item and index are updated, we just delegate
        // implementations of both updateItem and updateIndex to a general
        // method. This way doUpdate() is always called last with consistent
        // state, so we are guaranteed to be in a consistent state when the
        // cell is rendered, even if we are temporarily in an inconsistent 
        // state between the calls to updateItem and updateIndex.

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            doUpdate(item, getIndex(), empty);
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);
            doUpdate(getItem(), index, isEmpty());
        }

        // update the cell. This updates the text, graphic, context menu
        // (empty cells and the special button cell don't have context menus)
        // and editable state (empty cells and the special button cell can't
        // be edited)
        private void doUpdate(String item, int index, boolean empty) {
            if (empty) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
                setEditable(false);
            } else {
                if (index == getTableView().getItems().size() - 1) {
                    setText(null);
                    setGraphic(getButton());
                    setContextMenu(null);
                    setEditable(false);
                } else if (isEditing()) {
                    setText(null);
                    getTextField().setText(item);
                    setGraphic(getTextField());
                    getTextField().requestFocus();
                    setContextMenu(null);
                    setEditable(true);
                } else {
                    setText(item);
                    setGraphic(null);
                    setContextMenu(getMenu());
                    setEditable(true);
                }
            }
        }

        @Override
        public void startEdit() {
            if (! isEditable() 
                    || ! getTableColumn().isEditable()
                    || ! getTableView().isEditable()) {
                return ;
            }
            super.startEdit();
            getTextField().setText(getItem());
            setText(null);
            setGraphic(getTextField());
            setContextMenu(null);
            textField.selectAll();
            textField.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
            setContextMenu(getMenu());
        }

        @Override
        public void commitEdit(String newValue) {
            // note this fires onEditCommit handler on column:
            super.commitEdit(newValue);
            setText(getItem());
            setGraphic(null);
            setContextMenu(getMenu());
        }

        private void addNewItem(int index) {
            getTableView().getItems().add(index, "New Item");
            // force recomputation of cells:
            getTableView().layout();
            // start edit:
            getTableView().edit(index, getTableColumn());
        }

        private ContextMenu getMenu() {
            if (contextMenu == null) {
                createContextMenu();
            }
            return contextMenu ;
        }

        private void createContextMenu() {
            MenuItem addNew = new MenuItem("Add new");
            addNew.setOnAction(e -> addNewItem(getIndex() + 1));
            MenuItem edit = new MenuItem("Edit");
            // note we call TableView.edit(), not this.startEdit() to ensure 
            // table's editing state is kept consistent:
            edit.setOnAction(e -> getTableView().edit(getIndex(), getTableColumn()));
            contextMenu = new ContextMenu(addNew, edit);
        }

        private Button getButton() {
            if (button == null) {
                createButton();
            }
            return button ;
        }

        private void createButton() {
            button = new Button("Add");
            button.prefWidthProperty().bind(widthProperty());
            button.setOnAction(e -> addNewItem(getTableView().getItems().size() - 1));
        }

        private TextField getTextField() {
            if (textField == null) {
                createTextField();
            }
            return textField ;
        }

        private void createTextField() {
            textField = new TextField();
            // use setOnAction for enter, to avoid conflict with enter on cell:
            textField.setOnAction(e -> commitEdit(textField.getText()));
            // use key released for escape: note text fields do note consume
            // key releases they don't handle:
            textField.setOnKeyReleased(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

