/*
 * Created on 06.02.2016
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*
;

import de.swingempire.fx.scene.control.cell.DebugTextFieldTableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;/**
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
 * Inconsistent editingCell
 * - add and start edit on a cell off the visible pane
 * - click log the editing cell: has the row that was inserted
 * - scrollto the inserted row with button: receiving cancel and no edit
 * 
 * This was introduced as bug fix for data corruption, nevertheless its
 * inconsistent
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePersonCoreAddAndEdit extends Application {

    private Parent getContent() {
        TableView<Dummy> table = new TableView<>(createData(50)) {
            @Override
            public void edit(int row, TableColumn<Dummy, ?> column) {
                Exception ex = new RuntimeException("dummy");
                StackTraceElement[] stackTrace = ex.getStackTrace();
                TablePosition old = getEditingCell();
                String oldText = " no old editing";
                if (old != null) {
                    oldText = " old editing: " + old.getRow() + " / " + old.getColumn();
                }
                
                String caller = "CALLER-OF-EDIT - new editing " + row + " / " + column +  oldText + "\n";
                int max = Math.min(5, stackTrace.length);
                for (int i = 1; i < max; i++) { // first is this method
                        caller+= stackTrace[i].getClassName() + 
                        " / "+  stackTrace[i].getMethodName() + " / " + stackTrace[i].getLineNumber() + "\n";
                }
                LOG.info(caller);
//                layout();
                super.edit(row, column);

        }};
        // only with cell selection enabled the listener to cell's focusedProperty
        // ever jumps in
//        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(true);
        
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(TextFieldTableCell.forTableColumn());
//        column.setCellFactory(DebugTextFieldTableCell.forTableColumn());
        column.setMinWidth(200);
        table.getColumns().addAll(column);
        
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
            int insertIndex = selected < 0 ? 0 : selected;
            table.edit(insertIndex,  column);
        });
        
        Button addAndEdit = new Button("AddAndEdit");
        addAndEdit.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
            int insertIndex = 20; //selected < 0 ? 0 : selected;
            Dummy dummy = new Dummy();
            table.getItems().add(insertIndex, dummy);
            table.getSelectionModel().select(insertIndex);
            table.edit(insertIndex,  column);
            LOG.info("insertIndex" + insertIndex + "isAtIndex " + table.getItems().indexOf(dummy) + dummy);
        });
        
        Button logEditing = new Button("LogEditing");
        logEditing.setOnAction(e-> {
            TablePosition editingCell = table.getEditingCell();
            LOG.info(editingCell != null ? "editing row: " + editingCell.getRow() : "no editing cell");
        });
        Button scroll = new Button("ScrollToSelected");
        scroll.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex();
//            table.requestFocus();
            table.scrollTo(selected);
//            table.layout();
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
        
        Button tryEditingCell = new Button("setText on editing cell");
        // first press AddAndEdit, then this
        tryEditingCell.setOnAction(e -> {
            TablePosition editingCell = table.getEditingCell();
            if (editingCell == null) return;
            IndexedCell cell = getCell(table, editingCell.getRow(), 0);
            int cellCount = getCellCount(table);
            if (cell != null) {
                TextField field = (TextField) cell.getGraphic();
                // unreliably: getting a field the first time around, often not the
                // second or third time
                LOG.info("has cell? "  + cell.getIndex() + cell.isEditing() + cell.getItem() 
                   + (field != null ? field.getText() : "no field" ) + " cell count: " + cellCount);
                // this doesn't work - on scrolling (with button) the field has the initial text
//                field.setText("edited");
                // can commit though
                cell.commitEdit("edited");
            }
        });
        
        FlowPane buttons = new FlowPane(10, 10); 
        buttons.getChildren().addAll(edit, addAndEdit, logEditing, scroll, scrollAndEdit, tryEditingCell);
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
