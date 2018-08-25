/*
 * Created on 21.08.2018
 *
 */
package de.swingempire.fx.event;

import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import static javafx.scene.input.KeyCode.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import sun.util.logging.PlatformLogger;

/**
 * https://stackoverflow.com/q/51943654/203657
 * ctrl-A not working if dropDown showing
 * 
 * independent of filter
 * same for datepicker
 * 
 * reasons/fixes:
 * combo - listViewBehavior has mapping that consumes the A.shortCut 
 *     grab mapping from inputMap and set its auto-consume to false
 * picker - DatePickerContent consumes all keys that are not navigational in dropDown
 *     (Note: no InputMap involved!)
 *     install eventFilter to manually select text - borderline UX    
 *   
 * Also: left/right not working if dropDown showing
 * - if closed, textField gets pressed -> navigation
 * - if open, listView is focused, textField does not get pressed -> no navigation
 *   
 */
public class ComboCtrlA extends Application {
    
    boolean comboInstalled;
    @Override
    public void start(Stage stage) {
        // need to keep a reference to each ..
       Logger logger = FXUtils.getInputLogger(Level.FINE);
       Logger focus = FXUtils.getFocusLogger(Level.ALL);
        
        HBox root = new HBox();

        ComboBox<String> cb = new ComboBox<String>();
        cb.setEditable(true);

//        cb.fireEvent(event);
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");

//        FilteredList<String> filteredItems = new FilteredList<String>(items, p -> true);
        cb.setItems(items);
        
        cb.setOnShown(e -> {
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
                ListView<?> list = (ListView<?>) skin.getPopupContent();
                // could remove auto-consume from mapping
//                ListViewSkin<?> listSkin = (ListViewSkin<?>) list.getSkin();
//                ListViewBehavior<?> listBehavior = (ListViewBehavior<?>) FXUtils.invokeGetFieldValue(
//                        ListViewSkin.class, listSkin, "behavior");
//                InputMap<?> map = listBehavior.getInputMap();
//                Optional<Mapping<?>> mapping = map.lookupMapping(new KeyBinding(A).shortcut());
//                mapping.ifPresent(m -> m.setAutoConsume(false));
//                LOG.info(mapping + "");
//                list.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
//                    LOG.info("getting key: " + keyEvent);
//                    if ( keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
//                        cb.getEditor().selectAll();
//                    }
//                });
                cb.setOnShown(null);
        });

        root.getChildren().addAll(cb);

        Scene scene = new Scene(root, 300, 100);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboCtrlA.class.getName());
}

