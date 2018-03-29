/*
 * Created on 29.03.2018
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Add row and immediately start edit.
 * https://stackoverflow.com/q/49531071/203657
 * 
 * Solution was the call to table.layout() - good enough, even with
 * core TextFieldTableCell. Dont even need layout? hmmm
 */
public class TableViewAddButtonEdit extends Application {

    private final ObservableList<Person> data = FXCollections.observableArrayList(createData());

    private final TableView table = new TableView();

    public static void main(String[] args) {
        launch(args);
    }

    private static List<Person> createData() {
        List<Person> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(new Person("Jacob" + i, "Smith", "jacob.smith_at_example.com"));
        }

        return data;
    }

    @Override
    public void start(Stage stage) {

        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(700);
        stage.setHeight(550);

        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));

        // Create a customer cell factory so that cells can support editing.
        Callback<TableColumn, TableCell> cellFactory = (TableColumn p) -> {
            return new SOEditingCell();
        };

//        Callback<TableColumn<Person, String>, TableCell<Person, String>> cellFactory = TextFieldTableCell.forTableColumn();
        // Set up the columns
        TableColumn firstNameCol = new TableColumn("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        firstNameCol.setCellFactory(cellFactory);

        TableColumn lastNameCol = new TableColumn("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));
        lastNameCol.setCellFactory(cellFactory);
        lastNameCol.setEditable(true);

        TableColumn primaryEmailCol = new TableColumn("Primary Email");
        primaryEmailCol.setMinWidth(200);
        primaryEmailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));
        primaryEmailCol.setCellFactory(cellFactory);
        primaryEmailCol.setEditable(false);

        TableColumn secondaryEmailCol = new TableColumn("Secondary Email");
        secondaryEmailCol.setMinWidth(200);
        secondaryEmailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("secondaryMail"));
        secondaryEmailCol.setCellFactory(cellFactory);

        // Add the columns and data to the table.
        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol, primaryEmailCol, secondaryEmailCol);
        table.setEditable(true);

        // --- Here comes the interesting part! ---
        //
        // A button that adds a row below the currently selected one
        // and immediatly starts editing it.
        Button addAndEdit = new Button("Add and edit");
        addAndEdit.setOnAction((ActionEvent e) -> {
            int idx = table.getSelectionModel().getSelectedIndex() + 1;

            data.add(idx, new Person("first", "last", "contact"));
            table.getSelectionModel().select(idx);
            table.edit(idx, firstNameCol);
        });

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.getChildren().addAll(label, table, addAndEdit);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }
    
    private class SOEditingCell extends TableCell<Person, String> {
        private TextField textField;

        public SOEditingCell() {
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (textField == null) {
                createTextField();
            }
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction(e -> {
                commitEdit(textField.getText());
            });
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    // JW: with keyHandler, getting NPE from behaviour activate
                    // because column of focusedCell is null
                    //  TablePositionBase<TC> cell = getFocusedCell();
                    // boolean isEditable = isControlEditable() && cell.getTableColumn().isEditable();
//                    if (t.getCode() == KeyCode.ENTER) {
//                        commitEdit(textField.getText());
//                        t.consume();
//                    } else 
                        
                    if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }



}

