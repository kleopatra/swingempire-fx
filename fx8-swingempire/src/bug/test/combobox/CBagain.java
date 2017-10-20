/*
 * Created on 18.10.2017
 *
 */
package test.combobox;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CBagain extends Application{

    ComboBox<String> cb = new ComboBox<>();

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Button b = new Button("GOOWE");

        Label choice = new Label("What type of Vehicle do you drive");
        cb.getItems().addAll("Car","Jeep","Bus","Other");

        cb.setPromptText("Select your Vehicle");
        // qucik check: select bindings log warnings if null ...
      StringBinding selected =Bindings.selectString(cb.valueProperty(), "bytes");

      choice.textProperty().bind(selected);

        cb.setOnAction(e ->{

            System.out.println("value" + cb.getValue());
            
            if ("Other".equals(cb.getValue())){
                //Both the editable and cb.setEditable still give error in the terminal
                editable(cb);
                //cb.setEditable(true);
            }

        });
        VBox layout = new VBox(10);

        layout.setPadding(new Insets(10,10,10,10));
        layout.getChildren().addAll(choice,cb,b);

        b.setOnAction(e -> {

        cb.setEditable(true);

        });

        Scene sc = new Scene(layout,200,400);

        stage.setScene(sc);

        stage.show();

    }

    private void editable(ComboBox<String> cb2) {

            cb2.setEditable(true);

    }

}

