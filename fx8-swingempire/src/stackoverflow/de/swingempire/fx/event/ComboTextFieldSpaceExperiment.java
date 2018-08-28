/*
 * Created on 21.08.2018
 *
 */
package de.swingempire.fx.event;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Typing space in textfield hides popup
 * 
 * Digging:
 * - done in keyPressed handler in listView (registered via setOnKeyPressed)
 * - nulling is okay, but disables enter and esc as well
 * 
 * Regarding the left/right interference:
 * - idea: add all mappings of focusTraversal as child with interceptor?
 */
public class ComboTextFieldSpaceExperiment extends Application {
    
    
    public static class IComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {

        /**
         * @param control
         */
        public IComboBoxListViewSkin(ComboBox<T> control) {
            super(control);
            prepareListInputMappings();
        }

        /**
         * 
         */
        protected void prepareListInputMappings() {
            
            getSkinnable().addEventHandler(ComboBoxBase.ON_SHOWN, new EventHandler<>() {

                @Override
                public void handle(Event event) {
                    initListInputMaps();
                    getSkinnable().removeEventHandler(ComboBoxBase.ON_SHOWN, this);
                }

                
            });
            
        }
        
        /**
         * 
         * 
         * Note: this is indecent intimacy - must not tweak the behavior so heavily!
         * Nothing else to do without a decent api ...
         */
        protected void initListInputMaps() {
            ListView<?> listView = (ListView<?>) getPopupContent();
            InputMap<?> map = getListInputMap();
            // hacking around in implementation knowledge
            // 0: mac os
            // 1: other os
            // 2: vertical
            // 3: horiz
            
            InputMap<?> verticalMap = map.getChildInputMaps().get(2);
            
            InputMap<ListView<?>> ignoreFocusTraversal = new InputMap<>(listView);
            // idea for left-right: add interceptor/disable mapping
            // can't work as blocked mappings are ignored on lookup
//            ignoreFocusTraversal.setInterceptor(h -> true);
            
            List<Mapping<?>> traversalMappings = List.of(FocusTraversalInputMap.getFocusTraversalMappings());
           
            // replace traversal bindings
            // doesn't work because all mapping see the event as long as it is not consumed
            // and consuming is effectively the same as not being grabbed by traversal mappings
            List<KeyMapping> converted = 
                    traversalMappings.stream()
                .filter(m -> m instanceof KeyMapping)
                .map(k -> new KeyMapping((KeyBinding) k.getMappingKey(),  e -> {
                    
                    LOG.info("" + e);
                    // e.consume();
                }))
                .collect(Collectors.toList());    
            ignoreFocusTraversal.getMappings().addAll(converted);
            
             map.getChildInputMaps().add(ignoreFocusTraversal);   
            
        }

        /**
         * @return
         */
        protected InputMap<?> getListInputMap() {
            ListViewSkin<?> skin = (ListViewSkin<?>) ((Control) getPopupContent()).getSkin();
            // reflective access to behavior
            ListViewBehavior<?> listBehavior = (ListViewBehavior<?>) FXUtils.invokeGetFieldValue(
                    ListViewSkin.class, skin, "behavior");
            InputMap<?> map = listBehavior.getInputMap();
            return map;
        }

    }
    /**
     * @param cb
     */
    public void disableHidingBySpace(ComboBox<String> cb) {
        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
        ListView<?> list = (ListView<?>) skin.getPopupContent();
        EventHandler<? super KeyEvent> old = list.getOnKeyPressed();
        if (old !=  null) {
            list.setOnKeyPressed(key -> {
                if (key.getCode() == KeyCode.SPACE && cb.isEditable()) {
                    return;
                }
                old.handle(key);
            });
        }
    }

    @Override
    public void start(Stage stage) {
        HBox root = new HBox();

        ObservableList<String> items = FXCollections.observableArrayList(
                "One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");
        ComboBox<String> cb = new ComboBox<String>(items) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new IComboBoxListViewSkin<>(this);
            }
            
        };
        cb.setEditable(true);
        
        cb.setOnShown(e -> {
            if (!(cb.getSkin() instanceof IComboBoxListViewSkin)) {
                disableHidingBySpace(cb);
            }
            cb.setOnShown(null);
        });
        root.getChildren().addAll(cb);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboTextFieldSpaceExperiment.class.getName());
}

