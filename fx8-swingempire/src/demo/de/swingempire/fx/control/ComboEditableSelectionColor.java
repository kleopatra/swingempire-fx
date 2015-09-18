/*
 * Created on 10.09.2015
 *
 */
package de.swingempire.fx.control;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

    public static class MyComboSkin<T> extends ComboBoxListViewSkin<T> {

        public MyComboSkin(ComboBox<T> comboBox) {
            super(comboBox);
            getSkinnable().focusedProperty().addListener((source, ov, nv) -> {
                if (!nv) {
                    setTextFromTextFieldIntoComboBoxValue();
                }
            });
        }
        
    }
    /**
     * @return
     */
    private Parent getContent() {
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "Other");
        ComboBox<String> combo = new ComboBox(items);
        ComboBox<String> editable = new ComboBox(items);
        editable.setSkin(new MyComboSkin(editable));
        
        editable.setEditable(true);
        Label label = new Label();
        label.textProperty().bind(editable.valueProperty());
        VBox pane = new VBox(10, combo, editable, label);
        return pane;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
