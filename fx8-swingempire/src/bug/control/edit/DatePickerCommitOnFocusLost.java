/*
 * Created on 28.11.2017
 *
 */
package control.edit;

import java.time.LocalDate;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Regression: DatePicker's edited value must be committed on focusLost.
 * <li>Had been fixed in https://bugs.openjdk.java.net/browse/JDK-8136838
 * <li>had been broken in https://bugs.openjdk.java.net/browse/JDK-8150946
 * <p>
 * Reported as https://bugs.openjdk.java.net/browse/JDK-8191995
 * (without this example, could use old code)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DatePickerCommitOnFocusLost extends Application {

    /**
     * DatePicker's edited value is not committed on focusLost.
     */
    private void addDatePicker(GridPane grid, int row) {
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setEditable(true);
        Label valueLabel = new Label();
        valueLabel.textProperty().bind(datePicker.valueProperty().asString());
        addRow(grid, row, "DatePicker: ", datePicker, valueLabel);
    }

    /**
     * For comparison: ComboBox' edited value is committed on focusLost.
     */
    private void addComboBox(GridPane grid, int row) {
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "All");
        ComboBox<String> comboBox = new ComboBox<>(items);
        comboBox.setEditable(true);
        Label valueLabel = new Label(); 
        valueLabel.textProperty().bind(comboBox.valueProperty().asString()); 
        addRow(grid, row, "ComboBox: ", comboBox, valueLabel);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 300));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    private void addRow(GridPane grid, int row, String labelText, Node control,
            Node valueLabel) {
        int col = 0;
        grid.add(new Label(labelText), col++, row);
        grid.add(control, col++, row);
        grid.add(new Label("value: "), col++, row);
        grid.add(valueLabel, col++, row);
    
    }

    /**
     * @return
     */
    private Parent getContent() {
    
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
    
        int row = 0;
        addDatePicker(grid, row++);
        addComboBox(grid, row++);
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
