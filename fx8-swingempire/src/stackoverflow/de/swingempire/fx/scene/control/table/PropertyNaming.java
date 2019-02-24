/*
 * Created on 21.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Wondering: property starting with upper letter is okay for all accessors, 
 * setters/getters and property!
 * https://stackoverflow.com/q/54803893/203657
 * 
 * PENDING: different from core beans?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PropertyNaming extends Application {

    public static class Item {
//        String Name;
        StringProperty Name;
        
        public Item(String Name) {
            this.Name = new SimpleStringProperty(Name);
        }
        
        public StringProperty NameProperty() {
            return Name;
        }
//        public void setName(String Name) {
//            this.Name = Name;
//        }
//        
//        public String getName() {
//            return Name;
//        }
    }
    private Parent createContent() {
        TableView<Item> table = new TableView<>(FXCollections.observableArrayList(
                new Item("just a name")
                ));
        TableColumn<Item, String> column = new TableColumn<>("name");
        column.setCellValueFactory(new PropertyValueFactory<>("Name"));
        table.getColumns().addAll(column);
        return table;
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
            .getLogger(PropertyNaming.class.getName());

}
