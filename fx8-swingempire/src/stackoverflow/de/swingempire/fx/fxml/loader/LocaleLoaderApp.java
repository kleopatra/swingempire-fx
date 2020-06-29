/*
 * Created on 29.06.2020
 *
 */
package de.swingempire.fx.fxml.loader;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Function;

import de.swingempire.fx.fxml.loader.ComboLoader.Item;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/44321738/203657
 * 
 * Loading data into a combo via fxml: use nameSpace to 
 */
public class LocaleLoaderApp extends Application {

    private ComboBox<Locale> loadCombo(Object itemLoader, Function<Locale, String> extractor) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("comboloader.fxml"));
        loader.getNamespace().put("itemLoader", itemLoader);
        loader.getNamespace().put("cellFactoryProvider", new ListCellFactory<Locale>(extractor));
        System.out.println(loader.getNamespace());
        ComboBox<Locale> combo = loader.load();
        return combo;
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {

        primaryStage.setTitle("Populate combo from custom builder");

        Group group = new Group();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(25, 25, 25, 25));
        group.getChildren().add(grid);
        LocaleProvider dataProvider = new LocaleProvider();
        grid.add(loadCombo(dataProvider, Locale::getDisplayName), 0, 0);
        grid.add(loadCombo(dataProvider, Locale::getLanguage), 1, 0);
        Scene scene = new Scene(group, 450, 175);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static class LocaleProvider {
        ObservableList<Locale> locales = FXCollections.observableArrayList(Locale.getAvailableLocales());
        
        public ObservableList<Locale> getItems() {
            return locales;
        }
    }
    

    public static void main(String[] args) {
        launch(args);
    }
}