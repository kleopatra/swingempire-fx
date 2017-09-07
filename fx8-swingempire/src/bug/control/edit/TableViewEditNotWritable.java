/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableViewCell: representation may be incorrect after commitEdit 
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187314
 */
public class TableViewEditNotWritable extends Application {

    TablePosition<Dummy, String> editPosition;
    private Object editValue;
    @Override
    public void start(Stage primaryStage) {
        TableView<Dummy> table = new TableView<>(Dummy.dummies());
        table.setEditable(true);

        TableColumn<Dummy, String> first = new TableColumn<>("Text");
        first.setCellFactory(TextFieldTableCell.forTableColumn());
        first.setCellValueFactory(new PropertyValueFactory<>("dummy"));

        first.setOnEditStart(t -> editPosition = t.getTablePosition());
        first.addEventHandler(first.editCommitEvent(), t -> {
            editValue = t.getNewValue();
            System.out.println("doing nothing");
            
        });

        table.getColumns().addAll(first);

        Button button = new Button("Check value");
        button.setOnAction(e -> {
            if (editPosition == null) return;
            String value = editPosition.getTableColumn().getCellObservableValue(editPosition.getRow()).getValue();
            System.out.println(
                    "value in edited cell must represent backing data: " + value + " not the edited " + editValue);
        });
        BorderPane root = new BorderPane(table);
        root.setBottom(button);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static class Dummy {
        private String dummy;
        public Dummy(String dummy) {
            this.dummy = dummy;
        }
        
        public String getDummy() {
            return dummy;
        }
        
        public static ObservableList<Dummy> dummies() {
            return FXCollections.observableArrayList(
                    new Dummy("1"), new Dummy("2"), new Dummy("3")
                    );
        }
    }
}
