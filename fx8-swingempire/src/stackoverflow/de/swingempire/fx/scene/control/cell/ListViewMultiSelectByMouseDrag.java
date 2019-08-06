/*
 * Created on 06.08.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.behavior.ScrollBarBehavior;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ScrollBarSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Support extend selection by mouse drag.
 * https://stackoverflow.com/q/57366020/203657
 * 
 * Not simple? 
 * - on ListView/Skin level: no access to cell-under-mouse
 * - on cell/skin level: drags are delivered to cell where it started
 * - drags are delivered to higher up parents as well
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewMultiSelectByMouseDrag extends Application {

    public static class DragSelectSupport<T> {
        
        private ChangeListener controlSkinListener;
        private Control control;
        private ObjectProperty<MultipleSelectionModel<T>> selectionProperty;
        private ScrollBar vbar;
        private ChangeListener vbarSkinListener;
        private ChangeListener valueListener;
        private ScrollBarBehavior scrollBehavior;
        private VirtualFlow<?> flow;
        private int lastSelected;
        private boolean dragSelect;
        private boolean autoScroll;
        
        public DragSelectSupport(Control control, ObjectProperty<MultipleSelectionModel<T>> s) {
            
            this.control = control;
            this.selectionProperty = s;
            // init
            controlSkinListener = (src, ov, nv) -> grabSkinProperties();
            control.skinProperty().addListener(controlSkinListener);
            vbarSkinListener = (src, ov, nv) -> grabVBarSkinProperties();
            valueListener = ( src, ov, nv) -> scrollValueChanged();
            installListeners();
        }

        /**
         * @param control
         */
        protected void installListeners() {
            // select-by-drag handler
            control.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                dragSelect = true;
                PickResult pick = e.getPickResult();
                Node node = pick.getIntersectedNode();
                if (node instanceof ListCell) {
                    IndexedCell cell = (IndexedCell) node;
                    int index = cell.getIndex();
                    if (index != -1) {
                        lastSelected = index;
                        getSelectionModel().select(index);
                    }
                    int last = flow.getLastVisibleCell().getIndex();
                    if (index == last -1) {
                        startAutoSelect();
                    }
                } else {
                    startAutoSelect();
                }
                
            });
            
            control.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
                stopAutoSelect();
            });
        }

        protected void stopAutoSelect() {
            autoScroll = false;
            dragSelect = false;
            lastSelected = -1;
            scrollBehavior.incButtonReleased();
        }
        
        protected void startAutoSelect() {
            if (autoScroll) return;
            autoScroll = true;
            scrollBehavior.incButtonPressed();
        }
        
        protected void scrollValueChanged() {
            if (!dragSelect || lastSelected == -1) return;
            int last = flow.getLastVisibleCell().getIndex();
            if (last != -1) {
                lastSelected = last;
                getSelectionModel().select(lastSelected);
            }
        }
        protected MultipleSelectionModel<T> getSelectionModel() {
            return selectionProperty.get();
        }

        private void grabVBarSkinProperties() {
            Skin<?> skin = vbar.getSkin();
            scrollBehavior = (ScrollBarBehavior) FXUtils.invokeGetFieldValue(ScrollBarSkin.class, skin, "behavior");
            vbar.skinProperty().removeListener(vbarSkinListener);
        }

        private void grabSkinProperties() {
            flow = (VirtualFlow) control.lookup(".virtual-flow");
            vbar = (ScrollBar) control.lookup(".scroll-bar");
            vbar.skinProperty().addListener(vbarSkinListener);
            vbar.valueProperty().addListener(valueListener);
            control.skinProperty().removeListener(controlSkinListener);
        }
    }
    
    private ScrollBar vbar;
    private ScrollBarBehavior scrollBehavior;
    private VirtualFlow<?> flow;
    
    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(
                Arrays.stream(Locale.getAvailableLocales(), 10, 60).collect(Collectors.toList()));
//                Locale.getAvailableLocales());
        ListView<Locale> list = new ListView<>(data);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        new DragSelectSupport(list, list.selectionModelProperty());
//        list.setCellFactory(e -> {
//            return new DragAwareListCell<>();
//        });
        
        
        ScrollBar bar = new ScrollBar();
        bar.setMax(100);
        bar.setOrientation(Orientation.VERTICAL);
        list.setOnScroll(e -> {
        });
        
        bar.addEventFilter(ScrollEvent.ANY, e -> {
            LOG.info("scrolling: " + e);
            
        });
        list.skinProperty().addListener((src, ov, nv) -> {
            flow = (VirtualFlow) list.lookup(".virtual-flow");
            vbar = (ScrollBar) list.lookup(".scroll-bar");
//            vbar.setUnitIncrement(20);
            vbar.skinProperty().addListener((s, o, n) -> {
                Skin<?> skin = vbar.getSkin();
                scrollBehavior = (ScrollBarBehavior) FXUtils.invokeGetFieldValue(ScrollBarSkin.class, skin, "behavior");
                
            });
            LOG.info("" + flow + vbar);
//            vbar.valueProperty().addListener((sr, o, n) -> {
//                LOG.info("value: " + n);
//            });
        });
        BorderPane content = new BorderPane(list);
        content.setRight(bar);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Unused for now: doesn't help to move to cell? Need to support some sort of
     * @author Jeanette Winzenburg, Berlin
     */
    public static class DragAwareListCell<T> extends ListCell<T> {
    
            boolean inDrag;
            public DragAwareListCell() {
                super();
                addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
    //                LOG.info("pick: " + e.getPickResult());
                    
                    if (!inDrag) {
                        inDrag = true;
                        LOG.info("dragged on: " + getItemText(getItem()) 
    //                        + "\nat awt/fx: " + MouseInfo.getPointerInfo().getLocation() 
    //                        + "\n / [x, " + e.getScreenX() + ", y" + e.getScreenY()
                            );
                    }
                });
    //            addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
    //                LOG.info("moved: " + getItemText(getItem()));
    //            });
    //            addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
    //                LOG.info("entered: " + getItemText(getItem()));
    //            });
    //            addEventFilter(MouseEvent.MOUSE_EXITED, e -> {
    //                inDrag = false;
    //                LOG.info("exited: " + getItemText(getItem()));
    //            });
    //            setOnDragOver(
    ////                    DragEvent.DRAG_OVER, 
    //                    e -> {
    //                LOG.info("" + getItemText(getItem()));
    //            });
            }
    
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getItemText(item));
                }
            }
    
            /**
             * @param item
             * @return
             */
            protected String getItemText(T item) {
                return item.toString();
            }
            
            
        }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListViewMultiSelectByMouseDrag.class.getName());

}
