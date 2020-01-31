/*
 * Created on 31.01.2020
 *
 */
package de.swingempire.fx.event;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * https://stackoverflow.com/q/59973807/203657
 * playing with mouse transparent/pickbounds
 */
public class MouseTransparentController implements Initializable {
    
    @FXML 
    ScrollPane scrollPane;
    
    public void onMouseInteraction(MouseEvent mouseEvent) {
        System.out.println(mouseEvent);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        VBox box =  new VBox(new Button("one"), new TextField("other"));
        box.setMouseTransparent(true);
        box.setPickOnBounds(false);
        scrollPane.setContent(box);
        scrollPane.skinProperty().addListener(ov -> {
            
            Parent viewRect = (Parent) scrollPane.getChildrenUnmodifiable().get(0);
            // go up from content
            Parent content = null; //scrollPane.getContent();
            // if none, lookup viewport
            content = (Pane) scrollPane.lookup(".viewport");
            System.out.println("rect = port? " + viewRect.equals(content));
            
            // now down into its child
            content = (Pane) content.getChildrenUnmodifiable().get(0);
            
            System.out.println("child is content?" + box.equals(content.getChildrenUnmodifiable().get(0)));
            
            System.out.println("content: " + content);
            while (content != null) {
                content.setPickOnBounds(false);
                content.setMouseTransparent(true);
                if (content instanceof ScrollPane) break;
                System.out.println(content.getParent().getClass());
                content = content.getParent();
            }
        }); 
    }
}