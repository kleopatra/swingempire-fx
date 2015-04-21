/*
 * Created on 21.04.2015
 *
 */
package de.swingempire.fx.collection;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Example of how to implement a custom ObservableList.
 * http://stackoverflow.com/a/29773137/203657
 * 
 * Here: an immutable and unmodifiable (in itself) list containing distinct
 * values of properties of elements in a backing list, the values are extracted
 * via a function 
 */
public class DistinctMapperDemo extends Application {

    int categoryCount;
    private Parent getContent() {
        ObservableList<DemoData> data = FXCollections.observableArrayList(
                new DemoData("first", "some"),
                new DemoData("second", "some"),
                new DemoData("first", "other"),
                new DemoData("dup", "other"),
                new DemoData("dodo", "next"),
                new DemoData("getting", "last")
                
                );
        TableView<DemoData> table = new TableView<>(data);
        TableColumn<DemoData, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<DemoData, String> cat = new TableColumn<>("Category");
        cat.setCellValueFactory(new PropertyValueFactory<>("category"));
        table.getColumns().addAll(name, cat);
        
        Function<DemoData, String> mapper = c -> c.categoryProperty().get();
        ObservableList<String> mapped = new DistinctMappingList<>(data, mapper);
        ListView<String> cats = new ListView<>(mapped);
        
        Button remove = new Button("RemoveSelected DemoData");
        remove.setOnAction(e -> {
            int selected = table.getSelectionModel().getSelectedIndex(); 
            if (selected <0) return;
            data.remove(selected);
        });
        
        Button createNewCategory = new Button("Create DemoData with new Category");
        createNewCategory.setOnAction(e -> {
            String newCategory = data.size() == 0 ? "some" + categoryCount : 
                data.get(0).categoryProperty().get() + categoryCount;
            data.add(new DemoData("name" + categoryCount, newCategory));
            categoryCount++;
        });
        VBox buttons = new VBox(remove, createNewCategory);
        HBox box = new HBox(table, cats, buttons);
        return box;
    }

    public static class DistinctMappingList<V, E> extends ObservableListBase<E> {

        private List<E> mapped;
        private Function<V, E> mapper;

        public DistinctMappingList(ObservableList<V> source, Function<V, E> mapper) {
            this.mapper = mapper;
            mapped = applyMapper(source); 
            ListChangeListener l = c -> sourceChanged(c);
            source.addListener(l);
        }

        private List<E> applyMapper(List<? extends V> list) {
            List<E> backing = list.stream().map(p -> mapper.apply(p)).distinct()
                    .collect(Collectors.toList());
            return backing;
        }
        
        private void sourceChanged(Change<? extends V> c) {
            beginChange();
            List<E> backing = applyMapper(c.getList());
            while(c.next()) {
                if (c.wasAdded()) {
                    wasAdded(c, backing);
                } else if (c.wasRemoved()) {
                    wasRemoved(c, backing);
                } else {
                    // throw just for the example
                    throw new IllegalStateException("unexpected change " + c);
                }
            }
            endChange();
        }

        private void wasRemoved(Change<? extends V> c, List<E> backing) {
            List<E> removedCategories = applyMapper(c.getRemoved());
            for (E e : removedCategories) {
                if (!backing.contains(e)) {
                    int index = indexOf(e);
                    mapped.remove(index);
                    nextRemove(index, e);
                }
            }
        }

        private void wasAdded(Change<? extends V> c, List<E> backing) {
            List<E> addedCategories = applyMapper(c.getAddedSubList());
            for (E e : addedCategories) {
                if (!contains(e)) {
                    int last = size();
                    mapped.add(e);
                    nextAdd(last, last +1);
                }
            }
        }

        @Override
        public E get(int index) {
            return mapped.get(index);
        }

        @Override
        public int size() {
            return mapped.size();
        }
        
    }
    
    public static class DemoData {
        StringProperty name = new SimpleStringProperty(this, "name");
        StringProperty category = new SimpleStringProperty(this, "category");
        
        public DemoData(String name, String category) {
            this.name.set(name);
            this.category.set(category);
        }
        
        public StringProperty nameProperty() {
            return name;
        }
        
        public StringProperty categoryProperty() {
            return category;
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
