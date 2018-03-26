/*
 * Created on 18.12.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import de.swingempire.fx.collection.TreeIndexMappedList;
import de.swingempire.fx.collection.TreeIndicesList;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;


/**
 * Implementation of IndexedItemsController that handles a TreeView. Unfortunately, 
 * a treeView doesn't have a model and is needed for flattening the hierarchical 
 * structure of its <code>TreeItem</code>s into a sequential, indexable data
 * structure.
 * <p>
 * PENDING JW:
 * <li> root changes 
 * <li> changes to showRoot property not handled     
 * 
 * <p>
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSelectionController<T> implements IndexedItemsController<TreeItem<T>> {

    protected TreeIndicesList<T> indicesList;
    protected TreeIndexMappedList<T> indexedItems;

    public TreeSelectionController(TreeView<T> tree) {
        indicesList = new TreeIndicesList<>(tree);
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

}
