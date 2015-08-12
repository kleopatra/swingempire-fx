/*
 * Created on 11.08.2015
 *
 */
package de.swingempire.fx.scene.control.table.toggle;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import com.sun.prism.impl.Disposer;

import de.swingempire.fx.scene.control.table.toggle.ToggleButtonTableExample.ButtonCellX;
import de.swingempire.fx.scene.control.table.toggle.ToggleButtonTableExample.DataSelectionModel;

/**
 * Have a toggleButton as cell. 
 * http://stackoverflow.com/q/31893394/203657
 * 
 * Problem: selected sticks to button, not to row.
 */
public class PeopleControllerX {

    @FXML
    private TableView<Person> personTable;

    @FXML
    private TableColumn<Person, String> nameColumn;

    @FXML
    private TableColumn previewColumn;

    private MainAppX mainApp;

    @SuppressWarnings("unchecked")
    @FXML
    public void initialize() {
        SingleSelectionModel<Disposer.Record> model = new DataSelectionModel(personTable.itemsProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue()
                .nameProperty());
        previewColumn
                .setCellFactory(new Callback<TableColumn<Disposer.Record, Boolean>, TableCell<Disposer.Record, Boolean>>() {

                    @Override
                    public TableCell<Disposer.Record, Boolean> call(
                            TableColumn<Disposer.Record, Boolean> p) {
                        ButtonCellX cell = new ButtonCellX(model);
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                });
    
        Callback<CellDataFeatures, ObservableValue<Boolean>> callback = f -> {
            Object value = f.getValue();
            return Bindings.equal(value, model.selectedItemProperty());
        };
        previewColumn.setCellValueFactory(callback);
    }

    
    public void setMainApp(MainAppX mainApp) {
        this.mainApp = mainApp;
//        personTable.getItems().setAll(mainApp.getPersonData());
        personTable.setItems(mainApp.getPersonData());
    }

}