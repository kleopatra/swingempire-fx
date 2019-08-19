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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/47994158/203657
 * Click on combo cell, start edit and open dropdown on one click.
 * 
 * Show popup in startEdit seems to work (fx11)
 * answered
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableComboCellIEditOpenOnClickl extends Application {

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
        ObservableList<String> firstNames = FXCollections
                .observableArrayList(table.getItems().stream()
                        .map(p -> p.getFirstName()).collect(toList()));

        // keep approach by OP
        table.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
            TablePosition<Person, ?> focusedCellPos = table.getFocusModel()
                    .getFocusedCell();
            if (table.getEditingCell() == null) {
                table.edit(focusedCellPos.getRow(),
                        focusedCellPos.getTableColumn());
            }
        });
        // use modified standard combo cell shows its popup on startEdit
        firstName.setCellFactory(cb -> new ComboBoxTableCell<>(firstNames) {

            @Override
            public void startEdit() {
                super.startEdit();
                if (isEditing() && getGraphic() instanceof ComboBox) {
                    // needs focus for proper working of esc/enter
                    getGraphic().requestFocus();
                    ((ComboBox<?>) getGraphic()).show();
                }
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
            .getLogger(TableComboCellIEditOpenOnClickl.class.getName());

}
