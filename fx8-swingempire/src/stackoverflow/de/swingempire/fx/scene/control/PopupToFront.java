/*
 * Created on 14.01.2019
 *
 */
package de.swingempire.fx.scene.control;

import com.sun.javafx.tk.TKStage;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * https://stackoverflow.com/q/54180344/203657
 * popup to front on requestfocus
 * 
 * Solution: go dirty - implement the toFront just the same way as in Stage,
 * need to access internal class (TKStage) with reflection.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PopupToFront extends Application{

    public static void main(String... arguments){

        launch(arguments);
    }

    public void applyTo(Pane node, Popup parent){

        final double[] dragDelta = new double[2];

        node.setOnMousePressed(e -> {
            dragDelta[0] = parent.getX() - e.getScreenX();
            dragDelta[1] = parent.getY() - e.getScreenY();
            //code to bring parent Popup to front
            toFront(parent);
        });

        node.setOnMouseDragged(e -> {
            parent.setX(e.getScreenX() + dragDelta[0]);
            parent.setY(e.getScreenY() + dragDelta[1]);
        });
    }

    protected void toFront(Popup popup) {
        TKStage peer = (TKStage) FXUtils.invokeGetMethodValue(Window.class, popup, "getPeer");
        if (peer != null) {
            peer.toFront();
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception{

        Button b1 = new Button("Open p1");
        Button b2 = new Button("Open p2");

        HBox n1 = new HBox(new Label("This is p1"));
        HBox n2 = new HBox(new Label("This is p2"));
        n1.setMinSize(200, 120);
        n2.setMinSize(200, 120);
        n1.setStyle("-fx-background-color: blue; -fx-background-radius: 4px;");
        n2.setStyle("-fx-background-color: red; -fx-background-radius: 4px;");
        n1.setAlignment(Pos.CENTER);
        n2.setAlignment(Pos.CENTER);

        Popup p1 = new Popup();
        Popup p2 = new Popup();
        p1.getContent().add(n1);
        p2.getContent().add(n2);

        applyTo(n1, p1);
        applyTo(n2, p2);

        b1.setOnAction(event -> {
            if(!p1.isShowing()) p1.show(primaryStage);
            else p1.hide();
        });
        b2.setOnAction(event -> {
            if(!p2.isShowing()) p2.show(primaryStage);
            else p2.hide();
        });

        HBox root = new HBox(10, b1, b2);
        root.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(root, 500, 200));
        primaryStage.show();
    }

}
