/*
 * Created on 26.07.2019
 *
 */
package de.swingempire.fx.event;

import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * https://stackoverflow.com/q/42416738/203657
 * opening tooltip shouldn't bring owner stage to front nor focus it.
 * This solution doesn't show the tooltip if not focused
 * 
 * related 
 * https://stackoverflow.com/q/57207289/203657
 * tooltip must be showing no matter what (but not change the 
 * stacking order of windows)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PopupKeepStageFocus extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Stage secondStage = new Stage();
        secondStage.setTitle("second");
        
        Button btn = new Button("Button with Tooltip");
        btn.setTooltip(new FocussingTooltip("focussing"));
        
//        Tooltip primaryTooltip = new Tooltip("Blubb");
//        btn.setTooltip(primaryTooltip);
//        
//        primaryTooltip.setOnShown(e -> {
//            Window window = primaryTooltip.getScene().getWindow();
//            if (secondStage.isFocused())
//                secondStage.toFront();
//        });
//        
        // answer by raymond: use custom tooltip that's not showing 
        // is its owner is not focused
//        btn.setTooltip(new FixedTooltip("Blubb"));

        Button lower = new Button("with contextMenu");
        ContextMenu forButton = new ContextMenu();
        MenuItem item = new MenuItem("dummy");
        MenuItem other = new MenuItem("other");
        forButton.getItems().setAll(item, other);
        
        Shape rect = new Rectangle(200, 100);
        rect.setFill(Color.ROSYBROWN);
        Tooltip tip = new Tooltip("on rect");
        Tooltip.install(rect, tip );
        
        BorderPane primaryRoot = new BorderPane(btn);
        primaryRoot.setTop(rect);
        primaryRoot.setBottom(lower);
        Scene primaryScene = new Scene(primaryRoot, 320, 240);
        primaryStage.setScene(primaryScene);
        primaryStage.show();

        Button openContext = new Button("Open Context");
        openContext.setOnAction(e -> {
//            forButton.show(scene.getWindow());
            forButton.show(btn, 100, 100);
        });
        
        secondStage.setScene(new Scene(new BorderPane(openContext), 320, 240));
        // workaround by selim (OP): set the owner
        // drawback the owner is always on top of the owned
        // secondStage.initOwner(primaryStage);
        secondStage.show();
    }
    
    /**
     * Tooltip that tries to keep focused window on top in z-order.
     * Not entirely satisfying: introduces a slight flicker (sometimes only)
     * @author Jeanette Winzenburg, Berlin
     */
    public class FocussingTooltip extends Tooltip {

//        @Override
//        protected void show() {
//            super.show();
//            LOG.info("windows? " + Window.getWindows());
//            Optional<Window> focused = Window.getWindows().stream().filter(m -> isFocused()).findFirst();
//            if (focused.isPresent()) {
//                LOG.info("focused: " + focused.get());
//            }
//            if (focused.isPresent() && focused.get() != getScene().getWindow()) {
//                Window window = focused.get();
//                //focused.get().toFront();
//                LOG.info("window? " + window);
//            }
//        }

        public FocussingTooltip() {
            this(null);
        }

        public FocussingTooltip(String text) {
            super(text);
//            addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
            setOnShown( e -> {
                LOG.info("getting here?");
                Optional<Window> focused = Window.getWindows().stream().filter(m -> m.isFocused()).findFirst();
                if (focused.isPresent()) {
                    LOG.info("focused: " + focused.get());
                }
                if (focused.isPresent() && focused.get() != getScene().getWindow()) {
                    Window window = focused.get();
                    LOG.info("window? " + window);
                    if (window instanceof Stage) {
                        ((Stage) focused.get()).toFront();
                        
                        
                    }
                }
                
            });
        }

        /**
         * @param value
         */
        private void afterShown(WindowEvent e) {
          Optional<Window> focused = Window.getWindows().stream().filter(m -> isFocused()).findFirst();
          if (focused.isPresent()) {
              LOG.info("focused: " + focused.get());
          }
          if (focused.isPresent() && focused.get() != getScene().getWindow()) {
              Window window = focused.get();
              //focused.get().toFront();
              LOG.info("window? " + window);
          }
            
        }
        
    }
    /**
     * answer by Raymond Nagel: custom tooltip that
     * only shows when its owner window is focused.
     */
    public class FixedTooltip extends Tooltip {

        public FixedTooltip(String string) {
            super(string);
        }

        @Override
        protected void show() {        
            Window owner = getOwnerWindow();        
            if (owner.isFocused())
                super.show(); 
        }    

    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PopupKeepStageFocus.class.getName());
}