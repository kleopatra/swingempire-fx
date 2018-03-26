/*
 * Created on 09.02.2018
 *
 */
package de.swingempire.fx.scene;

import java.util.Arrays;
import java.util.List;

import static javafx.application.Application.launch;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8095306
 * Combo not clickable when used in cell.
 * 
 * fixed in fx8 (by showing the popup when instantiating the skin)
 * but: might be related to readdFocused - it's not synced when
 *    re-adding.
 */
public class ComboShowingWithoutScene extends Application {

    private TableView<String> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    private final List<String> countryList = Arrays.asList("China", "France", "New Zealand",
            "United States", "Germany", "Canada");

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setVisibleRowCount(5);

        stage.setTitle("Table View Sample");
        stage.setWidth(450);
        stage.setHeight(550);

        table.setEditable(true);
        table.getSelectionModel().setCellSelectionEnabled(true);

        TableColumn firstNameCol = new TableColumn("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellFactory(new Callback() {

            @Override
            public Object call(Object param) {
                return new TableCell<String, String>() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void startEdit() {
                        super.startEdit();
                        setText(null);
                        
                        setGraphic(comboBox);
                        ObservableList<String> items = FXCollections.observableList(countryList);
                        comboBox.setItems(items);
                        comboBox.setValue(countryList.get(0));


                        comboBox.show(); // This call is causing the trouble.
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public void cancelEdit() {
                        super.cancelEdit();

                        setText("test");
                        setGraphic(null);
                    }
                };
            }
        });

        table.setItems(FXCollections.observableArrayList("testData"));
        table.getColumns().addAll(firstNameCol);

        ((Group) scene.getRoot()).getChildren().addAll(table);

        stage.setScene(scene);
        stage.show();
    }
}
