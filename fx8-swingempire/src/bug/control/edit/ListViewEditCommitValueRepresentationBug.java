/*
 * Created on 05.09.2017
 *
 */
package control.edit;

import de.swingempire.fx.scene.control.cell.DebugTextFieldListCell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * suspected: listCell shows edited value even if commitHandler does nothing
 * to commit it back to the list
 *  
 * but okay ... cell always shows value of backing data, not the edited 
 * Why? Code in commitEdit looks very much the same. 
 * 
 * Debugging reveals:
 * 
 * - triggered layout pass
 * - VirtualFlow addTrailingCells
 * - VirualFlow setCellIndex
 * - indexedCell.indexChanged
 * - listCell updateItem(oldIndex) 
 * 
 * <ul> diffs
 * <li>ListCell listens to itemsList changes - but nothing changed, shouldn't be triggered
 * <li>
 */
public class ListViewEditCommitValueRepresentationBug extends Application {

    private int editIndex;

    @Override
    public void start(Stage primaryStage) {
        ListView<String> simpleList = new ListView<>(FXCollections
                .observableArrayList("Item1", "Item2", "Item3", "Item4"));
        simpleList.setEditable(true);
//        simpleList.setCellFactory(TextFieldListCell.forListView());
        simpleList.setCellFactory(DebugTextFieldListCell.forListView());

        simpleList.setOnEditCommit(e -> {
            editIndex = e.getIndex();
            System.out.println("doing nothing on " + e.getIndex());
        });
        
        Button button = new Button("print data at editIndex");
        button.setOnAction(e -> System.out.println("value at " + editIndex + " " + simpleList.getItems().get(editIndex)));
        BorderPane root = new BorderPane(simpleList);
        root.setBottom(button);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
