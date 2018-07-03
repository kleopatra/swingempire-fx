/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.logging.Logger;

import de.swingempire.fx.scene.ReaddFocusedComboBug.YComboBoxListViewSkin;
import de.swingempire.fx.scene.control.skin.ComboSkinDecorator;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * https://stackoverflow.com/q/48538763/203657
 * re-added combo is focused but not clickable
 * 
 * Here: extract for bug report - remove/add via button
 * can't use button (will close the popup) - use key events
 * 
 * problem happens if combo is removed from parent while the 
 * popup is open: on re-adding, the combo's showing is true (while
 * popup's showing is false) trying to open the popup via mouse does
 * nothing (because the combo's showing doesn't change)
 * 
 * Fix: make sure to keep the popup's showing in sync with combo's, f.i.
 * by listening to its parent property and hide/show the popup as appropriate.
 * This is analogous to the fix of JDK-8095306, which handles the sync
 * in the skin's constructor.
 * 
 * Reported as (with the original example):
 * https://bugs.openjdk.java.net/browse/JDK-8197846
 * 
 */
public class ReaddFocusedComboBug extends Application {

    /**
     * Skin that keeps the combo's showing and the popup's showing in sync
     * on dynamic add/remove of the combo.
     * 
     */
    public static class YComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> 
        implements ComboSkinDecorator {

        /**
         * @param combo
         */
        public YComboBoxListViewSkin(ComboBox<T> combo) {
            super(combo);
            // listener to keep popup showing in sync with combo showing
            registerChangeListener(combo.sceneProperty(), e -> {
                if (combo.getScene() == null) {
                    // arguable: 
                    // on removal, the popup is hidden,
                    // force combo to hide also
                    combo.hide();
                } else if (combo.isShowing()) {
                    // ensure popup is showing 
                    show();
                }
            });
            // this listener (its fx8 equivalent) has been removed in 
            // http://hg.openjdk.java.net/openjfx/8u40/rt/file/bc4910bf1984/modules/controls/src/main/java/com/sun/javafx/scene/control/skin/ComboBoxListViewSkin.java
            // has no effect, though - probably because the showing property does not change on removal/re-addition
//            registerChangeListener(combo.showingProperty(), e -> {
//                if (combo.isShowing()) {
//                    getPopupContent().setManaged(true);
//                } else {
//                    getPopupContent().setManaged(false);
//                }
//            });
 
        }

        /**
         * Bugfix: super show assumes scene != null and throws NPE when opening.
         * Late detection (and fix) in
         * https://bugs.openjdk.java.net/browse/JDK-8196827
         */
        @Override
        public void show() {
            if (getSkinnable() == null || getSkinnable().getScene() == null) return;
            super.show();
        }
        
    }
    
    /**
     * ComboSkin with access to its popup.
     * 
     */
    public static class XComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> 
        implements ComboSkinDecorator {

        public XComboBoxListViewSkin(ComboBox<T> combo) {
            super(combo);
            // enforce creation of the popup
            getPopupControl();
        }

        
    }
    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        Scene scene = new Scene(root, 200, 200);

        final ComboBox<String> combo = new ComboBox<>() {

            @Override
            protected Skin<?> createDefaultSkin() {
//                return new XComboBoxListViewSkin<>(this);
                return new YComboBoxListViewSkin<>(this);
            }
            
        };
        combo.getItems().add("Test1");
        combo.getItems().add("Test2");
        
        SimpleObjectProperty<Window> popup = new SimpleObjectProperty<>();
        
        ChangeListener<? super Boolean> showingComboListener = (src, ov, nv) -> {
//                LOG.info("showing on combo - combo/popup showing? " + combo.isShowing() + " / " +  popup.get().isShowing());
        };
        ChangeListener<? super Boolean> showingPopupListener = (src, ov, nv) -> {
//                LOG.info("showing on popup - combo/popup showing? " + combo.isShowing() + " / " +  popup.get().isShowing());
        };
        combo.skinProperty().addListener((src, ov, nv) -> {
//            if (!(nv instanceof ComboSkinDecorator)) return;
//            ComboSkinDecorator skin = (ComboSkinDecorator) nv;
//            popup.set(skin.getPopupControl());
//            combo.showingProperty().addListener(showingComboListener);
//            popup.get().showingProperty().addListener(showingPopupListener);
//            combo.focusedProperty().addListener(e -> LOG.info("focused: " + combo.isFocused()));
//            scene.focusOwnerProperty().addListener(e -> LOG.info("focused: " + scene.getFocusOwner()));
        });
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.F1) {
                combo.show();
            }
            if (e.getCode() == KeyCode.F2) {
                root.getChildren().remove(combo);
                if (popup.get() != null) {
                    LOG.info("removed - combo/popup showing? " + combo.isShowing() + " / " +  popup.get().isShowing());
                }
                
            }
            if (e.getCode() == KeyCode.F3) {
                root.getChildren().add(combo);
                if (popup.get() != null) {
                    LOG.info("added - combo/popup showing? " + combo.isShowing() + " / " +  popup.get().isShowing());
                }
            }
            if (e.getCode() == KeyCode.F5) {
                combo.hide();
                if (popup.get() != null) {
                    LOG.info("hide - combo/popup showing? " + combo.isShowing() + " / " +  popup.get().isShowing());
                }
            }
        });
        root.getChildren().addAll(
                // need something that's focused all the time
                // all fine if the combo is focused when removing
                new Button("dummy"), 
                combo);
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ReaddFocusedComboBug.class.getName());
}

