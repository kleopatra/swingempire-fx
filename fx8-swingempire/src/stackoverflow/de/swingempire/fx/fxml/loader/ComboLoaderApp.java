/*
 * Created on 29.06.2020
 *
 */
package de.swingempire.fx.fxml.loader;

import java.io.IOException;

import de.swingempire.fx.fxml.loader.ComboLoader.Item;
import de.swingempire.fx.fxml.loader.ListCellFactory.CListCell;
import javafx.application.Application;
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
public class ComboLoaderApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        primaryStage.setTitle("Populate combo from custom builder");

        Group group = new Group();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(25, 25, 25, 25));
        group.getChildren().add(grid);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("comboloader.fxml"));
        loader.getNamespace().put("itemLoader", new ComboLoader());
        loader.getNamespace().put("cellFactoryProvider", new ListCellFactory<Item>(Item::getName));
        System.out.println(loader.getNamespace());
        // was: ComboBox<String>
        ComboBox<Item> combo = loader.load();
//        combo.setCellFactory(cc -> new CListCell<Item>(item -> item.getName()));
        grid.add(combo, 0, 0);
//        ComboBox<Item> other = loader.load();
//        grid.add(other, 1, 0);
        Scene scene = new Scene(group, 450, 175);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}