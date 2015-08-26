/*
 * Created on 20.08.2015
 *
 */
package de.swingempire.fx.control;

import java.awt.Point;
import java.util.ArrayList;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Combo in secondary stage crashes/freezes app in jdk8u51.
 * http://stackoverflow.com/q/32083286/203657
 * 
 * can't reproduce in jdk8u60b18 (which is older than the 
 * release 51)
 */
public class ComboInSecondary extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        ArrayList<String> options = new ArrayList<String>();
        options.add("Apple");
        options.add("Orange");
        options.add("Pear");

        String result = Selection_Dialog.show(options, "Orange");

    }

    public static void main(String[] args) {
        launch(args);
    }    
    public static class Selection_Dialog {

        //Window Properties
        private static final String TITLE = "Selection Dialog";
        private static String DEFAULT_MESSAGE = "";
        private static int DEFAULT_WIDTH = 400;
        private static int DEFAULT_HEIGHT = 250;
        private static Point DEFAULT_POSITION = new Point();

        //Window Components
        private static Stage stage;
        private static javafx.scene.control.Label messageLabel;
        private static javafx.scene.control.Button okButton;
        private static ComboBox selector;

        //Show Window Popup
        public static String show(ArrayList<String> options, String initial) {
            return show(options, initial, DEFAULT_MESSAGE, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_POSITION);
        }
        public static String show(ArrayList<String> options, String initial, String message) {
            return show(options, initial, message, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_POSITION);
        }
        public static String show(ArrayList<String> options, String initial, String message, Point position) {
            return show(options, initial, message, DEFAULT_WIDTH, DEFAULT_HEIGHT, position);
        }
        public static String show(ArrayList<String> options, String initial, String message, int width, int height) {
            return show(options, initial, message, width, height, DEFAULT_POSITION);
        }
        public static String show(ArrayList<String> options, String initial, String message, int width, int height, Point position) {

            //Setup Properties
            stage = new Stage();
            BorderPane root = new BorderPane();
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root, width, height));
            stage.setTitle(TITLE);
            stage.setResizable(false);
            stage.setOnCloseRequest(new EventHandler() {
                @Override
                public void handle(javafx.event.Event event) {
                    event.consume();
                }
            });
            if(position == DEFAULT_POSITION){
                int x, y;
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();                
                x = (int) primaryScreenBounds.getWidth()/2;
                y = (int) primaryScreenBounds.getHeight()/2;
//                x = Screen.getMainScreen().getWidth()/2;
//                y = Screen.getMainScreen().getHeight()/2;
                x = x - width/2;
                y = y - height/2;
                position = new Point(x, y);
            }
            stage.setX(position.getX());
            stage.setY(position.getY());

            stage.setTitle(FXUtils.version());

            //Create Components
            messageLabel = new javafx.scene.control.Label(message);
            okButton = new javafx.scene.control.Button("Ok");
            selector = new ComboBox();
            selector.getItems().addAll(options);
            if(options.size() > 0){
                if(initial == null){
                    selector.getSelectionModel().select(0);
                }else if(options.contains(initial)){
                    selector.getSelectionModel().select(initial);
                }else{
                    selector.getSelectionModel().select(0);
                }

            }

            //Align Components
            root.setAlignment(messageLabel, Pos.CENTER);
            root.setAlignment(okButton, Pos.CENTER);
            messageLabel.setTranslateY(15);
            okButton.setTranslateY(-25);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setWrapText(true);

            //Add Components
            root.setBottom(okButton);
            root.setTop(messageLabel);
            root.setCenter(selector);

            //Add Listeners

            okButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    stage.close();
                }
            });

            //Show Window
            stage.showAndWait();

            return selector.getSelectionModel().getSelectedItem().toString();
        }

    }
}
