/*
 * Created on 25.03.2019
 *
 */
package de.swingempire.fx.scene.control.skin;

import static de.swingempire.fx.util.FXUtils.*;

import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkinBase;

/**
 * Decorator to facilitate access to internals of TableViewSkinBase.  
 * Must only be implemented by skins of type TableViewSkinBase
 * and its subclasses.
 * <p>
 * Note: some of the accessors are no longer needed in fx12. This is meant
 * to be used (as opposed to the TableSkin/TableBehariorDecorator which are meant
 * as a api design study).
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface TableViewSkinBaseDecorator {

    /**
     * Returns the TableHeaderRow as set by super. 
     * @return
     */
    default TableHeaderRow getTableHeader() {
        return (TableHeaderRow) invokeGetMethodValue(TableViewSkinBase.class, this, "getTableHeaderRow");
    }
}
