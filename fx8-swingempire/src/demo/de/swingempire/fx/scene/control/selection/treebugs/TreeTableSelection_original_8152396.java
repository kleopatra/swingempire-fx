/*
 * Created on 15.10.2017
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

import java.util.Collection;
import java.util.Objects;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

/**
 * This here is the original example - seems to work.
 * 
 * Check why the adaption doesn't.
 * 
 * depends on count of items:  
 * passes with 2 items
 * 
 * fails on 5th expand with 3 items
 * fails on 3th expand with 4 items
 * identical stacktrace for exception on expand
 * 
 * fails on 1st collapse with >= 5 items
 * different stacktrace for exception on collapse
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableSelection_original_8152396 extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        final TreeItem<String> childNode1 = new TreeItem<>("Child Node 1");
        childNode1.getChildren().addAll(
            new TreeItem<String>("Node 1-1"),
            new TreeItem<String>("Node 1-2"),
//            new TreeItem<String>("Node 1-3"),
//            new TreeItem<String>("Node 1-4"),
//            new TreeItem<String>("Node 1-5"),
            new TreeItem<String>("Node 1-last")
        );

        final TreeItem<String> root = new TreeItem<>("Root node");
        root.setExpanded(true);
        root.getChildren().add(childNode1);

        TreeTableColumn<String,String> column = new TreeTableColumn<>("Column");
        column.setPrefWidth(190);
        column.setCellValueFactory((CellDataFeatures<String, String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue()));

        final TreeTableView<String> treeTableView = new TreeTableView<>(root);
        treeTableView.getColumns().add(column);
        treeTableView.setPrefWidth(200);
        treeTableView.setShowRoot(true);
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Select all children of expanded node.
        treeTableView.expandedItemCountProperty().addListener((observable, oldCount, newCount) -> {
            if (newCount.intValue() > oldCount.intValue()) {
                selectChildrenOfRows(treeTableView, treeTableView.getSelectionModel().getSelectedIndices());
            }
        });

        filteredList = treeTableView.getSelectionModel().getSelectedItems().filtered(Objects::nonNull);
        filteredList.addListener((ListChangeListener.Change<? extends TreeItem<String>> change) -> {
            System.out.printf("Change: %s\n", change);
        });

        final Scene scene = new Scene(new Group(), 200, 400);
        Group sceneRoot = (Group)scene.getRoot();
        sceneRoot.getChildren().add(treeTableView);

        stage.setTitle("Tree Table View Samples");
        stage.setScene(scene);
        stage.show();
    }

    private FilteredList<TreeItem<String>> filteredList;

    private void selectChildrenOfRows(TreeTableView<String> table, Collection<Integer> selectedRows) {
        for (int index: selectedRows) {
            TreeItem<String> item = table.getTreeItem(index);

            if (item != null && item.isExpanded() && !item.getChildren().isEmpty()) {
                int startIndex = index + 1;
                int maxCount   = startIndex + item.getChildren().size();

                table.getSelectionModel().selectRange(startIndex, maxCount);
            }
        }
    }
    
/* Stacktrace 4 items
Exception in thread "JavaFX Application Thread" java.lang.ArrayIndexOutOfBoundsException
        at java.base/java.lang.System.arraycopy(Native Method)
        at javafx.base/javafx.collections.transformation.FilteredList.addRemove(FilteredList.java:274)
        at javafx.base/javafx.collections.transformation.FilteredList.sourceChanged(FilteredList.java:144)
        at javafx.base/javafx.collections.transformation.TransformationList.lambda$getListener$0(TransformationList.java:106)
        at javafx.base/javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.SelectedItemsReadOnlyObservableList.lambda$new$1(SelectedItemsReadOnlyObservableList.java:103)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$Generic.fireValueChangedEvent(ListListenerHelper.java:329)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList._endChange(ReadOnlyUnbackedObservableList.java:63)
        at javafx.controls/javafx.scene.control.MultipleSelectionModelBase$SelectedIndicesList._endChange(MultipleSelectionModelBase.java:895)
        at javafx.controls/javafx.scene.control.ControlUtils.updateSelectedIndices(ControlUtils.java:202)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel.fireCustomSelectedCellsListChangeEvent(TreeTableView.java:3352)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.base/javafx.collections.transformation.SortedList.sourceChanged(SortedList.java:111)
        at javafx.base/javafx.collections.transformation.TransformationList.lambda$getListener$0(TransformationList.java:106)
        at javafx.base/javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.base/javafx.collections.ModifiableObservableListBase.addAll(ModifiableObservableListBase.java:102)
        at javafx.controls/com.sun.javafx.scene.control.SelectedCellsMap.addAll(SelectedCellsMap.java:146)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel.selectIndices(TreeTableView.java:2951)
        at javafx.controls/javafx.scene.control.MultipleSelectionModel.selectRange(MultipleSelectionModel.java:180)
        at de.swingempire.fx.scene.control.selection.TreeTableSelection_original_8152396.selectChildrenOfRows(TreeTableSelection_original_8152396.java:100)
        at de.swingempire.fx.scene.control.selection.TreeTableSelection_original_8152396.lambda$1(TreeTableSelection_original_8152396.java:72)
        at javafx.base/com.sun.javafx.binding.ExpressionHelper$Generic.fireValueChangedEvent(ExpressionHelper.java:360)
        at javafx.base/com.sun.javafx.binding.ExpressionHelper.fireValueChangedEvent(ExpressionHelper.java:80)
        at javafx.base/javafx.beans.property.ReadOnlyIntegerPropertyBase.fireValueChangedEvent(ReadOnlyIntegerPropertyBase.java:72)
        at javafx.base/javafx.beans.property.ReadOnlyIntegerWrapper.fireValueChangedEvent(ReadOnlyIntegerWrapper.java:102)
        at javafx.base/javafx.beans.property.IntegerPropertyBase.markInvalid(IntegerPropertyBase.java:114)
        at javafx.base/javafx.beans.property.IntegerPropertyBase.set(IntegerPropertyBase.java:148)
        at javafx.controls/javafx.scene.control.TreeTableView.setExpandedItemCount(TreeTableView.java:1126)
        at javafx.controls/javafx.scene.control.TreeTableView.updateExpandedItemCount(TreeTableView.java:1918)
        at javafx.controls/javafx.scene.control.TreeTableView.layoutChildren(TreeTableView.java:1510)

----------------

Stacktrace 3 items:

Exception in thread "JavaFX Application Thread" java.lang.ArrayIndexOutOfBoundsException
        at java.base/java.lang.System.arraycopy(Native Method)
        at javafx.base/javafx.collections.transformation.FilteredList.addRemove(FilteredList.java:274)
        at javafx.base/javafx.collections.transformation.FilteredList.sourceChanged(FilteredList.java:144)
        at javafx.base/javafx.collections.transformation.TransformationList.lambda$getListener$0(TransformationList.java:106)
        at javafx.base/javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.SelectedItemsReadOnlyObservableList.lambda$new$1(SelectedItemsReadOnlyObservableList.java:103)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$Generic.fireValueChangedEvent(ListListenerHelper.java:329)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList._endChange(ReadOnlyUnbackedObservableList.java:63)
        at javafx.controls/javafx.scene.control.MultipleSelectionModelBase$SelectedIndicesList._endChange(MultipleSelectionModelBase.java:895)
        at javafx.controls/javafx.scene.control.ControlUtils.updateSelectedIndices(ControlUtils.java:202)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel.fireCustomSelectedCellsListChangeEvent(TreeTableView.java:3352)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.base/javafx.collections.transformation.SortedList.sourceChanged(SortedList.java:111)
        at javafx.base/javafx.collections.transformation.TransformationList.lambda$getListener$0(TransformationList.java:106)
        at javafx.base/javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:482)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.base/javafx.collections.ModifiableObservableListBase.addAll(ModifiableObservableListBase.java:102)
        at javafx.controls/com.sun.javafx.scene.control.SelectedCellsMap.addAll(SelectedCellsMap.java:146)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel.selectIndices(TreeTableView.java:2951)
        at javafx.controls/javafx.scene.control.MultipleSelectionModel.selectRange(MultipleSelectionModel.java:180)
        at de.swingempire.fx.scene.control.selection.TreeTableSelection_original_8152396.selectChildrenOfRows(TreeTableSelection_original_8152396.java:100)
        at de.swingempire.fx.scene.control.selection.TreeTableSelection_original_8152396.lambda$1(TreeTableSelection_original_8152396.java:72)
        at javafx.base/com.sun.javafx.binding.ExpressionHelper$Generic.fireValueChangedEvent(ExpressionHelper.java:360)
        at javafx.base/com.sun.javafx.binding.ExpressionHelper.fireValueChangedEvent(ExpressionHelper.java:80)
        at javafx.base/javafx.beans.property.ReadOnlyIntegerPropertyBase.fireValueChangedEvent(ReadOnlyIntegerPropertyBase.java:72)
        at javafx.base/javafx.beans.property.ReadOnlyIntegerWrapper.fireValueChangedEvent(ReadOnlyIntegerWrapper.java:102)
        at javafx.base/javafx.beans.property.IntegerPropertyBase.markInvalid(IntegerPropertyBase.java:114)
        at javafx.base/javafx.beans.property.IntegerPropertyBase.set(IntegerPropertyBase.java:148)
        at javafx.controls/javafx.scene.control.TreeTableView.setExpandedItemCount(TreeTableView.java:1126)
        at javafx.controls/javafx.scene.control.TreeTableView.updateExpandedItemCount(TreeTableView.java:1918)
        at javafx.controls/javafx.scene.control.TreeTableView.layoutChildren(TreeTableView.java:1510)

----------------

Stacktrace 5 items (fails on collapse)
 
Exception in thread "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: Index: 3, Size: 1
        at java.base/java.util.AbstractList.rangeCheckForAdd(AbstractList.java:632)
        at java.base/java.util.AbstractList.listIterator(AbstractList.java:338)
        at javafx.base/javafx.collections.transformation.FilteredList.addRemove(FilteredList.java:257)
        at javafx.base/javafx.collections.transformation.FilteredList.sourceChanged(FilteredList.java:144)
        at javafx.base/javafx.collections.transformation.TransformationList.lambda$getListener$0(TransformationList.java:106)
        at javafx.base/javafx.collections.WeakListChangeListener.onChanged(WeakListChangeListener.java:88)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$SingleChange.fireValueChangedEvent(ListListenerHelper.java:164)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:524)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.SelectedItemsReadOnlyObservableList.lambda$new$1(SelectedItemsReadOnlyObservableList.java:103)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper$Generic.fireValueChangedEvent(ListListenerHelper.java:329)
        at javafx.base/com.sun.javafx.collections.ListListenerHelper.fireValueChangedEvent(ListListenerHelper.java:73)
        at javafx.base/javafx.collections.ObservableListBase.fireChange(ObservableListBase.java:233)
        at javafx.base/javafx.collections.ListChangeBuilder.commit(ListChangeBuilder.java:524)
        at javafx.base/javafx.collections.ListChangeBuilder.endChange(ListChangeBuilder.java:541)
        at javafx.base/javafx.collections.ObservableListBase.endChange(ObservableListBase.java:205)
        at javafx.controls/com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList._endChange(ReadOnlyUnbackedObservableList.java:63)
        at javafx.controls/javafx.scene.control.MultipleSelectionModelBase$SelectedIndicesList._endChange(MultipleSelectionModelBase.java:895)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel$3.handle(TreeTableView.java:2524)
        at javafx.controls/javafx.scene.control.TreeTableView$TreeTableViewArrayListSelectionModel$3.handle(TreeTableView.java:2440)
        at javafx.base/javafx.event.WeakEventHandler.handle(WeakEventHandler.java:79)
        at javafx.base/com.sun.javafx.event.CompositeEventHandler$WeakEventHandlerRecord.handleBubblingEvent(CompositeEventHandler.java:248)
        at javafx.base/com.sun.javafx.event.CompositeEventHandler.dispatchBubblingEvent(CompositeEventHandler.java:80)
        at javafx.base/com.sun.javafx.event.EventHandlerManager.dispatchBubblingEvent(EventHandlerManager.java:238)
        at javafx.base/com.sun.javafx.event.EventHandlerManager.dispatchBubblingEvent(EventHandlerManager.java:191)
        at javafx.base/com.sun.javafx.event.BasicEventDispatcher.dispatchEvent(BasicEventDispatcher.java:58)
        at javafx.base/com.sun.javafx.event.EventDispatchChainImpl.dispatchEvent(EventDispatchChainImpl.java:114)
        at javafx.base/com.sun.javafx.event.EventUtil.fireEventImpl(EventUtil.java:74)
        at javafx.base/com.sun.javafx.event.EventUtil.fireEvent(EventUtil.java:49)
        at javafx.base/javafx.event.Event.fireEvent(Event.java:198)
        at javafx.controls/javafx.scene.control.TreeItem.fireEvent(TreeItem.java:763)
        at javafx.controls/javafx.scene.control.TreeItem.access$200(TreeItem.java:205)
        at javafx.controls/javafx.scene.control.TreeItem$4.invalidated(TreeItem.java:562)
        at javafx.base/javafx.beans.property.BooleanPropertyBase.markInvalid(BooleanPropertyBase.java:110)
        at javafx.base/javafx.beans.property.BooleanPropertyBase.set(BooleanPropertyBase.java:145)
        at javafx.base/javafx.beans.property.BooleanProperty.setValue(BooleanProperty.java:81)
        at javafx.controls/javafx.scene.control.TreeItem.setExpanded(TreeItem.java:538)
        at javafx.controls/com.sun.javafx.scene.control.behavior.TreeTableCellBehavior.handleDisclosureNode(TreeTableCellBehavior.java:132)
        at javafx.controls/com.sun.javafx.scene.control.behavior.TableCellBehaviorBase.doSelect(TableCellBehaviorBase.java:126)
        at javafx.controls/com.sun.javafx.scene.control.behavior.CellBehaviorBase.mouseReleased(CellBehaviorBase.java:185)
        at javafx.controls/com.sun.javafx.scene.control.inputmap.InputMap.handle(InputMap.java:274)
     
 */
}
