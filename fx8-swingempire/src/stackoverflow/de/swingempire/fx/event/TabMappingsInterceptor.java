/*
 * Created on 30.08.2018
 *
 */
package de.swingempire.fx.event;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javafx.scene.KeyboardShortcutsHandler;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.collections.FXCollections.*;
import static javafx.scene.input.KeyCode.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Focustraversal mappings are shared - globally disabled if set interceptor?
 * 
 * not even disabled on the current list...
 * global focus traversal is also managed by scene in its KeyboardShortCutHandler
 * handles Tab/shift, up, down, left right
 * 
 * global focusTraversalMappings only used by:
 * ListViewBehavior, ButtonBehavior, TitledPaneBehavior - why? not needed
 * TextInputControlBehavior has some mapping added that message the
 *    static methods of FocusTraversalInputMap (traversNext etc)
 * similar for TableViewBehaviorBase, but more of them    
 * nothing in TreeViewBehavior
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 * @see KeyboardShortcutsHandler
 */
public class TabMappingsInterceptor extends Application {

    private Parent createContent() {
        
        ListView<String> list1 = new ListView<>(observableArrayList("one", "two"));
        list1.skinProperty().addListener((src, ov, nv) -> {
            ListViewSkin<?> skin = (ListViewSkin<?>) nv;
            ListViewBehavior<?> old = (ListViewBehavior<?>) 
                    FXUtils.invokeGetFieldValue(ListViewSkin.class, skin, "behavior");
            InputMap<?> map = old.getInputMap();
            map.getChildInputMaps().clear();
            KeyCode right = TAB;
            Optional<Mapping<?>> tabMapping = map.lookupMapping(new KeyBinding(right));
            Predicate<? extends Event> interceptor = e -> true;
            tabMapping.ifPresentOrElse(
                    mapping -> {
                        LOG.info("mapping: " + mapping );
                        mapping.setInterceptor(interceptor);
                        mapping.setDisabled(true);
                    }, 
                    () -> LOG.info("none"));
            
            map.getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings());
            InputMap<TextField> input = FocusTraversalInputMap.createInputMap(new TextField());
            Optional<Mapping<?>> fieldTabMapping = map.lookupMapping(new KeyBinding(right));
            LOG.info("mappings: " + tabMapping + "\n " + fieldTabMapping);
            
            tabMapping.ifPresent(e -> {
                LOG.info("intercept: " + (interceptor == e.getInterceptor()) + interceptor.test(null));
            });
            
                

        });
        ListView<String> list2 = new ListView<>(observableArrayList("one1", "two1"));
        
        ComboBox<String> box = new ComboBox<>(list2.getItems());
        box.setEditable(true);
        HBox content = new HBox(10, list1, list2, box, new TextField("dummy"));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Logger logger = FXUtils.getInputLogger(Level.FINE);
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabMappingsInterceptor.class.getName());

}
