/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import de.swingempire.fx.scene.control.cell.TableCoreRecentlyChanged;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52519470/203657
 * highlight row with recently changed items.
 * 
 * the orig errors:
 * - static timer in cell (aka: shared by all cells)
 * - change off the fx app thread
 * 
 * Here fixing both, now does work but not optimal: 
 * a cell must not update its row!
 * 
 * @see TableCoreRecentlyChanged
 */
public class TableChangeMarker extends Application {

    private final ObservableList<Element> data = FXCollections.observableArrayList();

    public final Runnable changeValues = () -> {
        int i = 0;
        while (i <= 100000) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
//            data.get(0).setOccurence(System.currentTimeMillis());
            Platform.runLater(() -> {
                
                data.get(0).count();
            });
            i = i + 1;
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        return t;
    });

    @Override
    public void start(Stage primaryStage) {

        TableView<Element> table = new TableView<>();
        table.getStylesheets().add(this.getClass().getResource("tablecolor.css").toExternalForm());
        table.setEditable(true);

        TableColumn<Element, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(200);
        nameCol.setCellValueFactory(cell -> cell.getValue().nameProperty());
        nameCol.setCellFactory((TableColumn<Element, String> param) -> new ColorCounterTableCellRenderer(table));
        table.getColumns().add(nameCol);

        this.data.add(new Element());
        table.setItems(this.data);

        this.executor.submit(this.changeValues);

        Scene scene = new Scene(table, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class ColorCounterTableCellRenderer extends TableCell<Element, String> {

        private final static long MAX_MARKED_TIME = 3000;
        private final static long UPDATE_INTERVAL = 1000;

        // this was static ... don't you'll need one for each cell
        private Timer t = null;
        private final String highlightedStyle = "highlightedRow";

        private final TableView tv;

        public ColorCounterTableCellRenderer(TableView tv) {
            this.tv = tv;
            createTimer();
            setAlignment(Pos.CENTER_RIGHT);
        }

        private void createTimer() {
            if (t == null) {
                t = new Timer("Hightlight", true);
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        final long currentTime = System.currentTimeMillis();

                        TableRow tr = getTableRow();
                        if (tr != null && tr.getItem() != null) {

                            if (currentTime - ((Element) tr.getItem()).getOccurrenceTime() > MAX_MARKED_TIME) {
                                Platform.runLater(() -> {
                                    tr.getStyleClass().remove(highlightedStyle);
                                });
                            }
                        }
                    }
                }, 0, UPDATE_INTERVAL);
            }
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                setText(null);
                return;
            }

            long currentTime = System.currentTimeMillis();

            TableRow<Element> row = getTableRow();
            Element elementRow = row.getItem();

            double occurrenceTime = elementRow.getOccurrenceTime();

            if (currentTime - occurrenceTime < MAX_MARKED_TIME) {
                if (!row.getStyleClass().contains(highlightedStyle)) {
                    row.getStyleClass().add(highlightedStyle);
                }
            }

            super.updateItem(item, empty);
            setText(item + "");

        }
    }


    public static class Element {
        
        int x = 0;

        private final StringProperty nameProperty = new SimpleStringProperty("");

        private final AtomicReference<String> name = new AtomicReference<>();

        private final DoubleProperty occurence = new SimpleDoubleProperty();

        public void count() {
            x = x + 1;
            setOccurence(System.currentTimeMillis());
            nameProperty().set(Integer.toString(x));
//            if (name.getAndSet(Integer.toString(x)) == null) {
//                Platform.runLater(() -> nameProperty.set(name.getAndSet(null)));
//            }
        }

        public void setOccurence(double value) {
            occurence.set(value);
        }

        public String getName() {
            return nameProperty().get();
        }

        public void setName(String name) {
            nameProperty().set(name);
        }

        public StringProperty nameProperty() {
            return nameProperty;
        }

        public DoubleProperty occurenceProperty() {
            return occurence;
        }
        
        public double getOccurrenceTime() {
            return occurence.get();
        }
    }


}
