/*
 * Created on 25.03.2015
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.runtime.VersionInfo;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * https://javafx-jira.kenai.com/browse/RT-32620
 */
public class CheckBoxTreeCell_32620 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle(VersionInfo.getRuntimeVersion());
        stage.setScene(getScene());
        stage.show();
    }

    private Scene getScene() {
        
        List<TreeItem<Data>> ls = new ArrayList<TreeItem<Data>>(10);
        for (int i = 0; i < 10; i++) {
            ls.add(new TreeItem<Data>(new Data("Test " + i)));
        }
        
        TreeView<Data> tv = new TreeView<Data>();
        tv.setRoot(new TreeItem<Data>(new Data("Root")));
        
        tv.getRoot().getChildren().addAll(ls);
        tv.getRoot().setExpanded(true);
        tv.setEditable(true);
        
        tv.setCellFactory(CheckBoxTreeCell.forTreeView(new Callback<TreeItem<Data>, ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TreeItem<Data> param) {
                return new SimpleBooleanProperty(param.getValue().getValue().isEmpty());
            }
        }, new StringConverter<TreeItem<Data>>() {

            @Override
            public String toString(TreeItem<Data> object) {
                return object.getValue().getValue();
            }

            @Override
            public TreeItem<Data> fromString(String string) {
                return new TreeItem<Data>(new Data(string));
            }
        }));
        
        StackPane root = new StackPane();
        root.getChildren().add(tv);
        
        return new Scene(root, 600, 400);
    }
 
    public static class Data {
        private String value;

        public Data(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}