/*
 * Created on 14.09.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ComboWithConverter extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        int state = 0;

//        ObservableList<String> options = FXCollections.observableArrayList(
//                "Active",
//                "Blocked"
//                );
        ObservableList<Integer> options = FXCollections.observableArrayList(
            0,
            1
        );
        
        ComboBox comboBox = new ComboBox(options);
        StringConverter<Integer> converter = new StringConverter<Integer>() {

            @Override
            public String toString(Integer object) {
                if (object == null) return "";
                return object.intValue() == 0 ? "Blocked" : "Active";
            }

            @Override
            public Integer fromString(String string) {
                return null;
            }
            
        };
        comboBox.setConverter(converter);
        BorderPane bp = new BorderPane(comboBox);
        bp.setPrefSize(200, 200);
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}