/*
 * Created on 14.08.2018
 *
 */
package control.cell;

import java.util.logging.Logger;

import static java.util.stream.Collectors.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * trigger was:
 * https://stackoverflow.com/q/51806252/203657
 * include active elements of a cell into focus traversal
 * 
 * - core bug: can't navigate off the editing cell with tab
 * - core bug: navigation in row doesn't terminate edit
 * - user requirement - navigate on tab: task of behaviour which is internal api
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableComboCellNavigation extends Application {

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
        firstName.setCellFactory(ComboBoxTableCell.forTableColumn(firstNames));
        table.getFocusModel().focusedCellProperty().addListener((src, ov, nv) -> {
            LOG.info("focused: " + "\n ov" + ov + "\n nv" + nv);
        });
        
        lastName.setCellFactory(TextFieldTableCell.forTableColumn());
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
            .getLogger(TableComboCellNavigation.class.getName());

}
