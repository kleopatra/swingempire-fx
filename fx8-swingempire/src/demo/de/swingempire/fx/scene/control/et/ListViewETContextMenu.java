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

import de.swingempire.fx.util.DebugUtils;

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
        // core
//        ListView<String> listView = new ListView<>();
        // custom that effectively builds the chain twice
//        ListViewC<String> listView = new ListViewC<>();
        // custom that builds either from listView or from cell
        ListViewET<String> listView = new ListViewET<>();
        listView.setContextMenu(new ContextMenu(new MenuItem("listView")));
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
        private ContextMenuEvent originalContextMenuEvent;
        
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
         * letting the delegate dispatch it.<p>
         * 
         * PENDING how to replace the event only if the cell actually 
         * has a contextMenu?
         */
        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            event = maybeReplaceContextMenuEvent(event);
            // trying to not interfere if cell has no event ... no effect
//            Event tailHandled = delegate.dispatchEvent(event, tail);
//            if (originalContextMenuEvent != null && tailHandled == event) {
//                event = originalContextMenuEvent;
//            } else {
//                event = tailHandled;
//            }
//            originalContextMenuEvent = null;
            DebugUtils.printSourceTarget(event);
            return delegate.dispatchEvent(event, tail);
        }

        private Event maybeReplaceContextMenuEvent(Event event) {
            if (!(event instanceof ContextMenuEvent) || targetCell == null) return event;
            ContextMenuEvent cme = (ContextMenuEvent) event;
            if (!cme.isKeyboardTrigger()) return event;
            originalContextMenuEvent = cme;
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
 
//------------- ListViewET/Skin: trying to build the chain once only
    
    /**
     * ListViewSkin that implements EventTarget and hooks the focused cell into
     * the event dispatch chain.
     * 
     * No visible change against the C version ... still missing something
     */
    private static class ListViewETSkin<T> extends ListViewSkin<T> implements
            EventTarget {

        private ContextMenuEventDispatcher contextHandler = 
                new ContextMenuEventDispatcher(new EventHandlerManager(this));
        
        /**
         * Implemented to return the chain build from the focused cell, if 
         * available, or null if nothing focused.
         * 
         * PENDING the null return value violates super's contract!
         */
        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            int focused = getSkinnable().getFocusModel().getFocusedIndex();
            Cell<?> cell = null;
            if (focused > -1) {
                cell = flow.getCell(focused);
                contextHandler.setTargetCell(cell);
                tail = tail.prepend(contextHandler);
                tail = cell.buildEventDispatchChain(tail);
                return tail;//.prepend(contextHandler);
            }
            return null;
        }

        // boiler-plate constructor
        public ListViewETSkin(ListView<T> listView) {
            super(listView);
        }

    }

    /**
     * ListView that hooks its skin into the event dispatch chain.
     * 
     * Here we try to build the chain either from the cell or from 
     * the list.
     */
    private static class ListViewET<T> extends ListView<T> {

        @Override
        public EventDispatchChain buildEventDispatchChain(
                EventDispatchChain tail) {
            if (getSkin() instanceof EventTarget) {
                tail = ((EventTarget) getSkin()).buildEventDispatchChain(tail);
            }
            // PENDING this is whacky, violating super's contract
            if (tail != null) {
                return tail;
            }
            return super.buildEventDispatchChain(tail);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ListViewETSkin<>(this);
        }
        
    }

 //---------------- end of ListView/SkinET   
    
    
//---------------------- ListView/SkinC: builds the chain twice!    
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
            Cell<?> cell = null;
            if (focused > -1) {
                cell = flow.getCell(focused);
                tail = cell.buildEventDispatchChain(tail);
            }
            contextHandler.setTargetCell(cell);
            return tail.prepend(contextHandler);
        }


        // boiler-plate constructor
        public ListViewCSkin(ListView<T> listView) {
            super(listView);
        }

    }

    /**
     * ListView that hooks its skin into the event dispatch chain.
     * 
     * PENDING: shouldn't build the chain up to the scene twice, as we do here
     * - once from the focused cell (done in skin)
     * - once from this list (done in super
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

//------------------- end of ListView/SkinC
    
    
    private static class ListCellC<T> extends ListCell<T> {
     
        ContextMenu menu;
        public ListCellC(ContextMenu menu) {
            this.menu = menu;
//            setOnMousePressed(e -> DebugUtils.printLocalTo(this));
//            setOnContextMenuRequested(e -> DebugUtils.printContextLocation(e));
        }
        
        // boiler-plate: copy of default implementation
        @Override 
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                setContextMenu(null);
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
                setContextMenu(menu);
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
        }

    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
//        scene.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
//        scene.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, e -> e.consume());
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
