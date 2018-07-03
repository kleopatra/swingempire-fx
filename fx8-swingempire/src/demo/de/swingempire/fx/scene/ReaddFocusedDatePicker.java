/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.scene;

import java.time.LocalDate;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.skin.ComboSkinDecorator;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/48538763/203657
 * re-added combo is focused but not clickable
 * 
 * here: check DatePicker
 */
public class ReaddFocusedDatePicker extends Application {

    public static class YDatePickerSkin extends DatePickerSkin
        implements ComboSkinDecorator {

        /**
         * @param control
         */
        public YDatePickerSkin(DatePicker control) {
            super(control);
            if (control.isShowing()) {
                show();
            }
        }
        
    }
    @Override
    public void start(Stage stage) {
        VBox root = new VBox();

        DatePicker picker = new DatePicker(LocalDate.now()) {

//            @Override
//            protected Skin<?> createDefaultSkin() {
////                return super.createDefaultSkin();
//                return new YDatePickerSkin(this);
//            }
            
        };
        root.getChildren().add(picker);
        // try to startup with open popup
        picker.show();
//        DatePickerSkin skin;
        // adding listener after skin is attached has no effect
        picker.valueProperty().addListener((observable, oldValue, newValue) -> {
            // guess by sillyfly: combo gets confused if popup still open 
//            choices.hide();
            root.getChildren().clear();
            LOG.info("showing in listener? " + picker.isShowing() + "/popup " + ((ComboSkinDecorator) picker.getSkin()).getPopupControl().isShowing());
//            picker.show();
            root.getChildren().add(picker);
            // suggested in answer: working but then the choice isn't focused
            //root.requestFocus();
            // doesn't work
            //  choices.requestFocus();
        });
        Scene scene = new Scene(root, 100, 100);
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();
        LOG.info("showing? " + picker.isShowing() + "/popup " + ((ComboSkinDecorator) picker.getSkin()).getPopupControl().isShowing());

    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ReaddFocusedDatePicker.class.getName());
    
}

