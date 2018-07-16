/*
 * Created on 16.07.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Access position of caret in textArea
 * https://stackoverflow.com/q/51344090/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
    public class TextCaretPosition extends Application  {
    
        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.setTitle("TextArea Experiment 1");
    
            TextArea textArea = new TextArea("This is all\nmy text\nin here.");
    
            ObjectProperty<Rectangle> caretShape = new SimpleObjectProperty<>();
            textArea.caretPositionProperty().addListener((src, ov, nv ) -> {
                TextInputControlSkin<TextArea> skin = (TextInputControlSkin<TextArea>) textArea.getSkin();
                if (skin != null) {
                    Rectangle2D bounds = skin.getCharacterBounds(nv.intValue());
                    caretShape.set(new Rectangle(bounds.getMinX(), bounds.getMinY(), 
                            bounds.getWidth(), bounds.getHeight()));
                }
            });
            caretShape.addListener((src, ov, r) -> {
                Skin<?> skin = textArea.getSkin();
                if (skin instanceof SkinBase) {
                    if (ov != null) {
                        ((SkinBase<?>) skin).getChildren().remove(ov);
                    } 
                    if (r != null) {
                        r.setStroke(Color.RED);
                        r.setFill(Color.TRANSPARENT);
                        r.setMouseTransparent(true);
                        ((SkinBase<?>) skin).getChildren().add(r);
                    }
                }
            });
    
            VBox vbox = new VBox(textArea);
    
            Scene scene = new Scene(vbox, 200, 100);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    
        public static void main(String[] args) {
            launch(args);
        }
    }