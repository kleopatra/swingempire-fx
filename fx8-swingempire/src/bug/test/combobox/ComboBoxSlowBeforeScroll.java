/*
 * Created on 11.04.2019
 *
 */
package test.combobox;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8222326
 * 
 * - initial showing nearly freezing
 * - open popup: slow
 * - move mouse over cell in popup: slow until scrolled a tiny bit
 * 
 * similar with standard cell, the handler is for emphasis on mouseOver slowness
 * 
 * Reporter: initial layout (every layout until scrolled) is slow because
 *   of measuring (?) cells - once scrolled, there's a single cell added to the
 *   pile which speeds up the measuring (?)
 * 
 * It's combo only, list is fine.
 * 
 * - hack around by limiting the # of cells to measure
 * - if not wanting to live with the limitation, remove the limit property again
 * - drawback: on replacing the cells, we are back to initial problem because the pile
 *   is cleared
 * 
 * commented the issue with the hack around (not the drawback ;) - and
 * the difference between accumCell and pile is that the former calls applyCss on each 
 * measuring round (whereas the latter is assumed to be styled)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxSlowBeforeScroll extends Application {

    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        ComboBox<String> comboBox = new ComboBox<>();
        // prevent inital bottleneck by limiting the # of cell to measure 
        comboBox.getProperties().put("comboBoxRowsToMeasureWidth", 10);
        comboBox.setOnHidden(e -> {
            // remove the limitation after first showing
            comboBox.getProperties().remove("comboBoxRowsToMeasureWidth");
        });
        comboBox.setEditable(true);
        comboBox.setItems(getItems());
        comboBox.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> arg0) {

                return new ListCell<String>() {
                    {
                        this.setOnMouseEntered((MouseEvent e) -> {
                            comboBox.getSelectionModel().select(this.getIndex());
                        });
                    }

                    @Override
                    protected void updateItem(String text, boolean empty) {
                        super.updateItem(text, empty);
                        setText(text);
                    }

                };
            }

        });

        Button replaceItems = new Button("replaceItems");
        // back to initial problem: pile is cleared, no matter whether via setAll or replacing
        replaceItems.setOnAction(e -> comboBox.setItems(getItems()));
        replaceItems.setOnAction(e -> comboBox.getItems().setAll(getItems()));
        ListView<String> list = new ListView<>(getItems());
        list.setCellFactory(
                new Callback<ListView<String>, ListCell<String>>() {
                    @Override
                    public ListCell<String> call(ListView<String> arg0) {

                        return new ListCell<String>() {
                            {
                                this.setOnMouseEntered((MouseEvent e) -> {
                                    list.getSelectionModel()
                                            .select(this.getIndex());
                                });
                            }

                            @Override
                            protected void updateItem(String text,
                                    boolean empty) {
                                super.updateItem(text, empty);
                                setText(text);
                            }

                        };
                    }

                });

        root.getChildren().addAll(
                comboBox
//                list
                , replaceItems
                );

        primaryStage.show();
    }

    public ObservableList<String> getItems() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (int i = 0; i < 50000; i++) {
            list.add("" + Math.random());
        }
        return list;
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
