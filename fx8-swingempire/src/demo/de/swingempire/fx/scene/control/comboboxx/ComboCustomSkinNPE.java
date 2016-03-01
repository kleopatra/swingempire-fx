/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.lang.reflect.Field;

import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.css.Styleable;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;


/**
 * Custom skin throws NPE when 
 * - hiding popup (always)
 * - on mouse over or clicking arrow (is editable initially)
 * 
 * Note: eventhandlers on arrow are installed only if editable is true
 * at the time of instantiating the skin
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboCustomSkinNPE extends Application {

    /**
     * Minimal custom skin and behavior.
     */
    private static class CustomComboSkin<T> extends ComboBoxPopupControl<T> {

        private Pane content;
        private Label display;
        private ComboBoxBaseBehavior<T> behavior;
 
        public CustomComboSkin(ComboBoxBase<T> control) {
            super(control);
            // most basic behavior
            behavior = new CustomComboBehavior<>(control);
            getPopupContent().setManaged(false);
            getChildren().add(getPopupContent());
            getChildren().add(getDisplayNode());
            
            // hack around NPE on hiding:
            injectPopup();
        }
        
        /**
         * hack around NPE on hiding: completely replace the popupControl that's
         * created by super.
         * The creation code for the popup is copied from core, it's access
         * to behavior grabbing our custom behavior vs. using super's package
         * private method.
         */
        private void injectPopup() {
            PopupControl popup = new PopupControl() {
                @Override public Styleable getStyleableParent() {
                    return getControl();
                }
                {
                    setSkin(new Skin<Skinnable>() {
                        @Override public Skinnable getSkinnable() { return getControl(); }
                        @Override public Node getNode() { return getPopupContent(); }
                        @Override public void dispose() { }
                    });
                }
            };
            popup.getStyleClass().add(Properties.COMBO_BOX_STYLE_CLASS);
            popup.setConsumeAutoHidingEvents(false);
            popup.setAutoHide(true);
            popup.setAutoFix(true);
            popup.setHideOnEscape(true);
            popup.setOnAutoHide(e -> behavior.onAutoHide(popup));
            popup.addEventHandler(MouseEvent.MOUSE_CLICKED, t -> {
                // RT-18529: We listen to mouse input that is received by the popup
                // but that is not consumed, and assume that this is due to the mouse
                // clicking outside of the node, but in areas such as the
                // dropshadow.
                behavior.onAutoHide(popup);
            });
            popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, t -> {
                // Make sure the accessibility focus returns to the combo box
                // after the window closes.
                getSkinnable().notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            });

            // Fix for RT-21207
            // PENDING JW: do nothing .. needs further hacking
            InvalidationListener layoutPosListener = o -> {
//                popupNeedsReconfiguring = true;
//                reconfigurePopup();
            };
            getSkinnable().layoutXProperty().addListener(layoutPosListener);
            getSkinnable().layoutYProperty().addListener(layoutPosListener);
            getSkinnable().widthProperty().addListener(layoutPosListener);
            getSkinnable().heightProperty().addListener(layoutPosListener);

            // RT-36966 - if skinnable's scene becomes null, ensure popup is closed
            getSkinnable().sceneProperty().addListener(o -> {
                if (((ObservableValue)o).getValue() == null) {
                    hide();
                }
            });
            invokeSetPopup(popup);
        }
        
        
        private void invokeSetPopup(PopupControl popup) {
            Class<?> declaringClass = ComboBoxPopupControl.class;
            try {
                Field field = declaringClass.getDeclaredField("popup");
                field.setAccessible(true);
                field.set(this, popup);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // keeping compiler happy ..
        private ComboBoxBase<T> getControl() {
            return getSkinnable();
        }

        //---------- implement abstract methods
        @Override
        protected Node getPopupContent() {
            if (content == null) {
                content = new VBox(10);
                for (int i = 0; i <5; i++) {
                    content.getChildren().add(new Label("dummy-item " + i));
                }
                
            }
            return content;
        }

        @Override
        protected TextField getEditor() {
            return null;
        }

        @Override
        protected StringConverter<T> getConverter() {
            return null;
        }

        @Override
        public Node getDisplayNode() {
            if (display == null) {
                display = new Label("nothing real");
            }
            return display;
        }
        
    }
    
    private static class CustomComboBehavior<T> extends ComboBoxBaseBehavior<T> {

        public CustomComboBehavior(ComboBoxBase<T> comboBox) {
            super(comboBox);
        }
        
    }
    /**
     * @return
     */
    private Parent getContent() {
        ComboBox customFromStart = new ComboBox(FXCollections.observableArrayList("one", "two", "three")) {

            @Override
            protected Skin createDefaultSkin() {
                return new CustomComboSkin<>(this);
            }
            
        };
//        core.setSkin(new CustomComboSkin<>(core));
//        customFromStart.setEditable(true);
        Pane coreLane = new HBox(10, new Label("combo core with custom skin"), customFromStart);
        
        return new VBox(10, coreLane);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
