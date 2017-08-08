/*
 * Created on 28.07.2017
 *
 */
package de.swingempire.lang;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
    
/**
 * Asked in https://stackoverflow.com/questions/45369303/deep-reflection-fails-in-xxskin-classes
 * 
 * Here the error is:
 * 
 * Exception in thread "JavaFX Application Thread" java.lang.reflect.InaccessibleObjectException: 
Unable to make field private final com.sun.javafx.scene.control.behavior.BehaviorBase javafx.scene.control.skin.ButtonSkin.behavior accessible: 
module javafx.controls does not "opens javafx.scene.control.skin" to unnamed module @537fb2

 * can hack with adding param to default vm (in installed version)
 * 
 * --add-opens javafx.controls/javafx.scene.control.skin=ALL-UNNAMED
 * 
 * reason is the deliberate decision to allow default permission only for 
 * packages that had been available in java8: javafx.scene.control.skin is new and
 * thus not open by default!
 * 
 * Same probably for com.sun.javafx.scene.control.inputmap? Yes but the error is slightly
 * different:
 * 
 * Exception in thread "JavaFX Application Thread" java.lang.IllegalAccessError: 
 * class de.swingempire.lang.AccessFieldFX (in unnamed module @0x1bf6c96) cannot access 
 * class com.sun.javafx.scene.control.inputmap.InputMap (in module javafx.controls) 
 * because module javafx.controls does not export com.sun.javafx.scene.control.inputmap to unnamed module @0x1bf6c96
 * 
 * remedy is same as above: --add-opens
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class AccessFieldFX extends Application {

    private Parent getContent() {
        Button button = new Button("something to click on");
        // okay
        Object def = invokeGetFieldValue(Button.class, button, "defaultButton");

        button.setOnAction(e -> {
            ButtonSkin skin = (ButtonSkin) button.getSkin();
            // okay
            LambdaMultiplePropertyChangeListenerHandler cl = (LambdaMultiplePropertyChangeListenerHandler) invokeGetFieldValue(
                    SkinBase.class, skin, "lambdaChangeListenerHandler");
            // okay
            Object clField = invokeGetFieldValue(
                    LambdaMultiplePropertyChangeListenerHandler.class, cl,
                    "EMPTY_CONSUMER");
            // failure - needs --add-opens of control.skin package
            BehaviorBase beh = (BehaviorBase) invokeGetFieldValue(ButtonSkin.class, skin,
                    "behavior");
            InputMap map = beh.getInputMap();
            Object par = invokeGetFieldValue(InputMap.class, map,
                    "parentInputMap");
        });
        BorderPane pane = new BorderPane(button);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Object res = getClass().getResource("/com/sun/javafx/scene/control/skin/modena/modena.css");
        LOG.info(res + "");

        primaryStage.setScene(new Scene(getContent(), 600, 400));
        // primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static Object invokeGetFieldValue(Class declaringClass,
            Object target, String name) {
        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(AccessFieldFX.class.getName());
}
