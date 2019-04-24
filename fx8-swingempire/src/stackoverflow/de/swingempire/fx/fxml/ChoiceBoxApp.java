/*
 * Created on 18.04.2019
 *
 */
package de.swingempire.fx.fxml;


import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ChoiceBoxApp extends Application {

    public static int i = 0;

    @Override
        public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("choicebox.fxml"));
        Parent root = loader.load();
        ChoiceBoxController controller = loader.getController();
        primaryStage.setTitle("Hello World");

        ObservableList<Test> items = FXCollections.observableArrayList(e -> new Observable[]{e.nameProperty()});
        Test test1 = new Test("test1");
        Test test2 = new Test("test2");
        Test test3 = new Test("test3");

        Button button = controller.getButton();
        button.setOnAction(e -> {
            test1.setName("name changed" + ++i);
        });

        items.addAll(test1, test2, test3);
        ChoiceBox choiceBox = controller.getChoiceBox();
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

        Scene scene = new Scene(root, 300, 275);

        primaryStage.setScene(scene);
        primaryStage.show();
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


    public static void main(String[] args) {
        launch(args);
    }
}

