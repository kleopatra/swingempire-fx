/*
 * Created on 09.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * SelectedItem was cleared on replacing the items.
 * 
 */
public class LookupApp_35039 extends Application {

    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        final Map<String,SomeBean> map = new HashMap<>();
        map.put("aabbaa", new SomeBean("aabbaa"));
        map.put("bbc", new SomeBean("bbc"));
        final ComboBox<SomeBean> combo = new ComboBox<>();
        combo.setEditable(true);
        combo.setItems(FXCollections.observableArrayList(map.values()));
        combo.setConverter(new StringConverter<LookupApp_35039.SomeBean>() {
            
            @Override
            public String toString(SomeBean bean) {
                if(bean != null) {
                    return bean.getId();
                } else {
                    return "";
                }
            }
            
            @Override
            public SomeBean fromString(String text) {
                return map.get(text);
            }
        });
        Button button = new Button("Refresh items");
        button.setOnAction(new EventHandler<ActionEvent>() {
            
            @Override
            public void handle(ActionEvent arg0) {
                combo.setItems(FXCollections.observableArrayList(map.values()));
            }
        });
        primaryStage.setScene(new Scene(new VBox(combo, button)));
        primaryStage.show();
    }

    public class SomeBean {
        
        private String id;
        
        public SomeBean(String id) {
            this.id = id;
        }
        
        public String getId() {
            return id;
        }
    }
}
