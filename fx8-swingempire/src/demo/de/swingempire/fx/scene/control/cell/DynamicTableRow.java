/*
 * Created on 19.08.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * TableRow not updated on setting a new TableRowFactory. 
 * Only done if setting a cellFactory at the same time, so
 * looks like the expected behavior is a side-effect of
 * setting the cellFactory.
 * both 8b102, oldish 9ea
 * <p>
 * question on SO: http://stackoverflow.com/q/39031492/203657
 * <p>
 * learn-item: contextMenu can be shared across controls!
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DynamicTableRow extends Application {

    private final TableView<Person> table = new TableView<>();
    private final ComboBox<String> combo = new ComboBox<>();
    private final ObservableList<Person> data =
        FXCollections.observableArrayList(
            new Person("Jacob", "Smith", "jacob.smith@example.com"),
            new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
            new Person("Ethan", "Williams", "ethan.williams@example.com"),
            new Person("Emma", "Jones", "emma.jones@example.com"),
            new Person("Michael", "Brown", "michael.brown@example.com")
        );

    private Map<Object, ContextMenu> menus; 
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        
        Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(450);
        stage.setHeight(500);
        stage.setTitle(FXUtils.version());
        final Label label = new Label("Right Click a table Row");
        label.setFont(new Font("Arial", 20));

        combo.getItems().setAll("Type1","Type2");
        combo.getSelectionModel().select(0);
        menus = new HashMap<>();
        menus.put(combo.getItems().get(0), createContextMenu(combo.getItems().get(0)));
        menus.put(combo.getItems().get(1), createContextMenu(combo.getItems().get(1)));
        table.setEditable(true);

        //----------------------- Add table column ------------------------------//
        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("firstName"));

        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<>("lastName"));

        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(
                new PropertyValueFactory<>("email"));

        table.setItems(data);
        table.getColumns().addAll(Arrays.asList(firstNameCol, lastNameCol, emailCol));

        setTableMenu(combo.getValue());  // You Can Comment This //

        combo.setOnAction(event -> {
            setTableMenu(combo.getSelectionModel().getSelectedItem());
        });

        //-------------------------- Final Works ------------------------------//
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, combo, table);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }

    //---------- Set What ContextMenu according to the combobox -----------//
    
    /**
     * Update row/cellFactory on selection change in the combo, the goal is to
     * have a ContextMenu on the row that is controlled by the selection.
     * This method sets a new row factory with a new contextMenu containing
     * the selected item.
     * <p>
     * Issue: right-clicking the row still shows the old contextMenu. 
     * Behaves as expected when setting a cellFactory at the same time.
     */
    private void setTableMenu(Object type) {
         setRowFactory(type);  
         setCellFactory(type);
    }

    protected void setRowFactory(Object type) {
        LOG.info("setting rowFactory");
        table.setRowFactory((TableView<Person> tableView) -> {
            final TableRow<Person> row = new TableRow<>();
            // recreate
//            bindContextMenu(row, createContextMenu(type + " row"));
            // reuse
            bindContextMenu(row, menus.get(type));
            return row ;  
        });
    }

    protected void setCellFactory(Object type) {
        LOG.info("setting cellFactory");
        TableColumn first = table.getColumns().get(0);
        first.setCellFactory(p -> {
            TableCell cell = new MyTableCell();
//            bindContextMenu(cell, createContextMenu(type + " cell"));
            bindContextMenu(cell, menus.get(type));
            return cell;
        });
    }

    protected ContextMenu createContextMenu(String typeString) {
        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem item = new MenuItem(typeString + " menu");
        item.setOnAction((ActionEvent event) -> {
            System.out.println(typeString + " menu selected");
        });
        contextMenu.getItems().add(item);
        return contextMenu;
    }

    protected void bindContextMenu(final Cell cell, final ContextMenu contextMenu) {
            cell.contextMenuProperty().bind(
                Bindings.when(cell.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
        );
    }

    private static class MyTableCell<S, T> extends TableCell<S, T> {
        
        public MyTableCell() {
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

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DynamicTableRow.class.getName());
}