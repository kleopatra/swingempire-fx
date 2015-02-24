/*
 * Created on 23.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Stage;

import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.skin.ListViewSkin;

/**
 * Activate cell contextMenu by keyboard, quick shot on ListView<p>
 * 
 * Not supported: contextMenu requests are dispatched to the focused
 * component (which is the listView), not the cell. Need to dispatch
 * further down, as described in the issue evaluation<p>
 * 
 * https://javafx-jira.kenai.com/browse/RT-40071
 * <p>
 * 
 * Here's a stand-alone example  
 * 
 * ContextMenu is shown, but at a location relative to the containing
 * view, not relative to the cell. How to position it relative to 
 * the cell? <p>
 * 
 * Asked at SO
 * http://stackoverflow.com/q/28673753/203657
 * 
 * Notes after debugging:
 * the keyboard-triggered contextMenuEvent is created for a location somewhere 
 * in the middle of the targetComponent, that is the focusOwner and fired
 * onto the focusOwner. Subsequent dispatches
 * in the chain transform the coordinates into the new target's local
 * coordinates, but don't change
 * the location: even in cell coordinates, it's still in the middle of the
 * containing ListView! <p>
 * 
 * Need to explore if we can change the location? Custom EventDispatcher that
 * special cases a keyboard triggered ContextMenuEvent? 
 * 
 * @author Jeanette Winzenburg, Berlin
 * 
 * @see TableViewET
 * @see TableViewETSkin
 * @see TableRowET
 * @see TableRowETSkin
 * @see TableViewETContextMenu
 */
public class ListViewETContextMenu extends Application {

    private Parent getContent() {
        EventDispatcher t;
        ContextMenuEvent s;
        ObservableList<String> data = FXCollections.observableArrayList("one", "two", "three");
//        ListView<String> listView = new ListView<>();
        
        ListViewC<String> listView = new ListViewC<>();
//        listView.setContextMenu(new ContextMenu(new MenuItem("listView")));
        listView.setItems(data);
        listView.setCellFactory(p -> new ListCellC<>(new ContextMenu(new MenuItem("item"))));
        return listView;
    }

    /**
     * EventDispatcher that replaces a keyboard-triggered ContextMenuEvent by a 
     * newly created event that has screen coordinates relativ to the target cell.
     * 
     */
    private static class ContextMenuEventDispatcher implements EventDispatcher {

        private EventDispatcher delegate;
        private Cell<?> targetCell;
        
        public ContextMenuEventDispatcher(EventDispatcher delegate) {
            this.delegate = delegate;
        }
        
        /**
         * Sets the target cell for the context menu.
         * @param cell
         */
        public void setTargetCell(Cell<?> cell) {
            this.targetCell = cell;
        }
        
        /**
         * Implemented to replace a keyboard-triggered contextMenuEvent before
         * letting the delegate dispatch it.
         * 
         */
        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            event = handleContextMenuEvent(event);
            return delegate.dispatchEvent(event, tail);
        }

        private Event handleContextMenuEvent(Event event) {
            if (!(event instanceof ContextMenuEvent) || targetCell == null) return event;
            ContextMenuEvent cme = (ContextMenuEvent) event;
            if (!cme.isKeyboardTrigger()) return event;
            final Bounds bounds = targetCell.localToScreen(
                    targetCell.getBoundsInLocal());
            // calculate screen coordinates of contextMenu
            double x2 = bounds.getMinX() + bounds.getWidth() / 4;
            double y2 = bounds.getMinY() + bounds.getHeight() / 2;
            // instantiate a contextMenuEvent with the cell-related coordinates
            ContextMenuEvent toCell = new ContextMenuEvent(ContextMenuEvent.CONTEXT_MENU_REQUESTED, 
                    0, 0, x2, y2, true, null);
            return toCell;
        }
        
    }
    
    /**
     * ListViewSkin that implements EventTarget and hooks the focused cell into
     * the event dispatch chain
     */
    private static class ListViewCSkin<T> extends ListViewSkin<T> implements
            EventTarget {

        private ContextMenuEventDispatcher contextHandler = 
                new ContextMenuEventDispatcher(new EventHandlerManager(this));
        
        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            int focused = getSkinnable().getFocusModel().getFocusedIndex();
            Cell cell = null;
            if (focused > -1) {
                cell = flow.getCell(focused);
                tail = cell.buildEventDispatchChain(tail);
            }
            contextHandler.setTargetCell(cell);
            // the handlerManager doesn't make a difference
            return tail.prepend(contextHandler);
        }

        // boiler-plate constructor
        public ListViewCSkin(ListView<T> listView) {
            super(listView);
        }

    }

    /**
     * ListView that hooks its skin into the event dispatch chain.
     */
    private static class ListViewC<T> extends ListView<T> {

        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            if (getSkin() instanceof EventTarget) {
                tail = ((EventTarget) getSkin()).buildEventDispatchChain(tail);
            }
            return super.buildEventDispatchChain(tail);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ListViewCSkin<>(this);
        }
        
    }
    
    private static class ListCellC<T> extends ListCell<T> {
     
        public ListCellC(ContextMenu menu) {
            setContextMenu(menu);
//            setOnMousePressed(e -> DebugUtils.printLocalTo(this));
//            setOnContextMenuRequested(e -> DebugUtils.printContextLocation(e));
        }
        
        // boiler-plate: copy of default implementation
        @Override 
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item instanceof Node) {
                setText(null);
                Node currentNode = getGraphic();
                Node newNode = (Node) item;
                if (currentNode == null || ! currentNode.equals(newNode)) {
                    setGraphic(newNode);
                }
            } else {
                /**
                 * This label is used if the item associated with this cell is to be
                 * represented as a String. While we will lazily instantiate it
                 * we never clear it, being more afraid of object churn than a minor
                 * "leak" (which will not become a "major" leak).
                 */
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
        }

    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListViewETContextMenu.class.getName());
}
