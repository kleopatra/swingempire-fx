/*
 * Created on 09.02.2015
 *
 */
package de.swingempire.fx.event;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.skin.TableCellSkinBase;

/**
 * In custom cell with canvas, left (primary) button doesn't select (intended)
 * but right (secondary) button does select.
 * 
 * http://stackoverflow.com/q/28376103/203657
 */
public class DisableMouseEventSelect extends Application {
    private TableView<Person> table = new TableView<Person>();
    private ContextMenu menu = new ContextMenu();

    private final ObservableList<Person> data =
        FXCollections.observableArrayList(
                new Person("Jacob", "Smith", "jacob.smith@example.com"),
                new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                new Person("Ethan", "Williams", "ethan.williams@example.com"),
                new Person("Emma", "Jones", "emma.jones@example.com"),
                new Person("Michael", "Brown", "michael.brown@example.com")
        );

    class MyTableCell extends TableCell<Person, String> {
        public MyTableCell(ContextMenu menu) {
            super();
            setContextMenu(menu);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item);
            setGraphic(null);
        }
    }

    class MySpecialCellSkin extends TableCellSkinBase {
        private final TableColumn tableColumn;

        public MySpecialCellSkin(TableCell tableCell) {
            super(tableCell, new MySpecialCellBehavior(tableCell));
            // doesn't make a difference
            //consumeMouseEvents(true);
            this.tableColumn = tableCell.getTableColumn();
            super.init(tableCell);
        }

        @Override protected BooleanProperty columnVisibleProperty() {
            return tableColumn.visibleProperty();
        }

        @Override protected ReadOnlyDoubleProperty columnWidthProperty() {
            return tableColumn.widthProperty();
        }
        
    }
    
    class MySpecialCellBehavior extends TableCellBehavior {

        public MySpecialCellBehavior(TableCell control) {
            super(control);
        }

        @Override
        protected void doSelect(double x, double y, MouseButton button,
                int clickCount, boolean shiftDown, boolean shortcutDown) {
            if (button == MouseButton.SECONDARY) return;
            super.doSelect(x, y, button, clickCount, shiftDown, shortcutDown);
        }

    }

    class MySpecialCell extends MyTableCell {
        Canvas canvas = new Canvas(200.0, 12.0);
        public MySpecialCell() {
            super(null);
            canvas.setMouseTransparent(true);
            addEventFilter(MouseEvent.ANY, e -> e.consume());
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            if (! empty) {
                canvas.getGraphicsContext2D().strokeText(item, 5.0, 10.0);
                setGraphic(canvas);
            } else {
                setGraphic(null);
            }
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new MySpecialCellSkin(this);
        }
        
        
    }

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(450);
        stage.setHeight(500);

        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));

        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        menu.getItems().add(new MenuItem("Hello World"));
        Callback cellFactory = param -> new MyTableCell(menu);

        TableColumn firstNameCol = new TableColumn("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("firstName"));
        firstNameCol.setCellFactory(cellFactory);

        TableColumn lastNameCol = new TableColumn("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("lastName"));
        lastNameCol.setCellFactory(cellFactory);

        TableColumn emailCol = new TableColumn("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(
                new PropertyValueFactory<Person, String>("email"));
        emailCol.setCellFactory(param -> new MySpecialCell());

        table.setItems(data);
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;

        private Person(String fName, String lName, String email) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.email = new SimpleStringProperty(email);
        }

        public String getFirstName() {
            return firstName.get();
        }
        public void setFirstName(String fName) {
            firstName.set(fName);
        }

        public String getLastName() {
            return lastName.get();
        }
        public void setLastName(String fName) {
            lastName.set(fName);
        }

        public String getEmail() {
            return email.get();
        }
        public void setEmail(String fName) {
            email.set(fName);
        }

    }
}