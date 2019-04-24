/*
 * Created on 18.04.2019
 *
 */
package de.swingempire.fx.fxml;

import java.util.logging.Logger;

import de.swingempire.fx.fxml.ChoiceBoxApp.Test;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxUpdateBug extends Application {

    private int i;
    
    private Parent createContent() {
        ObservableList<Test> items = FXCollections.observableArrayList(e -> new Observable[]{e.nameProperty()});
        Test test1 = new Test("test1");
        Test test2 = new Test("test2");
        Test test3 = new Test("test3");

        Button button = new Button("change item");//controller.getButton();
        button.setOnAction(e -> {
            test1.setName("name changed" + ++i);
        });

        items.addAll(test1, test2, test3);
        ChoiceBox choiceBox = new ChoiceBox();//controller.getChoiceBox();
        choiceBox.setItems(items);

        StringConverter<Test> converter = new StringConverter<Test>() {

            @Override
            public String toString(Test album) {
                return album != null ? album.getName() : null;
            }

            @Override
            public Test fromString(String string) {
                return null;
            }
        };

        choiceBox.setConverter(converter);

        
        return new VBox(choiceBox, button);
    }

    class Test {
        StringProperty name;

        public Test(String name) {
            setName(name);
        }

        public StringProperty nameProperty() {
            if (name == null) name = new SimpleStringProperty(this, "name");
            return name;
        }

        public void setName(String name) {
            nameProperty().set(name);
        }

        public String getName() {
            return nameProperty().get();
        }
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
            .getLogger(ChoiceBoxUpdateBug.class.getName());

}
