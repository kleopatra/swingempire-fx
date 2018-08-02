/*
 * Created on 03.07.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;

import static javafx.scene.input.KeyCombination.*;
/**
 * Here variation: toggle visible instead of add/remove
 * 
 * Bug check: ComboBase with showing property set to true after removing from scene.
 * Partly reported as https://bugs.openjdk.java.net/browse/JDK-8197846 for comboBox.
 * 
 * Keys to manipulate the control 
 * F1: control.show
 * F2: control.setVisible(false)
 * F3: control.setVisible(true)
 *  
 * Steps to check state when popup is hidden on remove:
 * - run
 * - F2 to remove
 * - F1 to show
 * - F3 to add again
 * - click on button to open popup
 * 
 * Results (fx10)
 * DatePicker: popup not opened, bug
 * ComboBox: popup not opened, bug
 * ColorPicker: popup not opened, bug
 *  
 * Steps to check state when popup is showing on remove:
 * - run
 * - alt-page-down to open popup
 * - F2 to remove
 * - F3 to add again
 * - click on button to open popup
 * 
 * Results (fx10)
 * DatePicker: eventfilter not triggered, can't check - why? 
 *      Reason: datePickerContent consumes all keyEvents!
 *      editable doesn't matter
 * ComboBox: popup opened (in this context, needs to be unfocused to show misbehaviour, why?)
 *      a couple of days later: popup not opened why the change in behaviour?
 *      editable doesn't matter
 * ColorPicker: popup not opened, bug
 *      editable doesn't matter
 *  
 * 
 * Note: throws NPE on calling show, see https://bugs.openjdk.java.net/browse/JDK-8196827
 * is fixed for fx11. Here the sole purpose of the custom skin is to work around that bug.
 * Keep it inline, this example should be stand-alone!
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBaseToggleVisible extends Application {

    private ComboBoxBase<?> comboBase;
    private Pane createContent() {
//        comboBase = new DatePicker() {
//
//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new DatePickerSkin(this) {
//
//                    @Override
//                    public void show() {
//                        if (getSkinnable().getScene() == null) return;
//                        super.show();
//                    }
//                    
//                };
//            }
//            
//        };
        comboBase = new ComboBox<>() {
            @Override
            protected Skin<?> createDefaultSkin() {
                return new ComboBoxListViewSkin<>(this) {

                    @Override
                    public void show() {
                        if (getSkinnable().getScene() == null) return;
                        super.show();
                    }
                    
                };
            }
            
        };
//        comboBase = new ColorPicker() {
//            @Override
//            protected Skin<?> createDefaultSkin() {
//                return new ColorPickerSkin(this) {
//
//                    @Override
//                    public void show() {
//                        if (getSkinnable().getScene() == null) return;
//                        super.show();
//                    }
//                    
//                };
//            }
//           
//        };
//        comboBase.setEditable(true);
        return new BorderPane(comboBase);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = createContent();
        Scene scene = new Scene(root, 300, 100);
        stage.setScene(scene);
        stage.setTitle(FXUtils.version() + " visible ");
        SimpleObjectProperty<Window> popup = new SimpleObjectProperty<>();
        
        // accelerators not always triggered?
//        scene.getAccelerators().put(keyCombination("F1"), () -> comboBase.show());
//        scene.getAccelerators().put(keyCombination("F2"), () -> root.getChildren().remove(comboBase));
//        scene.getAccelerators().put(keyCombination("F3"), () -> root.getChildren().add(comboBase));
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.F1) {
                comboBase.show();
            }
            if (e.getCode() == KeyCode.F2) {
                comboBase.setVisible(false);
                if (popup.get() != null) {
                    LOG.info("removed - combo/popup showing? " + comboBase.isShowing() + " / " +  popup.get().isShowing());
                }
                
            }
            if (e.getCode() == KeyCode.F3) {
                comboBase.setVisible(true);
                if (popup.get() != null) {
                    LOG.info("added - combo/popup showing? " + comboBase.isShowing() + " / " +  popup.get().isShowing());
                }
            }
            if (e.getCode() == KeyCode.F5) {
                comboBase.hide();
                if (popup.get() != null) {
                    LOG.info("hide - combo/popup showing? " + comboBase.isShowing() + " / " +  popup.get().isShowing());
                }
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBaseToggleVisible.class.getName());

}
