/*
 * Created on 21.08.2018
 *
 */
package de.swingempire.fx.event;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.inputmap.KeyBinding.OptionalBoolean;

import static com.sun.javafx.scene.control.inputmap.KeyBinding.OptionalBoolean.*;
import static javafx.scene.input.KeyCode.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import test.combobox.ComboTextFieldNavigation;

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
 * reported as bug: 
 * reported: https://bugs.openjdk.java.net/browse/JDK-8209788
 * 
 * reason: focusTraversal mappings of listView grabs left/right
 * remedy: remove the mappings
 * 
 * Also: space hides popup
 * that's a onKeyPressed handler installed by comboSkin on creation of listView
 * it also hides on enter, esc
 * 
 * @see ComboTextFieldNavigation
 * 
 *   
 */
public class ComboCtrlA extends Application {
    
    public static class WComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        public WComboBoxSkin(ComboBox<T> control) {
            super(control);
            installListViewSkinListener();
        }

        /**
         * Creates a listener on the listView's skin property.
         */
        protected void installListViewSkinListener() {
            getPopupListView().skinProperty().addListener((src, ov, nv) -> {
                listViewSkinChanged(ov);
            });
        }
        
        /**
         * Callback from listener to listViewSkin. This implementation 
         * tweaks the listView's inputMap to suit its role in popup of
         * the combo.
         * 
         * @param ov the old value of the skin
         */
        protected void listViewSkinChanged(Skin<?> oldSkin) {
            @SuppressWarnings("unchecked")
            ListViewBehavior<T> listBehavior = (ListViewBehavior<T>) 
                    FXUtils.invokeGetFieldValue(ListViewSkin.class, 
                    (ListViewSkin<T>) getPopupListView().getSkin(), "behavior");
            
            // get inputmap
            InputMap<ListView<T>> listInputMap = listBehavior.getInputMap();
            // search the child inputMap that contains the vertical navigation mappings
           Optional<InputMap<ListView<T>>> verticalChild = listInputMap.getChildInputMaps().stream()
                .filter(child -> child.lookupMapping(new KeyBinding(UP)).isPresent())
                .findFirst();
           
           // remove focus bindings - shouldn't block, they are shared
           listInputMap.getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings());
           // mappings in parent map to keep
           List<KeyBinding> keepParent = List.of(new KeyBinding(PAGE_UP), new KeyBinding(PAGE_DOWN));
           Predicate<Event> block = e -> true;
           
           // block existing mapings
           listInputMap.getMappings().stream()
               .filter(mapping -> !keepParent.contains(mapping.getMappingKey()))
               .forEach(mapping -> mapping.setInterceptor(block));
           
           // block all child maps except vertical
           listInputMap.getChildInputMaps().stream()
               .filter(child -> child != verticalChild.get())
               .forEach(child -> child.setInterceptor(block));
           
           // mappings in vertical map to keep
           List<KeyBinding> keepVertical = List.of(new KeyBinding(UP), new KeyBinding(KP_UP),
                   new KeyBinding(DOWN), new KeyBinding(KP_DOWN));
           
           // block all vertical mappings except those to keep
           verticalChild.get().getMappings().stream()
               .filter(mapping -> !keepVertical.contains(mapping.getMappingKey()))
               .forEach(mapping -> mapping.setInterceptor(block));
           
           // remove onPressed handler that's set by super
           getPopupListView().setOnKeyPressed(null);
           
           // for now, simply replace with mappings .. should do better (like commit)
           EventHandler<KeyEvent> hider = e -> getSkinnable().hide();
           List<KeyBinding> hideTriggers = List.of(new KeyBinding(ENTER), new KeyBinding(ESCAPE));
           
           List<KeyMapping> hideMappings = hideTriggers.stream()
                   .map(b -> new KeyMapping(b, hider))
                   .collect(Collectors.toList());
           KeyMapping space = new KeyMapping(new KeyBinding(SPACE), e -> {
               if (!getSkinnable().isEditable()) {
                   getSkinnable().hide();
               }
           });
           hideMappings.add(space);
           hideMappings.forEach(m -> m.setAutoConsume(false));
           
           // add combo related mappings
           InputMap<ListView<T>> comboRelatedMap = new InputMap<>(getPopupListView());
           comboRelatedMap.getMappings().addAll(hideMappings);
           
        }

        @SuppressWarnings("unchecked")
        protected ListView<T> getPopupListView() {
            return (ListView<T>) getPopupContent();
        }
        
    }
    
    @Override
    public void start(Stage stage) {
        // need to keep a reference to each ..
//       Logger logger = FXUtils.getInputLogger(Level.FINE);
//       Logger focus = FXUtils.getFocusLogger(Level.ALL);
        
        HBox root = new HBox();

        ComboBox<String> cb = new ComboBox<String>() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new WComboBoxSkin<>(this);
            }
            
        };
        cb.setEditable(true);

//        cb.fireEvent(event);
        ObservableList<String> items = FXCollections.observableArrayList("One", "Two", "Three", "Four", "Five", "Six",
                "Seven", "Eight", "Nine", "Ten");

//        FilteredList<String> filteredItems = new FilteredList<String>(items, p -> true);
        cb.setItems(items);
        
//        cb.setOnShown(e -> {
//            LOG.info("" + ((Control) ((ComboBoxListViewSkin<String>) cb.getSkin()).getPopupContent()).getSkin());
////                removeTraversalMappings(cb);
//                cb.setOnShown(null);
//        });

        ListView<String> list = new ListView<>(items);
        // 
        list.setOrientation(Orientation.HORIZONTAL);
        root.getChildren().addAll(cb, list);

        Scene scene = new Scene(root, 300, 100);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param cb
     */
    protected void removeTraversalMappings(ComboBox<String> cb) {
        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
        ListView<?> list = (ListView<?>) skin.getPopupContent();
        ListViewSkin<?> listSkin = (ListViewSkin<?>) list.getSkin();
        // reflective access to behavior
        ListViewBehavior<?> listBehavior = (ListViewBehavior<?>) FXUtils.invokeGetFieldValue(
                ListViewSkin.class, listSkin, "behavior");
        InputMap<?> map = listBehavior.getInputMap();
        map.getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings());
    }

    
  //------------------ do not use    

    public final static String IS_POPUP_CONTENT = "IS_POPUP_CONTENT"; 
    
    public static class XComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {

        public XComboBoxListViewSkin(ComboBox<T> control) {
            super(control);
            ListView<T> listView = (ListView<T>) getPopupContent();
            listView.getProperties().put(IS_POPUP_CONTENT, true);
            LOG.info("setIt: " + listView.getProperties().get(IS_POPUP_CONTENT));
            listView.setSkin(new XListViewSkin<>(listView));
        }
        
    }

    /**
     * Cant cleanly inject (private field in skin, skin wires some handling)
     * So do tweak maps from skin - either ListViewSkin or combo. Latter
     * might be better because we need some actions that are special to combo
     * context. 
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class XListViewBehavior<T> extends ListViewBehavior<T> {

        /**
         * @param control
         */
        public XListViewBehavior(ListView<T> control) {
            super(control);
            Object inPopup = control.getProperties().get(IS_POPUP_CONTENT);
            if (Boolean.TRUE.equals(inPopup)) {
                LOG.info("has prop " + inPopup) ;
                getInputMap().getMappings().removeAll(FocusTraversalInputMap.getFocusTraversalMappings());
            }
        }
        
    }
    
    public static class XListViewSkin<T> extends ListViewSkin<T> {

        /**
         * @param control
         */
        public XListViewSkin(ListView<T> control) {
            super(control);
            
            ListViewBehavior<T> old = (ListViewBehavior<T>) 
                    FXUtils.invokeGetFieldValue(ListViewSkin.class, this, "behavior");
            // install default input map for the ListView control
            XListViewBehavior<T> behavior = new XListViewBehavior<>(control);
            FXUtils.invokeSetFieldValue(
                    ListViewSkin.class, this, "behavior", behavior);
            old.dispose();
            
            // init the behavior 'closures'
//            behavior.setOnFocusPreviousRow(() -> onFocusPreviousCell());
//            behavior.setOnFocusNextRow(() -> onFocusNextCell());
//            behavior.setOnMoveToFirstCell(() -> onMoveToFirstCell());
//            behavior.setOnMoveToLastCell(() -> onMoveToLastCell());
//            behavior.setOnSelectPreviousRow(() -> onSelectPreviousCell());
//            behavior.setOnSelectNextRow(() -> onSelectNextCell());
//            behavior.setOnScrollPageDown(this::onScrollPageDown);
//            behavior.setOnScrollPageUp(this::onScrollPageUp);
            

        }
        
    }
    
//------------- end of do not use
    

    // c&p from KeyBinding ... just for formatting to understand what happens
    private KeyCode code;
    private EventType<KeyEvent> eventType;
    private OptionalBoolean shift = FALSE;
    private OptionalBoolean ctrl = FALSE;
    private OptionalBoolean alt = FALSE;
    private OptionalBoolean meta = FALSE;


    public int getSpecificity(KeyEvent event) {
        int s = 0;
        if (code != null && code != event.getCode())
            return 0;
        else
            s = 1;
        if (!shift.equals(event.isShiftDown()))
            return 0;
        else if (shift != ANY)
            s++;
        if (!ctrl.equals(event.isControlDown()))
            return 0;
        else if (ctrl != ANY)
            s++;
        if (!alt.equals(event.isAltDown()))
            return 0;
        else if (alt != ANY)
            s++;
        if (!meta.equals(event.isMetaDown()))
            return 0;
        else if (meta != ANY)
            s++;
        if (eventType != null && eventType != event.getEventType())
            return 0;
        else
            s++;
        // We can now trivially accept it
        return s;
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboCtrlA.class.getName());
}

