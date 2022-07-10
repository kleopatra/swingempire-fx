/*
 * Created 23.04.2022
 */

package de.swingempire.fx.scene.control.table;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/71974953/203657
 * memory on clicking == replace tableView with new
 * 
 * usage error - tight loop, don't know the exact reason but .. working
 * with task and animationTimer, also memory freed correct with explicit gc
 */
public class MemoryLeakReplaceTable extends Application {

    private static final int LARGE_MEM_BYTES = 50_000_000;

    public  void runGc() {
        printMem("before gc");
        System.gc();
        sleep(2000);
        printMem("after gc");
//        Object weak = weakTab != null ? weakTab.get() : null;
//        System.out.println("weakRef: " + weak);
    }
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            e.printStackTrace(); // ignore
        }
    }

    public static void printMem(String message) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        System.out.println(message + "\t - used mem: " + (totalMemory - freeMemory) + " MB");
    }


    VBox vBox;

    private PersonTableView personTableView = null;

    private void replaceTable() {
        removeTable();

        addTable();
    }
    private void removeTable() {
        printMem("before adding table");
        if (personTableView != null) {
            vBox.getChildren().remove(personTableView);
        }
    }
    private void addTable() {
        personTableView = new PersonTableView();
        vBox.getChildren().add(personTableView);
        VBox.setVgrow(personTableView, Priority.ALWAYS);
        printMem("after adding table");
    }

    private void startTask() {
        Task<Void> replaceTask = new Task<>() {

            int size;

            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < 1_000; i++) {

                    // FIXME: read access from background thread
                    size = vBox.getChildren().size();
                    Platform.runLater(() -> {
                        removeTable();
                    });
                    while (size == vBox.getChildren().size()) {
                        System.out.println("waiting");
                        Thread.sleep(10);
                    }
                    size = vBox.getChildren().size();
                    System.out.println(i + "after remove: " + size);
                    Platform.runLater(() -> {
                        addTable();
                    });
                    while (size == vBox.getChildren().size()) {
                        System.out.println("waiting");
                        Thread.sleep(10);
                    }
                    size = vBox.getChildren().size();
                    System.out.println( i + " after add: " + size);
                }
                return null;
            }

        };
        new Thread(replaceTask).start();

    }
    @Override
    public void start(Stage stage) {
        vBox = new VBox();

        Button startReplaceTask = new Button("start task");
        startReplaceTask.setOnAction(e -> {
            startTask();
        });

        Button animation = new Button("use animation");
        animation.setOnAction(e -> {
            AnimationTimer ann = new AnimationTimer() {

                @Override
                public void handle(long now) {
                    replaceTable();
                }

            };
            ann.start();
        });

        Button clickMe = new Button("Replace table");
        clickMe.setOnAction(e -> {
            for (int i = 0; i < 1_000; i++) {
                for (int j = 0; j < 1000; j++) {
                    replaceTable();
//                    System.gc();
                }

            }
        });

        Button runGC = new Button("GC");
        runGC.setOnAction(e -> {
            runGc();
        });

        vBox.getChildren().addAll(clickMe, startReplaceTask, animation, runGC);
        replaceTable();
        stage.setTitle("HelloFX");
        stage.setScene(new Scene(vBox, 300, 200));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class Person {
        final StringProperty firstNameProperty = new SimpleStringProperty("");
        final StringProperty lastNameProperty = new SimpleStringProperty("");

        Person(String l, String f) {
            lastNameProperty.set(l);
            firstNameProperty.set(f);
        }
    }

    public static class PersonTableView extends TableView<Person> {

        static long counter;
        protected TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last");
        protected TableColumn<Person, String> firstNameColumn = new TableColumn<>("First" + counter++);

        PersonTableView() {
            getColumns().add(firstNameColumn);
            getColumns().add(lastNameColumn);

            lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty);
            firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty);

            setUserData(new byte[LARGE_MEM_BYTES]);
        }
    }
}

