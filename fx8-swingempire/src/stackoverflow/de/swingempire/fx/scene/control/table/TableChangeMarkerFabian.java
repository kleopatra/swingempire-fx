/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52519470/203657
 * highlight row with recently changed items.
 * @author fabian
 */
public class TableChangeMarkerFabian extends Application {

    private static class Item {

        private final IntegerProperty value = new SimpleIntegerProperty();
    }

    private final ObservableMap<Item, Long> markTimes = FXCollections.observableHashMap();
    private AnimationTimer updater;

    private void updateValue(Item item, int newValue) {
        int oldValue = item.value.get();
        if (newValue != oldValue) {
            item.value.set(newValue);

            // update time of item being marked
            markTimes.put(item, System.nanoTime());

            // timer for removal of entry
            updater.start();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Item item = new Item(); // the item that is updated
        TableView<Item> table = new TableView<>();
        table.getItems().add(item);

        // some additional items to make sure scrolling effects can be tested
        IntStream.range(0, 100).mapToObj(i -> new Item()).forEach(table.getItems()::add);

        TableColumn<Item, Number> column = new TableColumn<>();
        column.getStyleClass().add("mark-column");
        column.setCellValueFactory(cd -> cd.getValue().value);
        table.getColumns().add(column);

        final PseudoClass marked = PseudoClass.getPseudoClass("marked");

        table.setRowFactory(tv -> new TableRow<Item>() {

            final InvalidationListener reference = o -> {
                pseudoClassStateChanged(marked, !isEmpty() && markTimes.containsKey(getItem()));
            };
            final WeakInvalidationListener listener = new WeakInvalidationListener(reference);

            @Override
            protected void updateItem(Item item, boolean empty) {
                boolean wasEmpty = isEmpty();
                super.updateItem(item, empty);

                if (empty != wasEmpty) {
                    if (empty) {
                        markTimes.removeListener(listener);
                    } else {
                        markTimes.addListener(listener);
                    }
                }

                reference.invalidated(null);
            }

        });

        Scene scene = new Scene(table);
//        scene.getStylesheets().add("tablecolor.css");
        table.getStylesheets().add(this.getClass().getResource("tablecolor.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        updater = new AnimationTimer() {

            @Override
            public void handle(long now) {
                for (Iterator<Map.Entry<Item, Long>> iter = markTimes.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry<Item, Long> entry = iter.next();

                    if (now - entry.getValue() > 1_000_000_000L) { // remove after 1 sec
                        iter.remove();
                    }
                }

                // pause updates, if there are no entries left
                if (markTimes.isEmpty()) {
                    stop();
                }
            }
        };

        final Random random = new Random();

        Thread t = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    continue;
                }
                Platform.runLater(() -> {
                    updateValue(item, random.nextInt(4));
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
