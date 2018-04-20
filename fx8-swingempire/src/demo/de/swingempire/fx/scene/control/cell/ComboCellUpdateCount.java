/*
 * Created on 20.04.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * UpdateItem called for all cells when showing the combo popup.
 * https://stackoverflow.com/q/49930449/203657
 * 
 * due to measuring the prefWidth, can be limited by (undocumented)
 * "comboBoxRowsToMeasureWidth" entry in properties.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboCellUpdateCount extends Application {

    ComboBox<String> comboBox1;
    
    private Parent createContent() {
        initialize();
        comboBox1.getProperties().put("comboBoxRowsToMeasureWidth", 10);
        return new BorderPane(comboBox1);
    }

    public static class ExampleCell<T> extends ListCell<T> {

        static int count;
        Label myLabel;

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            count++;
            System.out.println("update " + count + item);
            if (count == 100) {
                new RuntimeException("who is calling?").printStackTrace();
                
            }
            if (empty) {
                setGraphic(null);
            } else {
                if (myLabel == null) {
                    myLabel = new Label((String) item);

                } else {
                    myLabel.setText((String) item);
                }
                setGraphic(myLabel);
            }
        }
    }

    public void initialize() {
        comboBox1 = new ComboBox<>();
        for (int i = 0; i < 10000; i++) {
            comboBox1.getItems().add("example" + i);
        }
        comboBox1.setCellFactory(param -> new ExampleCell<>());
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
            .getLogger(ComboCellUpdateCount.class.getName());

}
