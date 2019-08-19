/*
 * Created on 14.08.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/51806252/203657
 * include active elements of a cell into focus traversal
 * 
 * Not a good question but interesting.
 * 
 * There are several problems:
 * - user requirement: show/activate combo on focused
 * - must use editing mechanism because the goal is to change the value
 * - can listen to changes of focusedCell and start editing
 * - really need to roll-your-own (other than bug hacking)?
 * - core bug: need to subclass comboCell to requestFocus on the combo
 * - core bug: need to initialize focusedCell with column (bug reported, see below)
 * - core bug: can't navigate off the editing cell with tab
 * - core bug: navigation in row doesn't terminate edit
 * - user requirement - navigate on tab: task of behaviour which is internal api
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableComboCellInFocusTraversal extends Application {

    private Parent createContent() {
        TableView<Person> table = new TableView<>(Person.persons());
        // standard setup
        TableColumn<Person, String> firstName = new TableColumn<>("First Name");
        firstName.setCellValueFactory(p -> p.getValue().firstNameProperty());
        TableColumn<Person, String> lastName = new TableColumn<>("Last Name");
        lastName.setCellValueFactory(p -> p.getValue().lastNameProperty());
        table.getColumns().addAll(firstName, lastName);
        
        // set editable to see the combo
        table.setEditable(true);
        // list to show in the combo
        ObservableList<String> firstNames = FXCollections.observableArrayList(
                table.getItems().stream().map(p -> p.getFirstName()).collect(toList())
                );
        // use modified standard combo cell that requests focus on its graphic
        firstName.setCellFactory(cb -> new ComboBoxTableCell<>(firstNames) {

            @Override
            public void startEdit() {
                super.startEdit();
                if (getGraphic() != null) {
                    getGraphic().requestFocus();
                }
            }
            
        });
        
        table.getSelectionModel().setCellSelectionEnabled(true);
        // Note: due to bug https://bugs.openjdk.java.net/browse/JDK-8089652
        // users have to click once into the table to trigger the column to be !=null
        // we hack around by forcing the initial cell focus if the item is != null
        TablePosition<?,?> first = table.getFocusModel().getFocusedCell();
        if (first != null && first.getTableColumn() == null) {
            table.getFocusModel().focus(first.getRow(), table.getColumns().get(0));
        };
        table.getFocusModel().focusedCellProperty().addListener((src, ov, nv) -> {
            LOG.info("focused: " + nv);
            if (nv != null && nv.getTableColumn() == firstName) {
                table.edit(nv.getRow(), firstName);
            }
        });
        BorderPane content = new BorderPane(table);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableComboCellInFocusTraversal.class.getName());

}
