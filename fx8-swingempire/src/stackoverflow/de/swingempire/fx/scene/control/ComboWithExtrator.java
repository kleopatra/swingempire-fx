/*
 * Created on 06.02.2020
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * https://stackoverflow.com/q/60096575/203657
 * update string rep when updating property of item
 * 
 * as always: needs extractor
 */
public class ComboWithExtrator extends Application {

    private Parent createContent() {
        ObservableList<Hero> heros 
            = FXCollections.observableArrayList((Hero param) -> new Observable[] {param.levelProperty() });
        heros.add(new Hero("Ted", 1));
        heros.add(new Hero("Zed", 10));
        heros.add(new Hero("Med", 25));

        ComboBox<Hero> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(350);
        comboBox.setItems(heros);
        comboBox.setConverter(new StringConverter<Hero>() {
            @Override
            public String toString(Hero hero) {
                if (hero == null) return null;
                return hero.getName() + " - Level: " +  hero.getLevel();
            }

            @Override
            public Hero fromString(String string) {
                return null;
            }
        });

        Button button = new Button("Level Up");
        button.setOnAction(e -> {
             Hero value = comboBox.getValue();
             value.levelProperty().setValue(value.getLevel() + 1);
        });

        HBox hbox = new HBox(comboBox, button);
        return hbox;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    private static class Hero {

        private String name;
        private IntegerProperty level;

        public Hero(String name, Integer level) {
            this.name = name;
            this.level = new SimpleIntegerProperty(level);
        }

        private String getName() {
            return name;
        }

        private int getLevel() {
            return level.get();
        }

        public IntegerProperty levelProperty() {
            return level;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboWithExtrator.class.getName());

}
