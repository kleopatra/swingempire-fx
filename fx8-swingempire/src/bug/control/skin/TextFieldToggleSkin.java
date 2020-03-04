/*
 * Created on 04.03.2020
 *
 */
package control.skin;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8240506
 * switching skin dynamically throws NPE
 */
public class TextFieldToggleSkin extends Application {

    public static void main(String[] args) {
      launch(args);
    }

    private Scene scene;

    @Override
    public void start(Stage primaryStage) {
      String javaVersion = System.getProperty("java.version");
      String javafxVersion = System.getProperty("javafx.version");
      Label label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
      TextField textField = new TextField();
      CheckBox checkBox = new CheckBox("Hello JavaFx");
      Button button = new Button("Switch to JMetro");
      button.setOnAction(event -> {
        this.switchToJMetro(textField);
      });

      VBox vBox = new VBox();
      vBox.setPadding(new Insets(10.0));
      vBox.setSpacing(5.0);
      vBox.getChildren().addAll(label, textField, checkBox, button);
      this.scene = new Scene(vBox, 800, 600);
      //this.switchToJMetro();
      primaryStage.setScene(scene);
      primaryStage.setTitle("JMetro Test");
      primaryStage.show();
    }
    
    public static class MyTextFieldSkin extends TextFieldSkin {

        public MyTextFieldSkin(TextField control) {
            super(control);
        }
        
    }

    private void switchToJMetro(TextField textField) {
      System.out.println("Switching to JMetro");
      textField.setSkin(new MyTextFieldSkin(textField));
//      JMetro jMetro = new JMetro(Style.LIGHT);
//      jMetro.setScene(this.scene);
    }
  } 