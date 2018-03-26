/*
 * Created on 13.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * Asked on SO:
 * 
 * http://stackoverflow.com/q/26913280/203657
 * 
 * Related issues:
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8089709
 * fixed the permutation craziness in ListView, introduced
 * violation of notification contract:
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8147852
 * still open!  
 * @author Jeanette Winzenburg, Berlin
 */
public class SelectedIndicesOnItemsModified extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // orig SO
        ObservableList<Integer> items = FXCollections.observableArrayList(1, 2, 3, 4); 
//        ObservableList<Integer> items = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6);
        TableView<Integer> table = new TableView<>(items);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // orig SO
        table.getSelectionModel().selectRange(2, 4);
//        table.getSelectionModel().selectIndices(2, 4, 5);
        System.out.println("indices before modification: " + table.getSelectionModel().getSelectedIndices());
        ListView<Integer> list = new ListView<>(items);
        // variant: use SimpleListSelectionModel
//        list.setSelectionModel(new SimpleListSelectionModel<>(list));
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // orig SO
        list.getSelectionModel().selectRange(2, 4);
//        list.getSelectionModel().selectIndices(2, 4, 5);
        
        new PrintingListChangeListener("TableView indices ", table.getSelectionModel().getSelectedIndices());        
        new PrintingListChangeListener("ListView indices ", list.getSelectionModel().getSelectedIndices());  
        items.add(0, 111);
//        items.removeAll(2, 4);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static <T> void prettyPrint(Change<? extends T> change) {
        StringBuilder sb = new StringBuilder("\tChange event data:\n");
        sb.append("\n " + change.getClass() + "\n " + change);
        int i = 0;
        change.reset();
        while (change.next()) {
            sb.append("\n\tcursor = ").append(i++).append("\n");

            final String kind = change.wasPermutated() ? "permutated" : change
                    .wasReplaced() ? "replaced"
                    : change.wasRemoved() ? "removed"
                            : change.wasAdded() ? "added"
                                    : change.wasUpdated() ? "updated" : "none";
            sb.append("\t\tKind of change: ").append(kind).append("\n");

            sb.append("\t\tAffected range: [").append(change.getFrom())
                    .append(", ").append(change.getTo()).append("]\n");

            if (kind.equals("added") || kind.equals("replaced")) {
                sb.append("\t\tAdded size: ").append(change.getAddedSize())
                        .append("\n");
                sb.append("\t\tAdded sublist: ")
                        .append(change.getAddedSubList()).append("\n");
            }

            if (kind.equals("removed") || kind.equals("replaced")) {
                sb.append("\t\tRemoved size: ").append(change.getRemovedSize())
                        .append("\n");
                sb.append("\t\tRemoved: ").append(change.getRemoved())
                        .append("\n");
            }

            if (kind.equals("permutated")) {
                StringBuilder permutationStringBuilder = new StringBuilder("[");
                for (int k = change.getFrom(); k < change.getTo(); k++) {
                    permutationStringBuilder.append(k).append("->")
                            .append(change.getPermutation(k));
                    if (k < change.getTo() - 1) {
                        permutationStringBuilder.append(", ");
                    }
                }
                permutationStringBuilder.append("]");
                String permutation = permutationStringBuilder.toString();
                sb.append("\t\tPermutation: ").append(permutation).append("\n");
            }
        }
        System.out.println(sb.toString());
    };

    public static class PrintingListChangeListener implements ListChangeListener {
        String source;
        int counter;
        public PrintingListChangeListener() {
        }
        
        public PrintingListChangeListener(String message, ObservableList<?> list) {
            list.addListener(this);
            source = message;
        }
        @Override
        public void onChanged(Change change) {
            System.out.println("Change #" + counter++ + " on " +source + "\nlist = " + change.getList());
            prettyPrint(change);
        }
    }

}
