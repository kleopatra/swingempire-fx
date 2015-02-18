/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.util.FXUtils;

/**
 * per-cell contextMenu must be triggered by keyboard 
 * (f.i. shift-f10 on win) 
 * 
 */
public class TableViewContextMenu extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        TableView<Person> table = new TableView<>();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Person,String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        ContextMenu cellMenu = new ContextMenu(new MenuItem("cellItem"));
        firstNameCol.setCellFactory(p -> new PlainTableCell(cellMenu));
        table.getColumns().addAll(firstNameCol); 
        table.setItems(data);

        Button button = new Button("dummy for comparison");
        button.setContextMenu(new ContextMenu(new MenuItem("buttonItem")));
        Tooltip t = new Tooltip("button");
        button.setTooltip(t);
        t.setOnShowing(e -> {
            Scene scene = t.getScene();
            Parent root = scene.getRoot();
            // side-effect
//            t.setText("x/y: " + t.getX() + t.getY());
            t.textProperty().set("x/y: " + t.getX() + t.getY());
//            LOG.info("showing: " + t.getX() + "/" + t.getY());
        });
        VBox pane = new VBox(table, button);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setTitle(FXUtils.version(true));
    }

    /**
     * C&P of default tableCell in TableColumn + contextMenu
     */
    public static class PlainTableCell<S, T> extends TableCell<S, T> {
        
        public PlainTableCell(ContextMenu menu) {
            setContextMenu(menu);
        }
        @Override protected void updateItem(T item, boolean empty) {
            if (item == getItem()) return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node)item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }

    }

    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private Person(String fName, String lName) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
        }
        public String getFirstName() {return firstName.get();}
        public void setFirstName(String fName) {firstName.set(fName);}
        public Property<String> firstNameProperty() { return firstName; }
        public String getLastName() {return lastName.get();}
        public void setLastName(String fName) {lastName.set(fName);}
        public Property<String> lastNameProperty() { return lastName; }
    }

    ObservableList<Person> data = FXCollections.observableArrayList(
            new Person("Jacob", "Smith"),
            new Person("Isabella", "JohnsoJn"),
            new Person("Ethan", "Williams"),
            new Person("Emma", "Jones"),
            new Person("Michael", "Brown")
    );


    public static void main(String[] args) {
        Application.launch(args);
    }
}
