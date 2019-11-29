/*
 * Created on 04.12.2018
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

/**
 * strange resize of label/textfield when toggling focus from field to scrollPane.
 * layout issue, where?
 * https://stackoverflow.com/q/53603250/203657
 */
public class ScrollScaleOnFocusChange extends Application
{

    @Override
    public void start(Stage primaryStage)
    {
        Stage stage = primaryStage;

        HBox myHBox = new HBox();

        Label smallerText = new Label("this is small\ntext");
        smallerText.setStyle("-fx-font-weight: bold");
        smallerText.setTextFill(Color.web("#000000"));
        smallerText.setFont(Font.font("Leelawadee UI", FontPosture.REGULAR, 12));

        Label biggerText = new Label("this is big\ntext");
        biggerText.setStyle("-fx-font-weight: bold");
        biggerText.setTextFill(Color.web("#005FBA"));
        biggerText.setFont(Font.font("Leelawadee UI", FontPosture.REGULAR, 24));

        TextField myTextField = new TextField();

        myHBox.getChildren().addAll(smallerText, biggerText);

        VBox myVBox = new VBox();
        myVBox.getChildren().addAll(myHBox, myTextField);

        ScrollPane myScrollPane = new ScrollPane(myVBox);

        myScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        myScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);

        Scene scene = new Scene(myScrollPane, 300, 300);
        
        biggerText.widthProperty().addListener((src, ov, nv) -> {
            LOG.info("width bigger: " + nv);
        });
        scene.focusOwnerProperty().addListener(ov -> {
            LOG.info("bigger pref " + biggerText.prefWidth(-1) + " / " + biggerText.getWidth());
            LOG.info("" + myScrollPane.getWidth());
        });
        stage.setScene(scene);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollScaleOnFocusChange.class.getName());
}

