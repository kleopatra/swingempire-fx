/*
 * Created on 23.02.2015
 *
 */
package de.swingempire.fx.scene.control.et;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
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
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewETContextMenu extends Application {

    private Parent getContent() {
        EventDispatcher t;
        ObservableList<String> data = FXCollections.observableArrayList("one", "two", "three");
//        ListView<String> listView = new ListView<>();
        ListViewC<String> listView = new ListViewC<>();
        listView.setItems(data);
        listView.setCellFactory(p -> new ListCellC<>(new ContextMenu(new MenuItem("item"))));
        return listView;
    }

    /**
         * ListViewSkin that implements EventTarget and 
         * hooks the focused cell into the event dispatch chain
         */
        private static class ListViewCSkin<T> extends ListViewSkin<T> implements EventTarget {
            private EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    
            @Override
            public EventDispatchChain buildEventDispatchChain(
                    EventDispatchChain tail) {
                int focused = getSkinnable().getFocusModel().getFocusedIndex();
                if (focused > - 1) {
                    Cell<?> cell = flow.getCell(focused);
                    tail = cell.buildEventDispatchChain(tail);
                }
                // the handlerManager doesn't make a difference
                return tail.prepend(eventHandlerManager);
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
}
