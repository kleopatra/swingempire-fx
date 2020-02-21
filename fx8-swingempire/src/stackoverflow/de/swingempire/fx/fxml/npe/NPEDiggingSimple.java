/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class NPEDiggingSimple extends Application {

    private static class InnerListCell extends ListCell<String> {
        private Button button;
        private boolean buttonFocused;
        int reuseCounter;
        static int created = 0;
        int instance;
        
        public InnerListCell() {
            instance= created++;
            button =  new Button();
            button.focusedProperty().addListener((src, ov, nv) -> {
                buttonFocused = nv;
            });
            

        }
        
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null || empty) {
                if (buttonFocused) {
                    if (getListView() != null) {
                        getListView().requestFocus();
                    }
                }
                setText(null);
                setGraphic(null);
                button.setText(null);
            } else {
                reuseCounter++;
                button.setText(item);
                setGraphic(button);
            }
//            System.out.println("button focused: " + getItem() + button + instance);
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
            cellListView.setCellFactory(c -> new InnerListCell());
            cellMarker = creationCount;
            System.out.println("main cell created: " + creationCount++);
            
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(cellListView);
            
//            sceneProperty().addListener((src, ov, nv) -> {
//                System.out.println("scene of outer: " + nv);
//            });
        }

        @Override
        public void updateIndex(int i) {
            int oldIndex = getIndex();
            super.updateIndex(i);
//            if (i < 0 && getScene() != null) {
//                System.out.println("in outer new/old " + i + " / "+ oldIndex);
//            }
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
//                System.out.println("main cell " + cellMarker + " reused: " + reuseCount++);
                Node focusOwner = getScene().getFocusOwner();
                boolean myList = focusOwner == getListView();
                
                System.out.println("focusOwner my list? " + myList + getIndex() + focusOwner);
                if (!myList && focusOwner != null) {
                    Node parent = focusOwner.getParent();
                    if (parent instanceof InnerListCell) {
                        InnerListCell parentCell = (InnerListCell) parent;
                        System.out.println("parent " + parentCell.buttonFocused + " " +
                                parentCell.getIndex() + " " + parentCell.getItem());
                        System.out.println("  instance " + parentCell.instance 
                               + " reuseCounter: "+ parentCell.reuseCounter);
                        System.out.println("bounds: " + parentCell.getListView().getBoundsInParent());
                        
                        Platform.runLater(() -> {
                            System.out.println("requesting focus: " + this);
//                            requestFocus();
                        }
                                );
                    }
//                    Node parent = focusOwner.getParent();
                    while (parent != null) {
//                        boolean parentIsCellListView = parent == cellListView;
//                        System.out.println("   -- parent: " + parentIsCellListView + "  " +parent);
                        parent = parent.getParent();
                    }
                }
            }
        }
        
        
    }
    
    private Parent createContent() {
        ObservableList<String> model = FXCollections.observableArrayList(
                "item1", "item2", "item3", "item4", "item5");
        outer = new ListView<>(model);
        outer.setCellFactory(c -> new OuterListCell());
        System.out.println("no console?");
        BorderPane content = new BorderPane(outer);
        return content;
    }

    
    private void scrollTo() {
        outer.scrollTo(1);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F1), this::scrollTo);
        scene.focusOwnerProperty().addListener((scr, ov, nv) -> {
            System.out.println("old/new: " 
                   + "   \n " + ov
                    + "   \n " + nv
                    );
        });
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(NPEDiggingSimple.class.getName());
    private ListView<String> outer;

}
