/*
 * Created on 06.02.2016
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * 
 * http://stackoverflow.com/q/35279377/203657
 * add and edit can confuse the tableView
 * Solution is to _always_ make sure the target cell is visible
 * and force a complete layout!
 * 
 * Also old bug:
 * https://bugs.openjdk.java.net/browse/JDK-8093922
 * 
 * resolved as can't reproduce
 * 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePersonCoreAddAndEdit extends Application {

    private Parent getContent() {
        TableView<Dummy> table = new TableView<>(createData(50));
        table.setEditable(true);
        
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setMinWidth(200);
        table.getColumns().addAll(column);
        
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
            int insertIndex = selected < 0 ? 0 : selected;
//            Dummy dummy = new Dummy();
//            table.getItems().add(insertIndex, dummy);
//            LOG.info("insertIndex" + insertIndex + "isAtIndex " + table.getItems().indexOf(dummy) + dummy);
            table.edit(insertIndex,  column);
        });
        
        Button addAndEdit = new Button("AddAndEdit");
        addAndEdit.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
            int insertIndex = 20; //selected < 0 ? 0 : selected;
            Dummy dummy = new Dummy();
            table.getItems().add(insertIndex, dummy);
            table.edit(insertIndex,  column);
            LOG.info("insertIndex" + insertIndex + "isAtIndex " + table.getItems().indexOf(dummy) + dummy);
        });
        
        Button logEditing = new Button("LogEditing");
        logEditing.setOnAction(e-> {
            TablePosition editingCell = table.getEditingCell();
            LOG.info(editingCell != null ? "editing row: " + editingCell.getRow() : "no editing cell");
        });
        Button scrollAndEdit = new Button("ScrollAndEdit");
        scrollAndEdit.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
            int insertIndex = selected < 0 ? 0 : selected;
            table.requestFocus();
            table.scrollTo(insertIndex);
//            table.layout();
            table.edit(insertIndex,  column);
        });
        HBox buttons = new HBox(10, edit, addAndEdit, logEditing, scrollAndEdit);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }

    private ObservableList<Dummy> createData(int size) {
        return FXCollections.observableArrayList(
                Stream.generate(Dummy::new)
                .limit(size)
                .collect(Collectors.toList()));
    }
    
    private static class Dummy {
        private static int count;
        StringProperty value = new SimpleStringProperty(this, "value", "initial " + count++);
        public StringProperty valueProperty() {return value;}
        public String getValue() {return valueProperty().get(); }
        public void setValue(String text) {valueProperty().set(text); }
        public String toString() {return "[dummy: " + getValue() + "]";}
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TablePersonCoreAddAndEdit.class.getName());
}
