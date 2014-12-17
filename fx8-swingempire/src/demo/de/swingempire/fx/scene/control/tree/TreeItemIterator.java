/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.Iterator;
import java.util.Stack;

import javafx.scene.control.TreeItem;

/**
 * Answer by jewelsea
 * http://stackoverflow.com/a/26810381/203657
 * Iterate over items in a tree.
 * The tree should not be modified while this iterator is being used.
 *
 * @param <T> the type of items stored in the tree.
 */
public class TreeItemIterator<T> implements Iterator<TreeItem<T>> {
    private Stack<TreeItem<T>> stack = new Stack<>();

    public TreeItemIterator(TreeItem<T> root) {
        stack.push(root);
    }

    @Override
    public boolean hasNext() {
        return !stack.isEmpty();
    }

    @Override
    public TreeItem<T> next() {
        TreeItem<T> nextItem = stack.pop();
        nextItem.getChildren().forEach(stack::push);

        return nextItem;
    }
}