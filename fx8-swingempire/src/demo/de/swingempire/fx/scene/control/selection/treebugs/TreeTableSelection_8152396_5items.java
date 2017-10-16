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
public class TreeTableSelection_8152396_5items {

/*
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
