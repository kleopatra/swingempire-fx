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
 * This is a wrapper around the motivation api doc example of Cell. It contains
 * conditional customization of text color in updateItem. Doesn't work in
 * both directions:
 * - selected item is not shown as white
 * - sometimes, unselected item _is_ shown in white (select one, then scroll 
 * down)
 * 
 */
public class CellUpdateItemAndSelected extends Application {

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

    public Parent createContent() {
        Pane content = new VBox(10);
        ListView<Number> numberList = new ListView<>(createNumberData(20));
        // this is the motivating example from cell's api doc
        numberList.setCellFactory(cf -> {
            ListCell<Number> cell = new ListCell<Number>() {

                @Override
                protected void updateItem(Number item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null ? "" : item.toString());
                    if (item != null) {
                        double value = item.doubleValue();
                        boolean state = isSelected();
//                        boolean state = isFocused();
                        setTextFill(state ? Color.WHITE
                                : value == 0 ? Color.BLACK
                                        : value < 0 ? Color.RED : Color.GREEN);
                        if (state) {
                            LOG.info("selected index/item: " + getIndex() + " / " + item);
                        }
                    }
                }

                @Override
                public void updateSelected(boolean selected) {
                    super.updateSelected(selected);
                    updateItem(getItem(), isEmpty());
                }

                
            };
            return cell;
        });

        Label selectedLabel = new Label();
        selectedLabel.textProperty().bind(numberList.getSelectionModel().selectedIndexProperty().asString());
        content.getChildren().addAll(numberList, selectedLabel);
        return content;
    }

    protected ObservableList<Number> createNumberData(int count) {
        ObservableList<Number> data = FXCollections.observableArrayList();
        for (int i = -3; i < count; i++) {
            data.add(i);
        }
        return data;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CellUpdateItemAndSelected.class.getName());
}
