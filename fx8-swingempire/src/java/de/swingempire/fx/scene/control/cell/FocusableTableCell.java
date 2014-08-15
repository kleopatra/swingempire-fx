/*
 * Created on 15.08.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;

/**
 * Hack around: missing focus indicator.<p>
 * 
 * Issue https://javafx-jira.kenai.com/browse/RT-29369 <p>
 * 
 * Installs a listener on the focusModel's focusedCell property
 * to set the cell's focused property to true if it matches, 
 * false otherwise.
 * 
 * Notes:
 * - must add some styling for <code>.table-cell:focused</code>
 * - hampered by focusedCell being set only on clicking with mouse
 * - even that is lost when moving the focus via ctrl-somekey 
 * 
 */
public class FocusableTableCell<S, T> extends TableCell<S, T> {

    private ChangeListener<TablePosition> focusListener = (e, oldCell, newCell) -> {
        updateFocus(newCell);
    };

    public FocusableTableCell() {
        tableViewProperty().addListener((e, oldValue, newValue) -> {
            uninstallFocusListener(oldValue);
            installFocusListener(newValue);
        });
    }

    /**
     * Callback method from listener to focusedCellProperty.
     * 
     * @param the new value of focusedCell
     */
    protected void updateFocus(TablePosition<S, ?> focusedCell) {
        setFocused(match(focusedCell));
        // LOG.info("focused? " + isFocused() + newCell);
    }

    /**
     * c&p of super (WTF is that method private?)
     * 
     * @param pos a TablePosition to check for matching
     * @return true if the given position matches this cell, false otherwise.
     */
    protected boolean match(TablePosition<S, ?> pos) {
        return pos != null && pos.getRow() == getIndex()
                && pos.getTableColumn() == getTableColumn();
    }

    /**
     * C&P'ed from TableColumn default tableCell. Would expect this to reside in
     * TableCell ...
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        if (item == getItem())
            return;

        super.updateItem(item, empty);

        if (item == null) {
            super.setText(null);
            super.setGraphic(null);
        } else if (item instanceof Node) {
            super.setText(null);
            super.setGraphic((Node) item);
        } else {
            super.setText(item.toString());
            super.setGraphic(null);
        }
    }

    /**
     * @param table
     */
    private void installFocusListener(TableView<S> table) {
        if (table == null)
            return;
        TableViewFocusModel<S> model = table.getFocusModel();
        if (model != null)
            model.focusedCellProperty().addListener(focusListener);
    }

    /**
     * @param oldValue
     */
    private void uninstallFocusListener(TableView<S> table) {
        if (table == null)
            return;
        TableViewFocusModel<S> model = table.getFocusModel();
        if (model != null)
            model.focusedCellProperty().removeListener(focusListener);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FocusableTableCell.class
            .getName());
}