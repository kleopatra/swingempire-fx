/*
 * Created on 27.06.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Selecting the checkBox is unrelated to selection state of the list's selectionModel.
 * https://stackoverflow.com/q/56789144/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewWithCheckBoxSelected extends Application {

    private class Model {
        String text;
        BooleanProperty selected;

        private Model(String text, Boolean selected) {
            this.text = text;
            this.selected = new SimpleBooleanProperty(selected);
        }

        BooleanProperty selectedProperty() {
            return selected;
        }
        @Override public String toString() {
            return text;
        }
    }
    
    ListView<Model> listView;
    
    private Parent createContent() {
        listView = new ListView<>();
//      without selection
//      listView.setCellFactory(CheckBoxListCell.forListView(Model::selectedProperty));

        // actual "bad" solution: register a listener to the slectedProperty of the 
        // checkBox and update the selectionModel state accordingly
        // problem here: multiple listeners registered
//        listView.setCellFactory(factory -> {
//            CheckBoxListCell<Model> cell = new CheckBoxListCell<Model>() {
//                @Override
//                public void updateItem(Model item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (empty) {
//                        setText(null);
//                        setGraphic(null);
//                        return;
//                    }
//                    ((CheckBox) getGraphic()).selectedProperty().addListener(
//                            (observable, oldValue, newValue) -> listView.getSelectionModel().select(getItem()));
//                }
//            };
//            cell.setSelectedStateCallback(Model::selectedProperty);
//            return cell;
//        });

        // answered the "once" part of the question
        listView.setCellFactory(factory -> {
            CheckBoxListCell<Model> cell = new CheckBoxListCell<Model>() {
                
                InvalidationListener graphicListener = g -> {
                    registerUIListener();
                };
                
                {
                    graphicProperty().addListener(graphicListener);
                }

                private void registerUIListener() {
                    if (!(getGraphic() instanceof CheckBox)) throw new IllegalStateException("checkBox expected");
                    graphicProperty().removeListener(graphicListener);
                    ((CheckBox) getGraphic()).selectedProperty().addListener(
                            (observable, oldValue, newValue) -> listView.getSelectionModel().select(getItem()));
                }
            };
            cell.setSelectedStateCallback(Model::selectedProperty);
            return cell;
        });
        
        ObservableList<Model> items = FXCollections.observableArrayList();

        items.add(new Model("A", true));
        items.add(new Model("B", true));
        items.add(new Model("C", false));

        listView.setItems(items);
        
        BorderPane content = new BorderPane(listView);
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
            .getLogger(ListViewWithCheckBoxSelected.class.getName());

}
