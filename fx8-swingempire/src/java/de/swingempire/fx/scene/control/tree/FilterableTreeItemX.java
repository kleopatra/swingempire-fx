/*
 * Created on 16.06.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import de.swingempire.fx.collection.FilteredListX;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 * PENDING JW: doesn't compile in jdk9 - dig into problem? 
 * Didn't in 8 - ignore for now.
 * 
 * Implement a TreeItem with a filterList containing the children.
 * Applied the approach from:
 * http://www.kware.net/?p=204
 * 
 * to TreeItemX (need to use SimpleTreeSelectionModel!). Not working: throwing IllegalState when modifying 
 * the backing children/predicate even if nothing selected. Need to dig out why we get an
 * unexpected state.
 * 
 * Working with FilteredListX (which is using a fine-grained notification on refilter), so might
 * relax the IllegalState - add tests with FilteredList to the suit and see which changes we
 * actually get from it on refilter.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FilterableTreeItemX<T> extends TreeItemX<T> {

    private ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<TreeItemPredicate<T>>();

    
    /**
     * 
     */
    public FilterableTreeItemX() {
        super();
//        checkChildren();
    }

    /**
     * @param value
     * @param graphic
     */
    public FilterableTreeItemX(T value, Node graphic) {
        super(value, graphic);
//        checkChildren();
    }

    /**
     * @param value
     */
    public FilterableTreeItemX(T value) {
        super(value);
//        checkChildren();
    }

    /**
     * @return the predicate property
     */
    public final ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
        return this.predicate;
    }

    /**
     * @return the predicate
     */
    public final TreeItemPredicate<T> getPredicate() {
        return this.predicate.get();
    }

    /**
     * Set the predicate
     * 
     * @param predicate the predicate
     */
    public final void setPredicate(TreeItemPredicate<T> predicate) {
        this.predicate.set(predicate);
    }

    /**
     * Returns the source list of children.
     * 
     * @return
     */
    public ObservableList<TreeItem<T>> getBackingChildren() {
        return (ObservableList<TreeItem<T>>) getChildren().getSource();
    }

    /**
     * Overridden to cast to filteredList.
     */
    @Override
    public FilteredListX<TreeItem<T>> getChildren() {
        return (FilteredListX<TreeItem<T>>) super.getChildren();
    }

    @Override
    protected ObservableList<TreeItem<T>> createChildrenList() {
        ObservableList<TreeItem<T>> internal = super.createChildrenList();
        FilteredListX<TreeItem<T>> filteredList = new FilteredListX(internal);
        installPredicateBinding(filteredList);
        return filteredList;
    }

    /**
     * @param filteredList
     */
    private void installPredicateBinding(FilteredListX<TreeItem<T>> filteredList) {
        filteredList.predicateProperty().bind(Bindings.createObjectBinding(
                () -> {
                    return child -> {
                            // Set the predicate of child items to force filtering
                            if (child instanceof FilterableTreeItemX) {
                                    FilterableTreeItemX<T> filterableChild = (FilterableTreeItemX<T>) child;
                                    filterableChild.setPredicate(this.predicate.get());
                            }
                            // If there is no predicate, keep this tree item
                            if (this.predicate.get() == null)
                                    return true;
                            // If there are children, keep this tree item
                            if (child.getChildren().size() > 0)
                                    return true;
                            // Otherwise ask the TreeItemPredicate
                            return this.predicate.get().test(this, child.getValue());
                    };
                }, 
                this.predicate)
        );

    }


}
