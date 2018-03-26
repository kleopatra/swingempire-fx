/*
 * Created on 20.04.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Random;

import com.sun.javafx.tk.Toolkit;

import de.swingempire.fx.util.ListChangeReport;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Performance issue when many indices are selected.
 * https://javafx-jira.kenai.com/browse/RT-39776
 * 
 * PENDING JW: 
 * - same in IndicesList! The bottleneck seems to be selectAll
 * - core is optimized for sequential access, random access still suffering
 */
public class ListViewSpeedTest extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        ToggleButton dirBtn = new ToggleButton("Iterate forward");
        dirBtn.setSelected(true);

        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < 40_000; i++) {
            items.add("item " + i);
        }
        ListView listView = new ListView(items);
        listView.setSelectionModel(new SimpleListSelectionModel<>(listView));
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button btn = new Button("Start test - selectAll and Access sequentially");
        btn.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();
            
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            
            if (dirBtn.isSelected()) {
                for (int i = 0, max = selectedIndices.size(); i < max; i++) {
                    sum += selectedIndices.get(i);
                }
            } else {
                for (int i = selectedIndices.size() -1; i >= 0; i--) {
                    sum += selectedIndices.get(i);
                }
            }
            
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });
        
        Button indexOf = new Button("Start test - selectAll and indexOf all");
        indexOf.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();
            
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            long intermediate = 0;
            if (dirBtn.isSelected()) {
                for (int i = 0, max = selectedIndices.size(); i < max; i++) {
                    sum += selectedIndices.indexOf(i);
                    long time = System.nanoTime();
                    long current = (time - startTime) / 1000000;
                    long delta = current - intermediate;
                    intermediate = current;
                    System.out.println("intermediate: " + current + "ms at index " + i);
                }
            } else {
                for (int i = selectedIndices.size() -1; i >= 0; i--) {
                    sum += selectedIndices.indexOf(i);
                    long time = System.nanoTime();
                    System.out.println("intermediate: " + ((time - startTime) / 1000000) + "ms " + sum);
                }
            }
            
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });
        
        Button random = new Button("Start test - selectAll and Access randomly");
        random.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();
            
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            Random rnd = new Random();
            int max = selectedIndices.size();
            for (int i = 0; i < max; i++) {
                int access = rnd.nextInt(max);
                sum += selectedIndices.get(access);
            }
            
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });
        
        Button last = new Button("Start test - selectAll and Access last");
        last.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();
            
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            int max = selectedIndices.size();
            for (int i = 0; i < max; i++) {
                sum += selectedIndices.get(max - 1);
            }
            
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });
        
        Button access = new Button("select last and get only");
        access.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().select(items.size() - 1);
            Toolkit.getToolkit().firePulse();

            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            int max = selectedIndices.size();
            for (int i = 0; i < max; i++) {
                sum += selectedIndices.get(0);
            }

            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
            
        });
        Button first = new Button("Start test - selectAll and Access first");
        first.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();
            
            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            int max = selectedIndices.size();
            for (int i = 0; i < max; i++) {
                sum += selectedIndices.get(1);
            }
            
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });
        
        Button clearAll = new Button("Start test - selectAll and clearAll");
        clearAll.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();

            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();
            ListChangeReport report = new ListChangeReport(selectedIndices);
            listView.getSelectionModel().clearSelection();
//            report.prettyPrint();
//            int max = selectedIndices.size();
//            for (int i = 0; i < max; i++) {
//                sum += selectedIndices.get(1);
//            }
//
            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
        });

        
        Button contains = new Button("contains");
        contains.setOnAction(e -> {
            int sum = 0;
            System.out.println("Start timing - " + listView.getSelectionModel().getClass().getSimpleName());
            long startTime = System.nanoTime();
            listView.getSelectionModel().selectAll();
            Toolkit.getToolkit().firePulse();

            ObservableList<Integer> selectedIndices = listView.getSelectionModel().getSelectedIndices();

            if (dirBtn.isSelected()) {
                for (int i = 0, max = items.size(); i < max; i++) {
                    if (selectedIndices.contains(i)) {
                        sum++;
                    }
                }
            } else {
                for (int i = items.size() -1; i >= 0; i--) {
                    if (selectedIndices.contains(i)) {
                        sum++;
                    }
                }
            }

            long endTime = System.nanoTime();
            System.out.println("duration: " + ((endTime - startTime) / 1000000) + "ms " + sum);
            
        });
        
        VBox vbox = new VBox(10, dirBtn, listView, btn, random, indexOf, contains, clearAll, access, last, first);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);

        stage.show();
    }
}
