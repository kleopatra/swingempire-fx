/*
 * Created on 15.04.2019
 *
 */
package de.swingempire.fx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;

import static de.swingempire.fx.util.FXUtils.*;

import javafx.scene.control.skin.TableViewSkin;

/**
 * Decorator to facilitate access to internals of TableViewSkin.  
 * Must only be implemented by skins of type TableViewSkin
 * and its subclasses.
 * <p>
 * Note: some of the accessors are no longer needed in fx12. This is meant
 * to be used (as opposed to the TableSkin/TableBehariorDecorator which are meant
 * as a api design study).
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TableViewSkinDecorator<T> extends TableViewSkinBaseDecorator {

    default TableViewBehavior<T> getTableViewBehavior() {
        return (TableViewBehavior<T>) invokeGetFieldValue(TableViewSkin.class, this, "behavior");
    }
}
