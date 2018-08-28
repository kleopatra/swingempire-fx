/*
 * Created on 21.08.2018
 *
 */
package de.swingempire.fx.event;

import java.time.LocalDate;
import java.util.Optional;
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

/**
 * https://stackoverflow.com/q/51943654/203657
 * ctrl-A not working if dropDown showing
 * 
 * independent of filter
 * same for datepicker
 * 
 * reasons/fixes:
 * combo - listViewBehavior has mapping that consumes the A.shortCut 
 *     fx9: grab mapping from inputMap and set its auto-consume to false
 *     bug fx9: no navigation in textField if dropDown showing
 *     all: install eventHandler on listView and manually selectAll
 * picker - DatePickerContent consumes all keys that are not navigational in dropDown
 *     (Note: no InputMap involved!)
 *     install eventFilter to manually select text - borderline UX    
 *   
 *   
 */
public class FilterComboCtrlA extends Application {
    
    boolean comboInstalled;
    private boolean pickerInstalled;
    @Override
    public void start(Stage stage) {
        HBox root = new HBox();

        ComboBox<String> cb = new ComboBox<String>();
        cb.setEditable(true);

        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");

//        FilteredList<String> filteredItems = new FilteredList<String>(items, p -> true);
        cb.setItems(items);
        
        cb.setOnShown(e -> {
            if (!comboInstalled) {
                comboInstalled = true;
                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
                ListView<?> list = (ListView<?>) skin.getPopupContent();
                ListViewSkin<?> listSkin = (ListViewSkin<?>) list.getSkin();
                ListViewBehavior<?> listBehavior = (ListViewBehavior<?>) FXUtils.invokeGetFieldValue(
                        ListViewSkin.class, listSkin, "behavior");
                InputMap<?> map = listBehavior.getInputMap();
                Optional<Mapping<?>> mapping = map.lookupMapping(new KeyBinding(A).shortcut());
                mapping.ifPresent(m -> m.setAutoConsume(false));
                LOG.info(mapping + "");
//                list.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
//                    LOG.info("getting key: " + keyEvent);
//                    if ( keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
//                        cb.getEditor().selectAll();
//                    }
//                });
            }
            
        });
        DatePicker picker = new DatePicker(LocalDate.now());
        picker.setOnShown(e -> {
            if (!pickerInstalled) {
                pickerInstalled = true;
                DatePickerSkin skin = (DatePickerSkin) picker.getSkin();
                Node content = skin.getPopupContent();
                content.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
                    if ( keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
                        LOG.info("getting key: " + keyEvent);
                        picker.getEditor().selectAll();
                    }
                });
            }
        });

        root.getChildren().addAll(cb, picker);

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
//        ((ComboBoxListViewSkin)cb.getSkin()).getDisplayNode()
        scene.addEventFilter( KeyEvent.KEY_PRESSED, keyEvent -> {
            LOG.info("getting key: " + keyEvent);
            if ( keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A ) {
                cb.getEditor().selectAll();
            }
        });

    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FilterComboCtrlA.class.getName());
}
