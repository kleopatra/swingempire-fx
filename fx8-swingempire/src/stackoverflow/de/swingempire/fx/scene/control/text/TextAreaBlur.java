/*
 * Created on 19.11.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58932448/203657
 * broken text layout in TextArea 
 * 
 * worksforme in fx11 (not in fx8) so seems to be a fixed bug
 * 
 */
public class TextAreaBlur extends Application {
    private static final BackgroundFill blackBGF = new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY);
    private static final BackgroundFill whiteBGF = new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY);
    private static double textareaXY = 50; 
    private TextArea textarea = new TextArea();
    private int clickNo = 1;
    
    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root,400,400);
        scene.getStylesheets().add(getClass().getResource("textareablur.css").toExternalForm());
        primaryStage.setScene(scene);

        VBox vb = new VBox();
        root.setCenter(vb);
        Button b = new Button("ClickMe");
        b.addEventHandler(ActionEvent.ACTION, this::OnClickButton);
        vb.getChildren().add(b);
        vb.getChildren().add(textarea);

        textarea.setEditable(false);
        textarea.getStyleClass().add("text-area-centered");
        textarea.setBackground(new Background(blackBGF));
        textarea.setMinHeight(textareaXY);
        textarea.setMaxHeight(textareaXY);
        textarea.setMinWidth(textareaXY);
        textarea.setMaxWidth(textareaXY);
        textarea.setFont(new Font("Courier New",10));

        textarea.setText("1 2 3\n4 5 6\n7 8 9");

        primaryStage.show();
    }

    private void OnClickButton(ActionEvent event)
    {
        if(clickNo == 1)
        {
            textarea.setText("7");
            textarea.setFont(new Font("Courier New Bold",24));
        }
        else if(clickNo == 2)
        {
            Region region = ( Region ) textarea.lookup( ".content" );
            region.setBackground(new Background(blackBGF));
            textarea.setStyle("-fx-text-inner-color: white;");
        }
        else if(clickNo == 3)
        {
            Region region = ( Region ) textarea.lookup( ".content" );
            region.setBackground(new Background(whiteBGF));
            textarea.setStyle("-fx-text-inner-color: black;");
        }
        else if(clickNo == 4)
        {
            textarea.setText("1 2 3\n4 5 6\n7 8 9");
            textarea.setFont(new Font("Courier New",10));
        }
        clickNo++;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

