/*
 * Created on 06.08.2015
 *
 */
package de.swingempire.fx.control;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.skin.ProgressIndicatorSkin;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * javafx-9: initialize/updateProgress are package private 
 * 
 * -----------
 * Facts:
 * <li> Skin installs a child of type Text  
 * <li> its text property is set initially and on change of progress
 * <li> if progress is 1.0, the text is set to an internal static field
 *     that's initialized to hold the localized done-message
 * <li> Text has style ".percentage"     
 * <p>
 * 
 * We need to add a hook that configures the text _after_ skin did it, both
 * initially and on change. On change is handled by a listener to progress.
 * There are some caveats:
 * <li> if we install the listener (f.i. before the ,
 *   it gets notified _before_ the listener installed by the skin (implementation
 *   detail!) which then reverts the text
 * <li> if we set the initial progress to done before the skin is created, we
 *   need to guard against NPE
 * <li> config _after_ the skin is available requires to wrap into a runLater
 *   (safe enough?)    
 *      
 * @author Jeanette Winzenburg, Berlin
 */
public class ProgressIndicatorConfig extends Application {

    private ProgressIndicator indicator;
    
    public static class MyProgressIndicatorSkin extends ProgressIndicatorSkin {

        public MyProgressIndicatorSkin(ProgressIndicator control) {
            super(control);
            init();
            // register additional handlers - can't override
            // handler methods due to being package private in fx-9
            registerChangeListener(control.indeterminateProperty(), e -> init());
            registerChangeListener(control.progressProperty(), e -> progressChanged());
        }

//        @Override
        protected void init() {
            // package-private as of fx-9
//            super.initialize();
            configureDoneText();
        }

        /**
         * 
         */
        protected void configureDoneText() {
            if (shouldConfigureDoneText()) {
                Text text = getDoneText();
                if (text != null) 
                    text.setText(getCustomDoneMessage());
            }
        }
        
        /**
         * @return
         */
        protected Text getDoneText() {
            Node node = getSkinnable().lookup(".percentage");
            return node instanceof Text ? (Text)node : null;
        }

        protected boolean shouldConfigureDoneText() {
            return !getSkinnable().isIndeterminate() && 
                    getSkinnable().getProgress() >= 1 &&
                    getCustomDoneMessage() != null;
        }

        protected String getCustomDoneMessage() {
            return (String) getSkinnable().getProperties().get("doneMessage");
        }

//        @Override
        protected void progressChanged() {
            // package-private as of fx-9
//            super.updateProgress();
            configureDoneText();
        }
        
    }

    /**
     * change text under progressIndicator
     * http://stackoverflow.com/q/31814830/203657
     * @param progressIndicator
     */
    private void customDoneText(ProgressIndicator indicator) {
        // hack from 
        // http://stackoverflow.com/a/16038242/203657
//        if (indicator.getSkin() == null)
        // applyCss does _not_ force the skin!
        indicator.applyCss();
        indicator.progressProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number newValue) {
                // If progress is 100% then show Text
                if (newValue.doubleValue() >= 1) {
                    // Apply CSS so you can lookup the text
                    indicator.applyCss();
//                    Text text = (Text) indicator.lookup(".text.percentage");
                    Text text = (Text) indicator.lookup(".percentage");
                    // This text replaces "Done"
                    Platform.runLater(() -> {
                        text.setText("Foo");
                    });
                }
            }
        });
        indicator.setProgress(1);
    }
    
    /**
     * @return
     */
    private Parent getContent() {
        indicator = new ProgressIndicator(0);
        indicator.setSkin(new MyProgressIndicatorSkin(indicator));
        // install listener _before_ skin is available
        // NPE in fx-9 - didn't dig
//        customDoneText(indicator);
        Button button = new Button("End");
        button.setOnAction(e -> indicator.setProgress(1));
        VBox box = new VBox(indicator, button);
        return box;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
        // install listener after skin is available
//        customDoneText(indicator);
    }
    public static void main(String[] args) {
        launch(args);
    }

}
