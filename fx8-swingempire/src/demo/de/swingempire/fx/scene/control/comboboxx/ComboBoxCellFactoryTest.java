/*
 * Created on 16.12.2015
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * http://stackoverflow.com/q/34316254/203657
 * 
 * selected cell without custom marker
 * 
 * PENDING JW: 
 * <li>exact contract of updateItem? It's called only if the
 * item/emptyness is changed (all concrete implementations), it's not called on
 * changes to the state of the cell (f.i. its selected/focued properties) 
 * <li>
 * motivating example in cell api doc has color depending on isSelected - looks
 * similar to this, 
 * <li>core implementations that have additional state (f.i.
 * CheckBoxXXCell) bind the state to a visual property of the view
 * 
 */
public class ComboBoxCellFactoryTest extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Parent content = createContent();
        Scene scene = new Scene(content, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    IntegerProperty created = new SimpleIntegerProperty();
    public Parent createContent() {
        Pane content = new VBox(10);

        ComboBox<String> combo = new ComboBox<String>();
        // ComboBoxX<String> combo = new ComboBoxX<String>();
        combo.setItems(FXCollections.observableArrayList("Item 1", "Item 2")); // ,
                                                                               // "Item 3",
                                                                               // "Item 4"));
        combo.getSelectionModel().selectLast();
        combo.setCellFactory(createCell(combo));

        ListView<String> list = new ListView<>();
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setItems(combo.getItems());
        list.setCellFactory(createCell(null));

        ListView<Number> numberList = new ListView<>(createNumberData(100));
        // this is the motivating example from cell's api doc
        numberList.setCellFactory(cf -> {
            ListCell<Number> cell = new ListCell<Number>() {

                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null ? "" : item.toString());
                    if (item != null) {
                        double value = item.doubleValue();
                        setTextFill(isSelected() ? Color.WHITE
                                : value == 0 ? Color.BLACK
                                        : value < 0 ? Color.RED : Color.GREEN);
                        if (isSelected()) {
                            LOG.info("selected: " + item);
                        }
                    }
                }

            };
            created.set(created.get() + 1);
            return cell;
        });

        Label createdLabel = new Label();
        createdLabel.textProperty().bind(created.asString());
        content.getChildren().addAll(numberList, createdLabel);
        return content;
    }

    protected ObservableList<Number> createNumberData(int count) {
        ObservableList<Number> data = FXCollections.observableArrayList(5, -1,
                1, 7, 5, 8, 0, -10);
        for (int i = 0; i < count; i++) {
            data.add(i);
        }
        return data;
    }

    protected Callback<ListView<String>, ListCell<String>> createCell(
            ComboBox<String> combo) {
        return new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> p) {
                return new ListCell<String>() {
                    private final Rectangle rectangle;
                    {
                        rectangle = new Rectangle(10, 10);
                        // Binding<Color> selectionColor =
                        // Bindings.when(selectedProperty())
                        // .then(Color.GREENYELLOW)
                        // .otherwise(Color.RED);
                        // rectangle.fillProperty().bind(selectionColor);
                        selectedProperty().addListener((c, ov, nv) -> {
                            // updateItem(getItem(), getItem() == null);
                            });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // boolean selected = combo.getValue().equals(item);
                            boolean selected = isSelected();
                            // boolean selected = isFocused();
                            rectangle.setFill(selected ? Color.GREENYELLOW
                                    : Color.RED);
                            setGraphic(rectangle);
                            setText(item);
                            LOG.info("update: " + item + " / " + selected);
                        }
                    }
                };
            }
        };
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboBoxCellFactoryTest.class.getName());
}
