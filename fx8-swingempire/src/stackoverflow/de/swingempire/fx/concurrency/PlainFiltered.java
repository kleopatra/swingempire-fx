/*
 * Created on 31.03.2018
 *
 */
package de.swingempire.fx.concurrency;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/49580632/203657
 * throws exception 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PlainFiltered extends Application {

    private static ObservableList<Object> allObjects;
    private static FilteredList<Object> filteredObjects;

    public static void main(String[] args) {
        allObjects = FXCollections.observableArrayList();
        allObjects.addListener((ListChangeListener<Object>) change ->
                filteredObjects.setPredicate(title -> true)
        );
        filteredObjects= new FilteredList<>(allObjects, p -> true);

        Task task = new Task<Void>() {
            @Override
            protected Void call() {
                for (int i = 0; i < 100; i++) {
                    allObjects.add(new Object());
                }
                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.run();
    }

    @Override
    public void start(Stage stage) {
    }
}

