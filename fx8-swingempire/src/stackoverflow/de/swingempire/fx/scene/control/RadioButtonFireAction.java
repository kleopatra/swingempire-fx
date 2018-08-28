/*
 * Created on 28.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/a/52040327/203657
 * does rb fire on programmatic selection?
 * 
 * answer by OP: no 
 * - which is according to spec: it's called if the fire method is invoked,
 * by any event the concrete control might deem appropriate (click, programatically
 * invoke)
 */
public class RadioButtonFireAction extends Application {
    RadioButton rBtn;

    Button btn;

    @Override
    public void start(Stage primaryStage) {
        rBtn = new RadioButton();
        rBtn.setText("Select Me");
        rBtn.setOnAction(this::handleRBSelectedAction);
        
        RadioButton other = new RadioButton("other in group");
        ToggleGroup group = new ToggleGroup();
//        group.getToggles().addAll(rBtn, other);
        
        btn = new Button();
        btn.setText("Push Me");
        btn.setOnAction(this::handleBPushedAction);

        VBox root = new VBox(2);
        root.getChildren().addAll(rBtn, other, btn);
        
        Scene scene = new Scene(root, 150, 50);
        primaryStage.setTitle("RBExample");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleRBSelectedAction(ActionEvent event) {
        System.out.println("RB Selected directly: " + rBtn.isSelected());
        if (rBtn.isSelected()) {
        }
    }

    private void handleBPushedAction(ActionEvent event) {
        rBtn.setSelected(!rBtn.isSelected());
        System.out.println("RB toggled by button: " + rBtn.isSelected());
    }

    public static void main(String[] args) {
        launch(args);
    }
}