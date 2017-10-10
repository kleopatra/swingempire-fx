/*
 * Created on 05.10.2017
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/46563432/203657
 * bind scrollbar values of scrollPane and tableView
 * 
 * different scaling: in scrollPane normalized to 1.0 max, in tableView raw
 * pixels. Setting value is not forced to constraints, but pane is. That is any scrolling
 * in table moves pane to its upper boundary, any scrolling in pane moves table
 * to lower boundary
 * 
 * so can't use bidi-binding, but need to scale
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SyncScrollBarsOnTableAndScrollPane extends Application {
    private Scene scene;
    private VBox root;
    private ScrollPane scPane;
    private static final int N_COLS = 10;
    private static final int N_ROWS = 10;
    private Button button;
    private ScrollBar scrollBarInPane;
    private ScrollBar scrollBarInTable;

    public void start(Stage stage) throws Exception {

        root = new VBox();
        scPane = new ScrollPane();
        Label lbl = new Label("Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table Dynamic Table");
        scPane.setContent(lbl);

        root.getChildren().add(scPane);

        TestDataGenerator dataGenerator = new TestDataGenerator();

        TableView<ObservableList<String>> tableView = new TableView<>();

        // add columns
        List<String> columnNames = dataGenerator.getNext(N_COLS);
        for (int i = 0; i < columnNames.size(); i++) {
            final int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(
                    columnNames.get(i)
            );
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
            tableView.getColumns().add(column);
        }

        // add data
        for (int i = 0; i < N_ROWS; i++) {
            tableView.getItems().add(
                    FXCollections.observableArrayList(
                            dataGenerator.getNext(N_COLS)
                    )
            );
        }

        root.getChildren().add(tableView);

        tableView.setPrefHeight(200);

        button = new Button("Delete");
        button.setOnAction(event -> { tableView.getItems().clear();});

        Button logMax = new Button("log max");
        logMax.setOnAction(e -> {
            LOG.info(
                    "max pane/table " + scrollBarInPane.getMax() + " / "+ scrollBarInTable.getMax() 
               + "\n"+     "val pane/table " + scrollBarInPane.getValue() + " / "+ scrollBarInTable.getValue()
            
                    );
            
        });
        Button setMax = new Button("set max");
        setMax.setOnAction(e -> {
            scrollBarInPane.setMax(scrollBarInTable.getMax());
        });
        root.getChildren().addAll(button, logMax, setMax);

        Scene scene = new Scene(root, 600, 300);
        stage.setScene(scene);
        stage.setTitle(FXUtils.version());
        stage.show();

        for(Node node1: scPane.lookupAll(".scroll-bar"))
        {
            if(node1 instanceof ScrollBar)
            {
                scrollBarInPane = (ScrollBar)node1;
                if(scrollBarInPane.getOrientation() == Orientation.HORIZONTAL)
                {
                    for(Node node2: tableView.lookupAll(".scroll-bar"))
                    {
                        if(node2 instanceof ScrollBar)
                        {
                            scrollBarInTable = (ScrollBar)node2;
                            if(scrollBarInTable.getOrientation() == Orientation.HORIZONTAL)
                            {
                                bindScrollBarValues(scrollBarInTable, scrollBarInPane);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * @param scrollBarInPane 
     * @param scrollBarInTable 
     * 
     */
    protected void bindScrollBarValues(ScrollBar scrollBarInTable, ScrollBar scrollBarInPane) {
        // can't use bidi-binding because bar in scrollPane is normalized, bar in table is not
        // scrollBarInTable.valueProperty().bindBidirectional(scrollBarInPane.valueProperty());
        // scale manually
        scrollBarInTable.valueProperty().addListener((src, ov, nv) -> {
            double tableMax = scrollBarInTable.getMax();
            scrollBarInPane.setValue(nv.doubleValue() / tableMax);
        });
        
        scrollBarInPane.valueProperty().addListener((src, ov, nv) -> {
            double tableMax = scrollBarInTable.getMax();
            scrollBarInTable.setValue(nv.doubleValue() * tableMax);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class TestDataGenerator {
        private static final String[] LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc tempus cursus diam ac blandit. Ut ultrices lacus et mattis laoreet. Morbi vehicula tincidunt eros lobortis varius. Nam quis tortor commodo, vehicula ante vitae, sagittis enim. Vivamus mollis placerat leo non pellentesque. Nam blandit, odio quis facilisis posuere, mauris elit tincidunt ante, ut eleifend augue neque dictum diam. Curabitur sed lacus eget dolor laoreet cursus ut cursus elit. Phasellus quis interdum lorem, eget efficitur enim. Curabitur commodo, est ut scelerisque aliquet, urna velit tincidunt massa, tristique varius mi neque et velit. In condimentum quis nisi et ultricies. Nunc posuere felis a velit dictum suscipit ac non nisl. Pellentesque eleifend, purus vel consequat facilisis, sapien lacus rutrum eros, quis finibus lacus magna eget est. Nullam eros nisl, sodales et luctus at, lobortis at sem.".split(" ");

        private int curWord = 0;

        List<String> getNext(int nWords) {
            List<String> words = new ArrayList<>();

            for (int i = 0; i < nWords; i++) {
                if (curWord == Integer.MAX_VALUE) {
                    curWord = 0;
                }

                words.add(LOREM[curWord % LOREM.length]);
                curWord++;
            }

            return words;
        }
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SyncScrollBarsOnTableAndScrollPane.class.getName());
}

