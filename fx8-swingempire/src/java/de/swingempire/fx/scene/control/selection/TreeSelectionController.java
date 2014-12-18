/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import de.swingempire.fx.collection.IndexMappedList;
import de.swingempire.fx.collection.TreeIndexMappedList;
import de.swingempire.fx.collection.TreeIndicesList;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSelectionController<T> implements IndexedItemsController<TreeItem<T>> {

    protected TreeIndicesList<T> indicesList;
    protected TreeIndexMappedList<T> indexedItems;

    public TreeSelectionController(TreeView<T> items) {
        indicesList = new TreeIndicesList<>(items);
        indexedItems = new TreeIndexMappedList<>(indicesList);
    }

    @Override
    public ObservableList<Integer> getIndices() {
        return indicesList;
    }

    @Override
    public ObservableList<TreeItem<T>> getIndexedItems() {
        return indexedItems;
    }

    @Override
    public void setIndices(int... indices) {
        indicesList.setIndices(indices);
    }

    @Override
    public void addIndices(int... indices) {
        indicesList.addIndices(indices);
    }

    @Override
    public void clearIndices(int... indices) {
        indicesList.clearIndices(indices);
    }

    @Override
    public void setAllIndices() {
        indicesList.setAllIndices();
    }

    @Override
    public void clearAllIndices() {
        indicesList.clearAllIndices();
    }

    @Override
    public int sourceIndexOf(TreeItem<T> item) {
        return indicesList.getSource().getRow(item);
    }

    @Override
    public int getSourceSize() {
        return indicesList.getSource().getExpandedItemCount();
    }

    @Override
    public TreeItem<T> getSourceItem(int index) {
        return indicesList.getSource().getTreeItem(index);
    }

//    @Override
//    public ObservableList<? extends TreeItem<T>> getSource() {
//        // TODO Auto-generated method stub
//        return null;
//    }


}
