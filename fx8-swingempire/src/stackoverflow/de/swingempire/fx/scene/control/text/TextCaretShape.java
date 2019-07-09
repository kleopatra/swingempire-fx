/*
 * Created on 02.07.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.Arrays;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * trying to change the shape of the textCaret.
 * https://stackoverflow.com/q/56840948/203657
 * 
 * Basically, controlled by toolkit:
 * - caretPath is in TextInputControlSkin (package-private) a path
 * - filled with pathElements from readonly text.caretShapeProperty
 * - caretShapeProperty is an alias to (private class) Text.TextAttribute.caretShapeProperty
 * - the attribute's caretShape is a binding (dependencies are position and bias), that
 *   is calculated by TextLayout
 * - (private) TextLayout is controlled by Text, got from toolkit's TextLayoutFactory
 * - concrete class is PrismTextLayout
 * 
 * the path changes on every change to pos/bias, so need to listen and change
 * after the skin updated the path: can change the x/y of moveTo/lineTo - doesn't look
 * necessarily nice but possible
 * 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextCaretShape extends Application {

    private Parent createContent() {
        TextField field = new TextField("some dummy text - not important");
        field.skinProperty().addListener((src, ov, nv) -> {
            tweakCaret(nv);
        });
        BorderPane content = new BorderPane(field);
        return content;
    }

    /**
     * @param nv
     */
    private void tweakCaret(Skin<?> nv) {
        TextInputControlSkin<TextField> skin = (TextInputControlSkin<TextField>) nv;
        Path caret = (Path) FXUtils.invokeGetFieldValue(TextInputControlSkin.class, skin, "caretPath");
        Text text = (Text) skin.getSkinnable().lookup(".text");
        text.caretShapeProperty().addListener((src, ov, np) -> {
            if (np.length == 2) {
                LOG.info("path elements: " + Arrays.asList(np) + " / " + caret);
                
                MoveTo move = null;
                LineTo line = null;
                        
                if (np[0] instanceof MoveTo) {
                    move = (MoveTo) np[0];
                }
                if (np[1] instanceof LineTo) {
                    line = (LineTo) np[1];
                    
                }
                if (line != null && move != null) {
                    double down = line.getY();
                    move.setY(down);
                    line.setX(line.getX() + 10);
                }
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextCaretShape.class.getName());

}
