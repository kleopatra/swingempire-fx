/*
 * Created on 03.06.2014
 *
 */
package de.swingempire.fx.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.property.PropertyFactory;
import de.swingempire.fx.scene.control.ListXView;

/**
 * Driver for ListXView.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListXExample extends Application{

    private ListXView<Person, String> listView;
    private EventHandler<ActionEvent> updateFirstTitle;
    private EventHandler<ActionEvent> logComboValue;
    private ComboBox<Person> comboBox;

    public ListXExample() {
        // BUG: scrollbar missing default context menu
//        manager.addAlbums(10000);
        
        listView = new ListXView<Person, String>();
//        listView.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        listView.setCellValueFactory(new PropertyFactory<>("lastName"));
        
        listView.setItems(Person.persons());
//        listView.setItems(FXCollections.observableList(Person.persons(), p -> new Observable[] { p.lastNameProperty()}));

        comboBox = new ComboBox<>(listView.getItems());
        
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
            Person fx = listView.getItems().get(0);
            fx.setLastName(fx.getLastName() + "X");
        };

    }
    
    private Region getContent() {
        BorderPane pane = new BorderPane();
        pane.setTop(comboBox);
        pane.setCenter(listView);
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
    private static final Logger LOG = Logger.getLogger(ListXExample.class
            .getName());
}
