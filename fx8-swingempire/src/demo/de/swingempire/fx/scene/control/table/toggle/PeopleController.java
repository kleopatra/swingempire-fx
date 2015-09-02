/*
 * Created on 11.08.2015
 *
 */
package de.swingempire.fx.scene.control.table.toggle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.Callback;

import com.sun.prism.impl.Disposer;

/**
 * Have a toggleButton as cell. 
 * http://stackoverflow.com/q/31893394/203657
 * 
 * Problem: selected sticks to button, not to row.
 */
public class PeopleController {

    @FXML
    private TableView<Person> personTable;

    @FXML
    private TableColumn<Person, String> nameColumn;

    @FXML
    private TableColumn previewColumn;

    private MainApp mainApp;

    final ToggleGroup group = new ToggleGroup();

    @SuppressWarnings("unchecked")
    @FXML
    public void initialize() {
        System.out.println("on fx? " + Platform.isFxApplicationThread());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue()
                .nameProperty());
        previewColumn
                .setCellFactory(new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(
                            TableColumn<Disposer.Record, Boolean> p) {
                        ButtonCell cell = new ButtonCell(group);
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                });
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        personTable.setItems(mainApp.getPersonData());
    }

    public class ButtonCell extends TableCell<Disposer.Record, Boolean> {

        final ToggleButton cellButton = new ToggleButton("click");

        public ButtonCell(ToggleGroup group) {
            cellButton.setToggleGroup(group);
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            super.updateItem(t, empty);
            if (!empty) {
                setGraphic(cellButton);
            }
        }
    }
}