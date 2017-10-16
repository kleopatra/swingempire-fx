/*
 * Created on 16.10.2017
 *
 */
package de.swingempire.fx.scene.control.selection.treebugs;

/**
 * failure stacktrace
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeTableSelection_8152396_4items {

/*
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

    
 */
}
