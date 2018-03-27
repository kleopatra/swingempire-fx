/*
 * Created on 12.01.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import static com.sun.javafx.PlatformUtil.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

/**
 * Trying to make caret visible in non-editable textArea.
 * http://stackoverflow.com/q/27291536/203657
 * 
 * fx-9: properties in textAreaSkin not visible, hacked around via reflection.
 * 
 * Working as expected.
 * -------
 * fx-8
 * Not entirely successful, it's not blinking and gray.
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
            
            replaceCaretVisible();
            ObservableBooleanValue caretVisible = superCaretVisibleProperty();
            Path caretPath = superPath();
            caretPath.opacityProperty().bind(new DoubleBinding() {
                { bind(caretVisible); }
                @Override protected double computeValue() {
                    return caretVisible.get() ? 1.0 : 0.0;
                }
            });

        }

        private void replaceCaretVisible() {
            TextArea textInput = getSkinnable();
            BooleanProperty displayCaret = superDisplayCaretProperty();
            ObservableBooleanValue caretVisible = new BooleanBinding() {
                { bind(textInput.focusedProperty(), textInput.anchorProperty(), textInput.caretPositionProperty(),
                        textInput.disabledProperty(), displayCaret , superBlinkProperty() );}
                @Override protected boolean computeValue() {
                    // RT-10682: On Windows, we show the caret during selection, but on others we hide it
                    return !superBlinkProperty().get() &&  displayCaret.get() && textInput.isFocused() &&
                            (isWindows() || (textInput.getCaretPosition() == textInput.getAnchor())) &&
                            !textInput.isDisabled(); 
                }
            };
            
            FXUtils.invokeSetFieldValue(TextInputControlSkin.class, this, "caretVisible", caretVisible);
        }
        
        
        private ObservableBooleanValue superCaretVisibleProperty() {
            return (ObservableBooleanValue) FXUtils.invokeGetMethodValue(TextInputControlSkin.class, this, "caretVisibleProperty");
           
        }
        private BooleanProperty superDisplayCaretProperty() {
            return (BooleanProperty) FXUtils.invokeGetMethodValue(
                    TextInputControlSkin.class, this, "displayCaretProperty");
        }

        private BooleanProperty superBlinkProperty() {
            return (BooleanProperty) FXUtils.invokeGetMethodValue(TextInputControlSkin.class, this, "blinkProperty");
        }

        private Path superPath() {
            return (Path) FXUtils.invokeGetFieldValue(TextInputControlSkin.class, this, "caretPath");
        }

    }
    public static void main(String[] args) {
        launch(args);
    }
}

