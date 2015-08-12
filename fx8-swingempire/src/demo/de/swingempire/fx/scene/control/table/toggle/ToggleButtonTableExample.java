/*
 * Created on 12.08.2015
 *
 */
package de.swingempire.fx.scene.control.table.toggle;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.property.BugPropertyAdapters;
import de.swingempire.fx.scene.control.cell.ChoiceBoxTableCellDynamic;
import de.swingempire.fx.util.FXUtils;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ToggleButtonTableExample extends Application {

    public static class DataSelectionModel<S> extends SingleSelectionModel<S> {

        private ListProperty<S> listProperty;
        
        public DataSelectionModel(Property<ObservableList<S>> items) {
            //listProperty = BugPropertyAdapters.listProperty(items);
            listProperty = new SimpleListProperty<>();
            listProperty.bindBidirectional(items);
            ListChangeListener<S> itemsContentObserver = c -> {
                itemsChanged(c);
            };
            listProperty.addListener(itemsContentObserver);
        }
        
        protected void itemsChanged(Change<? extends S> c) {
            // TODO need to implement update on modificatins to the underlying list
        }

        @Override
        protected S getModelItem(int index) {
            if (index < 0 || index >= getItemCount()) return null;
            return listProperty.get(index);
        }

        @Override
        protected int getItemCount() {
            return listProperty.getSize();
        }
        
    }

    public static class ButtonCellX<S, T> extends TableCell<S, T> {

        private ToggleButton cellButton;
        private SingleSelectionModel<S> model;

        public ButtonCellX(SingleSelectionModel<S> group) {
            this.model = group;
            cellButton = new ToggleButton("click");
            cellButton.setOnAction(e -> updateToggle());
            updateToggle();
            setAlignment(Pos.CENTER);
        }

        protected void updateToggle() {
            model.select(cellButton.isSelected()? getIndex() : -1);
        }

        @Override
        protected void updateItem(T t, boolean empty) {
            super.updateItem(t, empty);
            if (empty) {
                setGraphic(null);
            } else {
                cellButton.setSelected(model.isSelected(getIndex()));
                setGraphic(cellButton);
            }
        }
    }

    private Parent getContent() {
        TableView<Person> table = new TableView<>();
        table.setItems(Person.persons());
        TableColumn<Person, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        SingleSelectionModel<Person> model = new DataSelectionModel<>(table.itemsProperty());
        TableColumn<Person, Boolean> toggle = new TableColumn<>("Preview");
        toggle.setCellFactory(c -> new ButtonCellX<Person, Boolean>(model));
        
        toggle.setCellValueFactory(f -> {
            Object value = f.getValue();
            return Bindings.equal(value, model.selectedItemProperty());
        });

        table.getColumns().addAll(name, toggle);
        
        Button select = new Button("Select 0");
        select.setOnAction(e -> {
            model.select(0);
        });
        VBox content = new VBox(10, table, select);
        return content;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ChoiceBoxTableCellDynamic.class.getName());

}
