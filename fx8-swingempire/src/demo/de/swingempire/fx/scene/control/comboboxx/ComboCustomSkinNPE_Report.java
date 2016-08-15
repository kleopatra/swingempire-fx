/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxPopupControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;


/**
 * Custom skin throws NPE when 
 * - hiding popup (always)
 * - on mouse over or clicking arrow (is editable initially)
 * 
 * Note: eventhandlers on arrow are installed only if editable is true
 * at the time of instantiating the skin
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8150951
 * deferred to java10 .. reasoning is that behavior is not public, all fixes
 * would be hacks as long as behaviours aren't public - the THINGY-TO-DO
 * would be to make it public, so wait ..
 * 
 * Implication: ComboBoxPopupControl is useless for extension, need to 
 * replicate all code in behaviour.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboCustomSkinNPE_Report extends Application {

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
//            behavior = new CustomComboBehavior<>(control);
            getPopupContent().setManaged(false);
            getChildren().add(getPopupContent());
            getChildren().add(getDisplayNode());
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

    private Parent getContent() {
        ComboBox customFromStart = new ComboBox(FXCollections.observableArrayList("one", "two", "three")) {

            @Override
            protected Skin createDefaultSkin() {
                return new CustomComboSkin<>(this);
            }
            
        };
        // if editable _before_ installing skin, throws NPE on moving mouse over arrow
        // customFromStart.setEditable(true);
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
