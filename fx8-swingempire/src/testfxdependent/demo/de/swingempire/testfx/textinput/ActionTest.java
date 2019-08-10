/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Logger;

import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.api.FxToolkitContext;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.service.support.FiredEvents;

import static org.testfx.api.FxAssert.*;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ActionTest extends  ApplicationTest {
    
    /**
     * Does not really test anything, just to see the output.
     */
    @Test
    public void testConsumeA() {
        // sanity: focused to receive the key
        verifyThat(".text-field", NodeMatchers.isFocused());
        press(KeyCode.A);
        
    }
    
    @Override
    public void start(Stage stage) {
        stopStoringFiredEvents();
        
        Parent root = ActionApp.createContent();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
        
    }

    public static void stopStoringFiredEvents() {
        FxToolkitContext context = FxToolkit.toolkitContext();
        FiredEvents fired =(FiredEvents) FXUtils.invokeGetFieldValue(FxToolkitContext.class, context, "firedEvents");
        fired.stopStoringFiredEvents();
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ActionTest.class.getName());
}
