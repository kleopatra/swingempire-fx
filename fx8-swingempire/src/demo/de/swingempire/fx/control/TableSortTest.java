/*
 * Created on 14.04.2015
 *
 */
package de.swingempire.fx.control;

import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Sorting not working ... here it does, but not in RL context.
 * http://stackoverflow.com/q/29610358/203657
 * 
 * In RL, it was an immutable list - then default sorting can't work
 * because it uses Collections.sort
 */
public final  class TableSortTest extends Application {

    private static final ObservableList<NumericCombo> values = FXCollections.observableList(
            IntStream.range(1, 100).mapToObj(i -> new NumericCombo()).collect(Collectors.toList()));

    public static void main(String[] args) { 
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Collections.shuffle(values);
        ObservableList<NumericCombo> immutable = 
                FXCollections.unmodifiableObservableList(values);
//                new ImmutableObservableList<>(values);
    
        
        TableView<NumericCombo> tableView = new TableView<>();
        SortedList sorted = new SortedList(immutable);
        tableView.setItems(sorted);
        sorted.comparatorProperty().bind(tableView.comparatorProperty());
        
        TableColumn<NumericCombo,Number> combo1 = new TableColumn<>("COMBO 1");
        combo1.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().combo1));

        TableColumn<NumericCombo,Number> combo2 = new TableColumn<>("COMBO 2");
        combo2.setCellValueFactory(c -> c.getValue().combo2);

        TableColumn<NumericCombo,Number> combo3 = new TableColumn<>("COMBO 3");
        combo3.setCellValueFactory(c -> c.getValue().combo3);

        tableView.getColumns().addAll(combo3, combo1,combo2);

        Group root = new Group(tableView);

        Scene scene = new Scene(root);

        primaryStage.setScene(scene);   

        primaryStage.show();

    }

    
    private static final class NumericCombo { 
        private static final Random rand = new Random();
        private static boolean odd = false;
        private final int combo1;
        private final IntegerProperty combo2;
        private final Property<Number> combo3;

        private NumericCombo() {
            combo1 = rand.nextInt((10000 - 0) + 1);
            combo2 = new SimpleIntegerProperty(rand.nextInt((10000 - 0) + 1));
            if (odd) {
                combo3 = new SimpleObjectProperty<>(rand.nextInt((10000 - 0) + 1));
            } else {
                    combo3 = new SimpleObjectProperty<>(rand.nextDouble() * 10000.);
                }
//            odd = !odd;
        }
    }
}

