/*
 * Created on 10.09.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.scene.control.comboboxx.CellUpdateItemAndSelected;
import de.swingempire.fx.util.FXUtils;

/**
 * SO ??
 * different selection color for editable vs. not-editable combo
 * can't reproduce
 * 
 * <p>
 * 
 * Behaviour change of editable combo (datepicker, didn't test the latter)
 * http://stackoverflow.com/q/32620739/203657
 * was: text committed on focus lost (can't check, no early jdk)
 * is: text not committed on focus lost
 * <p>
 * <UL> Options:
 * <li> use custom skin - drawback: access of hidden api
 * <li> trick with textFormatter - drawback: commit on navigating the dropdown
 *   (probably due to commit on setText)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboEditableSelectionColor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    /**
     * @return
     */
    private Parent getContent() {
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "Other");
        ComboBox<String> combo = new ComboBox<>(items);
        ComboBox<String> editable = new ComboBox<>(items);
        editable.setEditable(true);
        // solution via a custom skin
//        editable.setSkin(new MyComboSkin(editable));
        // solution via a textFormatter
        editable.setConverter(TextFormatter.IDENTITY_STRING_CONVERTER);
        TextFormatter<String> formatter = new TextFormatter<>(editable.getConverter());
        editable.getEditor().setTextFormatter(formatter);
//        editable.valueProperty().bindBidirectional(formatter.valueProperty());
        // visualize update
        Label label = new Label(editable.getEditor().getText());
        label.textProperty().bind(editable.valueProperty());

        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        int row = 0;

        grid.add(new Label("not-editable: "), 0, row);
        grid.add(combo, 1, row);

        row++;
        grid.add(new Label("editable value: "), 0, row);
        grid.add(label, 1, row);
        
        row++;
        grid.add(new Label("editable: "), 0, row);
        grid.add(editable, 1, row);
        
        return grid;
    }
    
    public static void main(String[] args) {
        CellUpdateItemAndSelected c;
        launch(args);
    }

}
