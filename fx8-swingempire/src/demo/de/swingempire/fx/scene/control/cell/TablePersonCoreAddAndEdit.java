/*
 * Created on 06.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePersonCoreAddAndEdit extends Application {

    private Parent getContent() {
        TableView<Dummy> table = new TableView<>(
                FXCollections.observableArrayList(new Dummy(), new Dummy())
                );
        table.setEditable(true);
        
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        table.getColumns().addAll(column);
        
        
        Button addAndEdit = new Button("AddAndEdit");
        addAndEdit.setOnAction(e -> {
            int insertIndex = 1;
            table.getItems().add(insertIndex, new Dummy());
            table.edit(insertIndex,  column);
        });
        BorderPane content = new BorderPane(table);
        content.setBottom(addAndEdit);
        return content;
    }
    
    private static class Dummy {
        private static int count;
        StringProperty value = new SimpleStringProperty(this, "value", "initial " + count++);
        public StringProperty valueProperty() {return value;}
        public String getValue() {return valueProperty().get(); }
        public void setValue(String text) {valueProperty().set(text); }
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
