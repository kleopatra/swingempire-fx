/*
 * Created on 19.10.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class RestrictedTextInput extends Application {

    
    /**
     * @return
     */
    private Parent getContent() {
        int len = 20;
        TextField field = new TextField("max chars: " + len );
        // here we reject any change which exceeds the length 
        UnaryOperator<Change> rejectChange = c -> {
            // check if the change might effect the validating predicate
            if (c.isContentChange()) {
                // check if change is valid
                if (c.getControlNewText().length() > len) {
                    // invalid change
                    // sugar: show a context menu with error message
                    final ContextMenu menu = new ContextMenu();
                    menu.getItems().add(new MenuItem("This field takes\n"+len+" characters only."));
                    menu.show(c.getControl(), Side.BOTTOM, 0, 0);
                    // return null to reject the change
                    return null;
                }
            }
            // valid change: accept the change by returning it
            return c;
        };
        field.setTextFormatter(new TextFormatter(rejectChange));
        
        // here we adjust the new text 
        TextField adjust = new TextField("scrolling: " + len);
        UnaryOperator<Change> modifyChange = c -> {
            if (c.isContentChange()) {
                int newLength = c.getControlNewText().length();
                if (newLength > len) {
                    // replace the input text with the last len chars
                    String tail = c.getControlNewText().substring(newLength - len, newLength);
                    c.setText(tail);
                    // replace the range to complete text
                    // valid coordinates for range is in terms of old text
                    int oldLength = c.getControlText().length();
                    c.setRange(0, oldLength);
                }
            }
            return c;
        };
        adjust.setTextFormatter(new TextFormatter(modifyChange));
        VBox pane = new VBox(10, field, adjust);
        return pane;
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(RestrictedTextInput.class.getName());
}
