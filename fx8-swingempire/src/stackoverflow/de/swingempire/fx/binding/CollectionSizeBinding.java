/*
 * Created on 13.08.2018
 *
 */
package de.swingempire.fx.binding;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Get notified when size of a List changes
 * https://stackoverflow.com/q/51811277/203657
 * 
 * Fabian suggests an InvalidationListener and keeping track on the current 
 * size manually.
 * 
 * Alternatives:
 * - use Bindings.size(data)
 * - wrap into a ListProperty and listen to its sizeProperty
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CollectionSizeBinding extends Application {

    int counter;
    private IntegerBinding iBind;
    
    private Parent createContent() {
        
        ObservableList<String> data = FXCollections.observableArrayList(
                Stream.generate(this::createItem)
                .limit(5)
                .collect(Collectors.toList())
                );
        
        ListProperty<String> listData = new SimpleListProperty<>(data);
        listData.sizeProperty().addListener((src, ov, nv) -> {
            System.out.println("listProperty changeListener: " + ov + "/" +  nv);
            
        });
        
        listData.sizeProperty().addListener(ov -> {
            System.out.println("listProperty invalidated: " + ov );
            
        });
        ListView<String> list = new ListView<>(listData);
        
        Label sizeBinding = new Label();
        sizeBinding.textProperty().bind(Bindings.size(data).asString());
        
        IntegerBinding iBind = Bindings.size(data);
        iBind.addListener((src, ov, nv) -> {
            System.out.println("bindings: " + ov + "/" +  nv);
        });
        Label sizeListProperty = new Label();
        sizeListProperty.textProperty().bind(listData.sizeProperty().asString());
        
        Button add = new Button("add");
        add.setOnAction(e -> data.add(createItem()));
        
        Button remove = new Button("remove");
        remove.setOnAction(e -> data.remove(list.getSelectionModel().getSelectedItem()));
        
        Button replace = new Button("replace selected");
        replace.setOnAction(e -> {
            if (!list.getSelectionModel().isEmpty()) 
                data.set(list.getSelectionModel().getSelectedIndex(), createItem());
        });
        BorderPane content = new BorderPane(list);
        content.setTop(new HBox(20, sizeBinding, sizeListProperty));
        content.setBottom(new HBox(10, add, remove, replace));
        return content;
    }

    private String createItem() {
        return "A" + counter++;
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
            .getLogger(CollectionSizeBinding.class.getName());

}
