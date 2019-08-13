/*
 * Created on 09.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.api.FxToolkitContext;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.service.support.FiredEvents;

import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.stage.WindowEventDispatcher;

import static org.testfx.api.FxAssert.*;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.testfx.util.TestFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
    public void start(Stage stage) throws Exception {
        TestFXUtils.stopStoringFiredEvents(stage);
        Parent root = ActionApp.createContent();
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.show();
        
    }
    
    
//    public static void stopStoringFiredEvents(Stage stage) {
//        // remove the event-logging filter
//        FxToolkitContext context = FxToolkit.toolkitContext();
//        FiredEvents fired =(FiredEvents) FXUtils.invokeGetFieldValue(FxToolkitContext.class, context, "firedEvents");
//        fired.stopStoringFiredEvents();
//        // really cleanup: 
//        // removing the filter only nulls the eventHandler in CompositeEventHandler
//        // but does not remove the Composite from EventHandlerManager.handlerMap
//        // as a result, handlerManager.dispatchCapturingEvent runs into the fixForSource
//        // block which copies the event even though there is no filter
//        WindowEventDispatcher windowDispatcher = (WindowEventDispatcher) stage.getEventDispatcher();
//        EventHandlerManager manager = windowDispatcher.getEventHandlerManager();
//        EventHandler<? super Event> handler = manager.getEventHandler(EventType.ROOT);
//        Map handlerMap = (Map) FXUtils.invokeGetFieldValue(EventHandlerManager.class, manager, "eventHandlerMap");
//        handlerMap.clear();
//        
//    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ActionTest.class.getName());
}