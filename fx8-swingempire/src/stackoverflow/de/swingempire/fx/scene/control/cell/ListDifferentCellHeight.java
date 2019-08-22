/*
 * Created on 22.08.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57592354/203657
 * Changing cell content in updateItem based on its selected state
 * doesn't update cell height  
 * <p>
 * 
 * OP posted answer: related to editing cell? But here we don't
 * have any editing ... the trick is forcing a fake 
 * transition of editable state. Hacky but effective.
 * 
 * <p>
 * alternative (answered): change graphics "earlier" than updateItem
 * in updateSelected(boolean)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListDifferentCellHeight extends Application {
    private class SelectedCell extends VBox {
        public SelectedCell() {
            getChildren().add(new Label("---"));
            getChildren().add(new Label("Selected Cell"));
            getChildren().add(new Label("---"));
        }
    }

    private class DefaultCell extends VBox {
        public DefaultCell() {
            getChildren().add(new Label("Default Cell"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        List<String> items = List.of("Item A", "Item B", "Item C", "Item D");

        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(items);

//        listView.setEditable(true);
        LOG.info("fixed/editable " + listView.getFixedCellSize() + " " + listView.isEditable());
        listView.setCellFactory(lv -> new ListCell<>() {
            private SelectedCell selectedCell = new SelectedCell();
            private DefaultCell defaultCell = new DefaultCell();
            
// ------------- answer OP
//            {
//                // fake cancel/startEdit to force a re-layout 
//                // working even for not-editable lists
//                selectedProperty().addListener((observable, oldValue, newValue) -> {
//                    if(oldValue != null && oldValue) {
//                        cancelEdit();
//                    }
//
//                    if(newValue != null && newValue) {
//                        startEdit();
//                    }
//                });
//
//            }
//            @Override
//            public void startEdit() {
//                super.startEdit();
//                setGraphic(selectedCell);
//            }
//
//            @Override
//            public void cancelEdit() {
//                if(!isSelected()) {
//                    super.cancelEdit();
//                    setGraphic(defaultCell);
//                }
//            }
//------------- end answer op    
            
//--------- try override updateSelected: working, answered

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                setGraphic(selected ? selectedCell : defaultCell);
            }



            @Override
            protected void updateItem(String s, boolean b) {
                super.updateItem(s, b);

                if(s == null || b) {
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setGraphic(null);
                }
                else {
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(isSelected() ? selectedCell : defaultCell);
                    // answer OP
                    //setGraphic(isEditing() ? selectedCell : defaultCell);
                }
            }

//--------- end updatedSelected            
        });

        Scene scene = new Scene(listView, 200, 500);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListDifferentCellHeight.class.getName());
}

