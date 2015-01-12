/*
 * Created on 12.01.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import java.lang.reflect.Field;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import com.sun.javafx.scene.control.skin.TextInputControlSkin;

import static com.sun.javafx.PlatformUtil.*;

/**
 * Trying to make caret visible in non-editable textArea.
 * 
 * Not entirely successful, it's not blinking and gray.
 * http://stackoverflow.com/q/27291536/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class TextAreaReadOnly extends Application {

    public TextAreaReadOnly() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        TextArea editable = new TextArea();
        editable.setText("This is all\nreadonly text\nin here.");
        
        TextArea textarea = new TextArea() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTextAreaSkin(this);
            }
            
        };
        textarea.setText("This is all\nreadonly text\nin here.");
        textarea.setEditable(false);
        Pane content = new HBox(textarea, editable);
        Scene scene = new Scene(content, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }    

    public static class MyTextAreaSkin extends TextAreaSkin {

        public MyTextAreaSkin(TextArea textInput) {
            super(textInput);
            caretVisible = new BooleanBinding() {
                { bind(textInput.focusedProperty(), textInput.anchorProperty(), textInput.caretPositionProperty(),
                        textInput.disabledProperty(), displayCaret , blinkProperty() );}
                @Override protected boolean computeValue() {
                    // RT-10682: On Windows, we show the caret during selection, but on others we hide it
                    return !blinkProperty().get() &&  displayCaret.get() && textInput.isFocused() &&
                            (isWindows() || (textInput.getCaretPosition() == textInput.getAnchor())) &&
                            !textInput.isDisabled(); 
                }
            };
            caretPath.opacityProperty().bind(new DoubleBinding() {
                { bind(caretVisible); }
                @Override protected double computeValue() {
                    return caretVisible.get() ? 1.0 : 0.0;
                }
            });

        }
        
        BooleanProperty blinkAlias;
        
        BooleanProperty blinkProperty() {
            if (blinkAlias == null) {
                Class<?> clazz = TextInputControlSkin.class;
                try {
                    Field field = clazz.getDeclaredField("blink");
                    field.setAccessible(true);
                    blinkAlias = (BooleanProperty) field.get(this);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                
            }
            return blinkAlias;
        }
        
    }
    public static void main(String[] args) {
        launch(args);
    }
}

