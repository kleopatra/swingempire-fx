/*
 * Created on 11.06.2021
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.Locale;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/67935641/203657
 * longButton with ellipses text
 * 
 * worksforme in fx16+, has described issue in fx8
 */
public class ButtonTest extends Application
{

    public static void main(String[] args)
    {
        Locale.setDefault(Locale.ENGLISH);
       Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
       Scene scene = new Scene(getBottomPanel(),600,50);
       primaryStage.setScene(scene);
       primaryStage.show();
    }

    private AnchorPane getBottomPanel()
    {
       HBox infraBox = new HBox(5);
       infraBox.setAlignment(Pos.CENTER);
       infraBox.getChildren().add(new Label("Input (manuell):"));
       infraBox.getChildren().add(new TextField());

       Button shortButton = new Button("OK");
       Button longButton = new Button("\u00dcberspringen");
       ButtonBar buttonBar = new ButtonBar();
       buttonBar.getButtons().addAll(shortButton, longButton);

       AnchorPane bottomPanel = new AnchorPane(infraBox, buttonBar);
       bottomPanel.setPadding(new Insets(5));
       AnchorPane.setLeftAnchor(infraBox, 0.0);
       AnchorPane.setRightAnchor(buttonBar, 5.0);
       return bottomPanel;
    }
}
