/*
 * Created on 18.10.2017
 *
 */
package test.combobox;

import com.sun.javafx.scene.control.FakeFocusTextField;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/46806113/203657
 * combo throws NPE when setting editiable in its
 * action handler. 
 * 
 * Same focus problem as in fx8: select marker -> change editable 
 * -> textField shown but not focused
 * 
 * Bug in fx8, fixed in fx9
 */
public class ComboBoxActionEditable extends Application{

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
//        cb.valueProperty().addListener((src, ov, nv) -> {
//            // fx8 bug: internals blow up when changing editable while showing
////            if (cb.isShowing() || cb.isEditable()) return;
//            if ("Other".equals(nv)) {
//                cb.setEditable(true);
////                cb.requestFocus();
//                TextField textField = cb.getEditor();
//                textField.requestFocus();
////                textField.setFakeFocus(true);
//            }
//        });

        cb.setOnAction(e ->{

            if (cb.getValue().equals("Other")){
//            if ("Other".equals(cb.getValue())){
                //Both the editable and cb.setEditable still give error in the terminal
                cb.setEditable(true);
              FakeFocusTextField textField = (FakeFocusTextField) cb.getEditor();
              textField.requestFocus();
              textField.setFakeFocus(true);
//                cb.getEditor().requestFocus();
            }

        });
        VBox layout = new VBox(10);

        layout.setPadding(new Insets(10,10,10,10));
        layout.getChildren().addAll(choice,cb,b);

        b.setOnAction(e -> {
//            cb.setValue("Other");
            cb.setEditable(true);
            cb.getEditor().requestFocus();

        });

        Scene sc = new Scene(layout,200,400);

        stage.setScene(sc);
        stage.setTitle(FXUtils.version());
        stage.show();

    }

    private void editable(ComboBox<String> cb2) {

            cb2.setEditable(true);

    }

}