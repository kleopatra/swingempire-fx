/*
 * Created on 22.01.2018
 *
 */
package de.swingempire.fx.scene.control.edit;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 * 
 * SO_FAQ
 */
public class TableEditWithoutProperties extends Application {
    
    /**
     * A bean exposing plain fields is always read-only, even if it
     * has setters!
     * 
     * Similar to crappy example from tutorial (which even uses internally
     * properties).
     */
    public static class SimpleBean {
        private String name;
        
        public SimpleBean(String name) {
            setName(name);
        }
        
        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return getName();
        }
        
        public static ObservableList<SimpleBean> beans() {
            return FXCollections.observableArrayList(
                    new SimpleBean("first"), new SimpleBean("second"), new SimpleBean("other"));
        }
    }
    
    /**
     * expose property to make Table handle edits automagically.
     */
    public static class SimpleBeanP {
        private StringProperty name;
        
        public SimpleBeanP(String name) {
            setName(name);
        }

        /**
         * @return the name
         */
        public String getName() {
            return nameProperty().get();
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            nameProperty().set(name);
        }
        
        public StringProperty nameProperty() {
            if (name == null) {
                name =  new SimpleStringProperty(this, "name");
            }
            return name;
        }
        
        @Override
        public String toString() {
            return getName();
        }

        public static ObservableList<SimpleBeanP> beans() {
            return FXCollections.observableArrayList(
                    new SimpleBeanP("first"), new SimpleBeanP("second"), new SimpleBeanP("other"));
        }
    }

    /**
     * @return
     */
    private Parent getContent() {
        TableView<SimpleBean> table = new TableView(SimpleBean.beans());
        table.setEditable(true);
        TableColumn<SimpleBean, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        
        TableColumn<SimpleBean, String> shadow = new TableColumn<>("Name (copy)");
        shadow.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.getColumns().addAll(nameColumn, shadow);
        
        Button log = new Button("print content");
        log.setOnAction(e -> System.out.println(table.getItems()));
        BorderPane pane = new BorderPane(table);
        pane.setBottom(log);
        return pane;
    }
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
