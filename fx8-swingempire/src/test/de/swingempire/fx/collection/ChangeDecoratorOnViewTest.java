/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.collection;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static org.junit.Assert.*;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Here: try to test
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChangeDecoratorOnViewTest extends Application {

    public static class DecoTableRow<T> extends TableRow<T> {

        final PseudoClass marked = PseudoClass.getPseudoClass("marked");

        /**
         * Overridden to update the dirty marker visuals.
         * Note: need to do it here because updateItem is not called
         * for update changes.
         */
        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            boolean highlight = false;
            
            if (!isEmpty() 
                    && getTableView() != null 
                    && getTableView().getItems() instanceof ChangeDecorator 
                    && getItem() != null) {
                highlight = ((ChangeDecorator<T>) getTableView().getItems()).isChanged(getItem());
            }
            pseudoClassStateChanged(marked, highlight);
        }
        
    }
    
    protected void testChangedReset() {
        changeFirstName();
        List<ChangeDecorator<Person>.Marker<Person>> markers = decorator.markers;
        ChangeDecorator<Person>.Marker<Person> marker = markers.get(0);
        Person person = marker.getElement();
        marker.recentTimer.statusProperty().addListener((src, ov, nv) -> {
            assertEquals(false, marker.changedProperty().get());
            assertFalse(decorator.isChanged(person));
        });
    }
    
 
    protected void runTestTask(Runnable test, long sleep) {
        Task task = createTestTask(test, sleep);
        Thread th =  new Thread(task);
        th.setDaemon(true);
        th.start();
    }


    /**
     * @param test
     * @param sleep
     * @return
     */
    protected Task createTestTask(Runnable test, long sleep) {
        Task task = new Task<>() {

            @Override
            protected Object call() throws Exception {
                test.run();
                Thread.sleep(sleep);
                return "";
            }
       
          };
        return task;
    }
    
    protected void testChangedDuration() {
        long markerDuration = (long) decorator.markerDuration.toMillis();
        long allowedRange = 100; // arbitrary ... any better marker?
        long upper = markerDuration + allowedRange;
        changeFirstName();
        Instant now = Instant.now();
        List<ChangeDecorator<Person>.Marker<Person>> markers = decorator.markers;
        ChangeDecorator<Person>.Marker<Person> marker = markers.get(0);
        marker.changedProperty().addListener((src, ov, nv) -> {
            Instant later = Instant.now();
            long dur = now.until(later, ChronoUnit.MILLIS);
            boolean allowed = (dur > markerDuration) && (dur < upper);
            LOG.info("markerDuration/measured " + markerDuration + " allowed range " + allowedRange + " / " + dur);
            assertTrue("marker duration must be in range [" + markerDuration + ", " + upper + "] but was: " + dur, 
                    allowed);
        });
        
    }


    /**
     * 
     */
    protected void changeFirstName() {
        Person three = decorator.get(3);
        three.setFirstName(three.getFirstName() + "X");
    }

    List<Future> futures;
    private Parent createContent() {
        
        ObservableList<Person> persons = FXCollections.observableList(Person.persons()
                , e -> new Observable[] {e.firstNameProperty()});
        decorator = new ChangeDecorator<>(persons);
        decorator.addListener((ListChangeListener<Person>)c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
//                    LOG.info("dirty? " + decorator.isDirty(c.getList().get(c.getFrom()))  + c);
                }
            };
        });
        TableView<Person> table = new TableView<>(decorator);
        
        table.setRowFactory(c -> new DecoTableRow<>());
        
        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(p -> p.getValue().firstNameProperty());
        firstNameCol.getStyleClass().add("mark-column");
        
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(p -> p.getValue().lastNameProperty());
        
        table.getColumns().addAll(firstNameCol, lastNameCol);
        
        Button changeFirst = new Button("Test change isReset");
        changeFirst.setOnAction(e -> {
            runTestTask(() -> testChangedReset(), 2500);
        });
        
        Button changeOffFX = new Button("Test change duration");
        changeOffFX.setOnAction(e -> {
            runTestTask(() -> testChangedDuration(), 2500);
        });
        
        Button changeOnThread = new Button("Use executor");
        changeOnThread.setOnAction(e -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            futures = new ArrayList<>();
            createAllTasks().forEach(t -> futures.add(executor.submit(t)));
        });
        
        Button logFutures = new Button("log futures");
        logFutures.setOnAction(e -> LOG.info("futures: " + futures));
        table.getStylesheets().add(this.getClass().getResource("tablecolor.css").toExternalForm());

        HBox buttons = new HBox(10, changeFirst, changeOffFX, changeOnThread, logFutures);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }

    /**
     * @return
     */
    private List<Task> createAllTasks() {
        List<Task> tasks = List.of(
                createTestTask(() -> testChangedReset(), 2500)
                ,createTestTask(() -> testChangedDuration(), 1000)
                );
        return tasks;
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
            .getLogger(ChangeDecoratorOnViewTest.class.getName());
    private ChangeDecorator<Person> decorator;

}
