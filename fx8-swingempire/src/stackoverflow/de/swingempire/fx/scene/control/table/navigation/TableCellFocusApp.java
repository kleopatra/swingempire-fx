/*
 * Created on 15.08.2018
 *
 */
package de.swingempire.fx.scene.control.table.navigation;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.table.navigation.TableCellFocusController.TestModel;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * https://stackoverflow.com/q/51806252/203657
 * navigate to cell and focus (aka: edit) the control that represents the 
 * editing actor
 * 
 * Here: make the example minimal and use core support where possible
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellFocusApp extends Application {

    private Parent createContent() {
        
        ObservableList<TestModel> olTestModel = FXCollections
                .observableArrayList(testmodel -> new Observable[] {
                        testmodel.textFieldProperty(),
                        testmodel.comboBoxFieldProperty() });
        
        TableView<TestModel> table = new TableView<>();

        olTestModel.add(new TestModel("test row 1", "M"));
        olTestModel.add(new TestModel("test row 2", "F"));
        olTestModel.add(new TestModel("test row 3", "F"));
        olTestModel.add(new TestModel("test row 4", "M"));
        olTestModel.add(new TestModel("test row 5", "F"));

        TableColumn<TestModel, String> colTextField = new TableColumn<>("text col");
        colTextField
                .setCellValueFactory(cb -> cb.getValue().textFieldProperty());

        TableColumn<TestModel, String> gender= new TableColumn<>("Gender");
        gender.setMinWidth(100);
        gender.setCellValueFactory(cb -> cb.getValue().comboBoxFieldProperty());
        StringConverter<String> converter = new StringConverter<>() {

            @Override
            public String toString(String object) {
                return "F".equals(object) ? "Female" : "Male";
            }

            @Override
            public String fromString(String string) {
                return "Female".equals(string) ? "F" : "M";
            }
            
        };
        gender.setCellFactory(cb -> new ComboBoxTableCell<>(converter, "F", "M") {

            @Override
            public void startEdit() {
                super.startEdit();
                if (getGraphic() != null) {
                    getGraphic().requestFocus();
                }
            }
            
        });

        // just to see that the data is updated correctly - add a readonly column
        TableColumn<TestModel, String> plainGender = new TableColumn<>("readonly");
        plainGender.setCellValueFactory(cb -> cb.getValue().comboBoxFieldProperty());
        plainGender.setEditable(false);
        
        table.getFocusModel().focusedCellProperty().addListener((src, ov, nv) -> {
            if (nv != null && nv.getTableColumn() == gender) {
                table.edit(nv.getRow(), gender);
            }
        });

        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(true);
        
        table.getColumns().addAll(colTextField,gender, plainGender ); //, colComboBoxField );
        table.setItems(olTestModel);
       
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
            .getLogger(TableCellFocusApp.class.getName());

}
