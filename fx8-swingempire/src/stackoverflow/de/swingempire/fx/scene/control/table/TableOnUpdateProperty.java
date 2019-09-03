/*
 * Created on 03.09.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Simple example of update of cells in TableView/ListView
 * with different mechanisms:
 * 
 * <ul>
 * <li> TableView: expose property, use either PropertyValueFactory or poperty
 *     auto-update
 * <li> TableView: setters only, use PropertyValueFactory - no auto-update
 * <li> ListView: custom cellFactory, no auto-update
 * </ul>
 * @author Jeanette Winzenburg, Berlin
 */
public class TableOnUpdateProperty extends Application {

    public static class DummyItem {
        StringProperty name;
        String raw;
        
        public DummyItem(String name, String raw) {
            this.name = new SimpleStringProperty(this, "name", name);
            this.raw = raw;
        }
        
        public StringProperty nameProperty() {
            return name;
        }
        
        public void setName(String name) {
            nameProperty().set(name);
        }
        
        public String getName() {
            return nameProperty().get();
        }
        
        
        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }


        public static ObservableList<DummyItem> items = FXCollections.observableArrayList(
                new DummyItem("one", "raw one")
                , new DummyItem("other", "raw other")
                );
        
    }
    
    private Parent createContent() {
        TableView<DummyItem> table = new TableView<>(DummyItem.items);
        // property itself: updated on change
        TableColumn<DummyItem, String> prop = new TableColumn<>("use property");
        prop.setCellValueFactory(c -> c.getValue().nameProperty());
        // propertyValueFactory backed by property: unpdated on change
        TableColumn<DummyItem, String> useSetter = new TableColumn<>("use setters");
        useSetter.setCellValueFactory(new PropertyValueFactory<>("name"));
        // propertyValueFactory backed by getter/setter: not updated on change
        TableColumn<DummyItem, String> raw = new TableColumn<>("raw value");
        raw.setCellValueFactory(new PropertyValueFactory<>("raw"));
        
        table.getColumns().addAll(prop, useSetter, raw);
        
        // list has no cellObservableValue, so no update on change
        // way out is an extractor on the items
        ListView<DummyItem> list = new ListView<>(table.getItems());
        list.setCellFactory(cc -> {
            ListCell<DummyItem> cell = new ListCell<>() {

                @Override
                protected void updateItem(DummyItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getName());
                    }
                }
                
            };
            return cell;
        });
        
        Button button = new Button("edit first");
        button.setOnAction(e -> {
            DummyItem item = table.getItems().get(0);
            item.setName(item.getName() + "X");
            item.setRaw(item.getRaw() + "Y");
        });
        
        BorderPane content = new BorderPane(table);
        content.setRight(list);
        content.setBottom(button);
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
            .getLogger(TableOnUpdateProperty.class.getName());

}
