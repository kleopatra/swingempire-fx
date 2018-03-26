/*
 * Created on 28.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.util.ArrayList;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
/**
 * Issue: item not updated in popup if bound items are listProperty
 * https://javafx-jira.kenai.com/browse/RT-29709
 * 
 * reported against 8u20, closed as not reproducible: cannot reproduce as well
 * neither with primitive nor with objects - problem seems to be fixed
 * 
 * though a bit weird that this was reported, can't think of a setup where
 * it would blow
 * 
 */
public class ChoiceBoxListPropertyAsItemsRT29709 extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(getScene());
//        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] arguments) {
        Application.launch(arguments);
    }

    public Scene getScene() {
        VBox v = createBoxWithPrimitiveItems();
        VBox v2 = createBoxWithObjectItems();
        HBox h = new HBox();
        h.getChildren().addAll(v, v2);
        Scene scene = new Scene(h);
        return scene;
    }

    protected VBox createBoxWithObjectItems() {
        
        final ChoiceBox<Person> box = new ChoiceBox<>();
        StringConverter<Person> c = new StringConverter<Person>() {

            @Override
            public String toString(Person object) {
                return object.getEmail();
            }

            @Override
            public Person fromString(String string) {
                return null;
            }
            
        };
        box.setConverter(c);
        ListProperty<Person> list = new SimpleListProperty<>(
                FXCollections.observableList(new ArrayList<Person>(), 
                        person -> new Observable[]{ person.emailProperty()}));
        // ObservableList<String> list = FXCollections.observableArrayList();
        
        for (int i = 0; i < 3; i++) {
            list.add(createPerson(i));
        }
        
        box.setItems(list);
        // Bindings.bindContent(box.getItems(), list);
        // box.itemsProperty().bind(list);
        
        Button add = new Button("Add Item");
        add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                box.getItems().add(createPerson(list.size()));
            }
        });
        
        Button set = new Button("Set Item");
        set.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                box.getItems().set(0, createPerson(list.size()));
            }
        });
        
        Button button = new Button("update Item");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                box.getItems().get(0).setEmail("changedEmail");
            }
        });
        
        list.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                System.out.println(observable);
                System.out.println(box.getItems());
            }
        });
        
        VBox v = new VBox();
        v.getChildren().addAll(box, add, set, button);
        return v;
    }
    /**
     * @param i
     * @return
     */
    private Person createPerson(int i) {
        return new Person("first" + i, "last" + i, "mail" + i);
    }

    protected VBox createBoxWithPrimitiveItems() {
        final String item1 = new String("Item 1");
        final String item2 = new String("Item 2");
        final String item3 = new String("Item 3");

        final ChoiceBox<String> box = new ChoiceBox<>();

        ListProperty<String> list = new SimpleListProperty<>(
                FXCollections.observableArrayList());
        // ObservableList<String> list = FXCollections.observableArrayList();

        list.addAll(item1, item2, item3);

        box.setItems(list);
        // Bindings.bindContent(box.getItems(), list);
        // box.itemsProperty().bind(list);

        Button add = new Button("Add Item");
        add.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                box.getItems().add(new String("Another added"));
            }
        });
        
        Button button = new Button("Set Item");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                box.getItems().set(0, "another set");
            }
        });

        list.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                System.out.println(observable);
                System.out.println(box.getItems());
            }
        });

        VBox v = new VBox();
        v.getChildren().addAll(box, add, button);
        return v;
    }
}
