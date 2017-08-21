/*
 * Created on 06.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * 
 * http://stackoverflow.com/q/35279377/203657
 * add and edit can confuse the tableView
 * Solution is to _always_ make sure the target cell is visible
 * and force a complete layout!
 * 
 * To reproduce:
 * - click addAndEdit to insert cell off the visual range and start edit
 * - click scrollBar to scroll to inserted cell
 * - click addAndEdit again
 * - expected: cell with "initial 51" is edited
 * - actual: cell with "initial 50" is edited
 * - type x at beginning of editor text field, commit by enter
 * - note two cells with "xinitial 50"
 * - scroll back and forth to get rid of the visual artefact (only one is really changed)
 * - note that "initial 51" is changed to "xinitial 50" - corrupted data!
 * 
 * filed as:
 * https://bugs.openjdk.java.net/browse/JDK-8150525
 * fixed - the solution was to call updateEditing in indexChanged(int, int)
 * hacking around by reflectively calling updateEditing in updateIndex(int)
 * 
 * Also old bug:
 * https://bugs.openjdk.java.net/browse/JDK-8093922
 * 
 * resolved as can't reproduce
 * 
 * New issue (9ea-u180)
 * start editing anywhere, scroll by clicking -> editing is cancelled
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePCoreAddAndEdit extends Application {

    /**
     * Subclass to test the fix of 
     * https://bugs.openjdk.java.net/browse/JDK-8150525
     * seems fixed!
     */
    public static class FixedTextFieldTableCell extends TextFieldTableCell {

         public FixedTextFieldTableCell() {
             super(new DefaultStringConverter());
        }
        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            FXUtils.invokeMethod(TableCell.class, this, "updateEditing");
        }
        
        
    }
    
    private Parent getContent() {
        TableView<Dummy> table = new TableView<>(createData(50));
        table.setEditable(true);
        
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(TextFieldTableCell.forTableColumn());
//        column.setCellFactory(p -> new FixedTextFieldTableCell());
        column.setMinWidth(200);
        table.getColumns().addAll(column);
        
        // insert and start editing at an invisible row
        // in my environment, I see about 12 rows
        int insertIndex = 20; 
        Button addAndEdit = new Button("AddAndEdit");
        addAndEdit.setOnAction(e -> {
            Dummy dummy = new Dummy();
            table.getItems().add(insertIndex, dummy);
            table.edit(insertIndex,  column);
            LOG.info("insertIndex" + insertIndex + "isAtIndex " + table.getItems().indexOf(dummy) + dummy);
        });
        
        Button logEditing = new Button("LogEditing");
        logEditing.setOnAction(e-> {
            TablePosition<?, ?> editingCell = table.getEditingCell();
            LOG.info((editingCell != null 
                    ? "editing row: " + editingCell.getRow() + table.getItems().get(editingCell.getRow()): "no editing cell")
                    + "value at insertIndex: " + table.getItems().get(insertIndex)    
                    );
        });
        HBox buttons = new HBox(10, addAndEdit, logEditing);
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
            .getLogger(TablePCoreAddAndEdit.class.getName());
}
