/*
 * Created on 13.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection.corefix709;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;

/**
 * Asked on SO:
 * 
 * http://stackoverflow.com/q/26913280/203657
 * 
 * Here we concentrate on presumed fix for ListView
 * https://bugs.openjdk.java.net/browse/JDK-8089709
 * 
 * Status:
 * - permutation replaced by added/removed changes
 * - sequence of subchanges incorrect: must be ordered by getFrom - reopened issue
 * - subchanges should be concatenated if possible: fix doesn't
 * 
 * Reopen isn't an option ;-) Filed another:
 * https://bugs.openjdk.java.net/browse/JDK-8147852
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SelectedIndicesOnItemsModified extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // orig SO
        ObservableList<String> items = FXCollections.observableArrayList("F1", "F2", "F3", "F4"); 
        // variant: more items
//        ObservableList<String> items = FXCollections.observableArrayList(
//                "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8");
        ListView<String> list = new ListView<>(items);
        // variant: use SimpleListSelectionModel
//        list.setSelectionModel(new SimpleListSelectionModel<>(list));
        // variant: use fixed core model - no longer needed, included in
        // 9-ea-108
//        list.setSelectionModel(new ListViewBitSelectionModelCorefix709<>(list));
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // orig SO
        list.getSelectionModel().selectRange(2, 4);
        // variant: disjoint selection
//        list.getSelectionModel().selectIndices(2, 4, 6);
        List<Integer> copy = new ArrayList<>();
        Bindings.bindContent(copy, list.getSelectionModel().getSelectedIndices());
        System.out.println("bound: " + copy);
        new PrintingListChangeListener("ListView indices ", list.getSelectionModel().getSelectedIndices());  
        items.add(0, "A111");
//        items.add(3, "A111");
//        items.set(4, "A111");
//        items.remove(3);
//        items.removeAll("F2", "F4");
//        System.out.println("list after mod: " + items);
        System.out.println("bound after: " + copy);
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
