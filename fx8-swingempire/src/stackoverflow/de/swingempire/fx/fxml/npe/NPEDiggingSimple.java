/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class NPEDiggingSimple extends Application {

    private static class InnerListCell extends ListCell<String> {
        private Button button;
        
        public InnerListCell() {
            button =  new Button();
            
        }
        
        @Override
        public void updateItem(String item, boolean empty)
        {
            super.updateItem(item, empty);
            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            }
            else {
                button.setText(item);
                setGraphic(button);
            }
        }

    }
    private static class OuterListCell extends ListCell<String> {
        static int creationCount;
        int cellMarker;
        int reuseCount;

        private ListView<String> cellListView;

        public OuterListCell() {
            setPrefHeight(300);
            setPrefWidth(300);
            
            cellListView = new ListView<>();
//            cellListView.setCellFactory(v -> new NoteCell());
            cellListView.setCellFactory(c -> new InnerListCell());
            cellMarker = creationCount;
            System.out.println("main cell created: " + creationCount++);
            
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(cellListView);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
//                setText(null);
//                setGraphic(null);
            } else {
                cellListView.getItems().setAll(item);
//                setGraphic(cellListView);
            }
            if (getScene() != null) {
                System.out.println("main cell " + cellMarker + " reused: " + reuseCount++);
                Node focusOwner = getScene().getFocusOwner();
                boolean myList = focusOwner == getListView();
                System.out.println("focusOwner my list? " + myList + focusOwner);
            }
        }
        
        
    }
    
    private Parent createContent() {
        ObservableList<String> model = FXCollections.observableArrayList(
                "item1", "item2", "item3", "item4", "item5");
        ListView<String> outer = new ListView<>(model);
        outer.setCellFactory(c -> new OuterListCell());
        BorderPane content = new BorderPane(outer);
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(NPEDiggingSimple.class.getName());

}
