/*
 * Created on 07.03.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * CheckBoxTableCell binds only to observable of type BooleanProperty, not 
 * of type ObjectProperty<Boolean>. The latter is doc'ed and intended (?), its
 * failure is unexpected.
 * 
 * https://stackoverflow.com/q/49154386/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CheckBoxCellBinding extends Application {

    private static final int NUM_ELEMENTS = 10;

    private final TableView<ExampleBean> table = new TableView<>();

    private final ObservableList<ExampleBean> data = FXCollections.observableArrayList();

    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) {
        final Scene scene = new Scene(new Group());
        stage.setTitle("Table View Sample");
        stage.setWidth(300);
        stage.setHeight(500);

        final TableColumn<ExampleBean, Boolean> c1 = new TableColumn<>("A");
        c1.setCellValueFactory(new PropertyValueFactory<ExampleBean, Boolean>("p1"));
//        c1.setCellFactory(CheckBoxTableCell.forTableColumn(c1));
        c1.setCellFactory(c -> new FixedCheckBoxTableCell<>());
        c1.setEditable(true);
        c1.setPrefWidth(100);


        for (int i = 0; i < NUM_ELEMENTS; i++) {
            data.add(new ExampleBean());
        }

        final ScrollPane sp = new ScrollPane();
        sp.setContent(table);
        sp.setMaxHeight(Double.POSITIVE_INFINITY);
        sp.setMaxWidth(Double.POSITIVE_INFINITY);
        sp.setFitToHeight(true);
        sp.setFitToWidth(true);

        table.setEditable(true);
        table.setItems(data);
        // table.setMaxHeight(Double.POSITIVE_INFINITY);
        // table.setMaxWidth(Double.POSITIVE_INFINITY);
        table.getColumns().addAll(c1);

        final ContextMenu cm = new ContextMenu();
        cm.getItems().add(new MenuItem("bu"));
        table.setContextMenu(cm);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        VBox.setVgrow(sp, Priority.ALWAYS);
        vbox.getChildren().addAll(sp);

        scene.setRoot(vbox);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Quick hack around: use a callback that adapts ObjectProperty<Boolean> to BooleanProperty
     * as expected by super.
     *  
     * @author Jeanette Winzenburg, Berlin
     */
    public static class FixedCheckBoxTableCell<S, T> extends CheckBoxTableCell<S, T> {

        @Override
        public void updateItem(T item, boolean empty) {
            checkCallBack();
            super.updateItem(item, empty);
        }

        private void checkCallBack() {
            if (getSelectedStateCallback() != null) return;
            ObservableValue<Boolean> observable = 
                    (ObservableValue<Boolean>) getTableColumn().getCellObservableValue(getIndex());
            // handled by super
            if (observable instanceof BooleanProperty) return;
            // can't bidi-bind anyway
            if (!(observable instanceof Property)) return;
            // getting here if we have a ObjectProperty<Boolean>, that's not handled by super
            setSelectedStateCallback(index -> {
                ObjectProperty<Boolean> p = (ObjectProperty<Boolean>) getTableColumn().getCellObservableValue(index);
                return BooleanProperty.booleanProperty(p);
            });
        }
        
        
    }

    public static class ExampleBean {

        private ObjectProperty<Boolean> p1;

        // private BooleanProperty p1;
        private ObjectProperty<String> p2;

        public ExampleBean() {
            p1 = new SimpleObjectProperty<>(true);
            // p1 = new SimpleBooleanProperty(true);
            p1.addListener((o, ov, nv) -> {
                System.err.println("Value changed " + ov + " -> " + nv);
            });

            p2 = new SimpleObjectProperty(true);
            p2.addListener((o, ov, nv) -> {
                System.err.println("Value changed " + ov + " -> " + nv);
            });
        }

        public final ObjectProperty<Boolean> p1Property() {
            return this.p1;
        }
        // public final BooleanProperty p1Property() {
        // return this.p1;
        // }
    }
}

