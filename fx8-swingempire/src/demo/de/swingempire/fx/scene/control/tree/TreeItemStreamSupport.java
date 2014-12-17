/*
 * Created on 17.12.2014
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javafx.scene.control.TreeItem;

/**
 * Answer by jewelsea
 * http://stackoverflow.com/a/26810381/203657
 * Provide a stream of tree items from a root tree item.
 */
public class TreeItemStreamSupport {
    public static <T> Stream<TreeItem<T>> stream(TreeItem<T> rootItem) {
        return asStream(new TreeItemIterator<>(rootItem));
    }

    private static <T> Stream<TreeItem<T>> asStream(TreeItemIterator<T> iterator) {
        Iterable<TreeItem<T>> iterable = () -> iterator;

        return StreamSupport.stream(
                iterable.spliterator(),
                false
        );
    }
}