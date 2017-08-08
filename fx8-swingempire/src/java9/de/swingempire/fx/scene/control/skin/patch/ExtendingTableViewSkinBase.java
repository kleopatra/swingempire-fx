/*
 * Created on 05.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin.patch;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkinBase;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class ExtendingTableViewSkinBase<T> 
    extends TableViewSkinBase<T, T, TableView<T>, TableRow<T>, TableColumn<T, ?>> 
    implements SkinBaseDecorator {

    /**
     * @param table
     */
    public ExtendingTableViewSkinBase(TableView<T> table) {
        super(table);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected int getItemCount() {
        // TODO Auto-generated method stub
        return 0;
    }

}
