/*
 * Created on 03.06.2014
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Driver for ListXView. Here we compare against tableView with a
 * single column.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListTableFakeExample extends Application{

    private TableView<Person> tableView;
    private EventHandler<ActionEvent> updateFirstTitle;
    private EventHandler<ActionEvent> logComboValue;
    private ComboBox<Person> comboBox;

    public ListTableFakeExample() {
        // BUG: scrollbar missing default context menu
//        manager.addAlbums(10000);
        
        tableView = new TableView<Person>();
        tableView.setEditable(true);
        // quick check for http://stackoverflow.com/q/36452735/203657
        // let cells receive keyboard events
        // doesn't work, similar to contextMenu triggers
        // see experiments with custom event dispatchers
        tableView.setRowFactory(rf -> {
            TableRow row = new TableRow() {
                {
                    addEventFilter(KeyEvent.KEY_RELEASED, e -> {
                        System.out.println("got key " + e);
                    });
                }
            };
            return row;
        });
//        listView.setCellValueFactory(new PropertyFactory<>("lastName"));
        TableColumn<Person, String> lastName = new TableColumn<>("Last Name");
        lastName.setCellValueFactory(cc -> cc.getValue().lastNameProperty());
        lastName.setCellFactory(TextFieldTableCell.forTableColumn());
        tableView.getColumns().addAll(lastName);
        tableView.setItems(Person.persons());
//        listView.setItems(FXCollections.observableList(Person.persons(), p -> new Observable[] { p.lastNameProperty()}));

        comboBox = new ComboBox<>(tableView.getItems());
        
        StringConverter<Person> converter = new StringConverter<Person>() {

            @Override
            public String toString(Person album) {
                return album != null ? album.getLastName() : null;
            }

            @Override
            public Person fromString(String string) {
                return null;
            }
            
        };
        comboBox.setConverter(converter);

        logComboValue = e -> {
            LOG.info("comboValue: " + comboBox.getValue());
        };
        comboBox.setOnAction(logComboValue);
        
        updateFirstTitle = e -> {
            Person fx = tableView.getItems().get(0);
            fx.setLastName(fx.getLastName() + "X");
        };

    }
    
    private Region getContent() {
        BorderPane pane = new BorderPane();
        pane.setTop(comboBox);
        pane.setCenter(tableView);
        Button updateButton = new Button("update first title");
        updateButton.setOnAction(updateFirstTitle);
        pane.setBottom(updateButton);
        return pane;
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        Region content = getContent();
        Scene scene = new Scene(content);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch();
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ListTableFakeExample.class
            .getName());
}
