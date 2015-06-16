/*
 * Created on 16.06.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.function.Predicate;

import javafx.scene.control.TreeItem;

/**
 * Filtering Tree: all related classes copied from
 * http://www.kware.net/?p=204
 */
@FunctionalInterface
public interface TreeItemPredicate<T> {
 
    boolean test(TreeItem<T> parent, T value);
 
    static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
        return (parent, value) -> predicate.test(value);
    }
 
}