/*
 * Created on 14.10.2014
 *
 */
package de.swingempire.fx.scene.control.table;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * fx-9: formally updated, untested
 * 
 * ----------
 * TableView with visibleRowCountProperty.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewWithVisibleRowCount<T> extends TableView<T> {

    private IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 10);
    
    public IntegerProperty visibleRowCountProperty() {
        return visibleRowCount;
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableViewSkinX<T>(this);
    }
    
    /**
     * Skin that respects table's visibleRowCount property.
     */
    public static class TableViewSkinX<T> extends TableViewSkin<T> {
        private TableHeaderRow headerAlias;

        public TableViewSkinX(TableViewWithVisibleRowCount<T> tableView) {
            super(tableView);
            registerChangeListener(tableView.visibleRowCountProperty(), e -> visibleRowCountChanged());
            // fx-9: no way to inject a custom flow ...    
        }
        
        /**
         * @return
         */
        private void visibleRowCountChanged() {
            FXUtils.invokeSetFieldValue(TableViewSkinBase.class, this, "needCellsReconfigured", true);
            // PENDING JW: really focus? not layout?
            //getSkinnable().requestFocus();
            getSkinnable().requestLayout();
        }
        

        /**
         * Returns the visibleRowCount value of the table.
         */
        private int getVisibleRowCount() {
            return ((TableViewWithVisibleRowCount<T>) getSkinnable()).visibleRowCountProperty().get();
        }
        
        /**
         * Calculates and returns the pref height of the 
         * for the given number of rows.
         * 
         * If flow is of type MyFlow, queries the flow directly
         * otherwise invokes the method.
         */
        protected double getFlowPrefHeight(int rows) {
            double height = 0;
//            if (flow instanceof MyFlow) {
//                height = ((MyFlow) flow).getPrefLength(rows);
//            }
//            else {
                for (int i = 0; i < rows && i < getMyItemCount(); i++) {
                    height += invokeFlowCellLength(i);
                }
//            }    
            return height + snappedTopInset() + snappedBottomInset();

        }
        
        /**
         * super getItemCount is package private - no rocket science, though
         * @return
         */
        protected int getMyItemCount() {
            TableView<T> tableView = getSkinnable();
            return tableView.getItems() == null ? 0 : tableView.getItems().size();
        }
        
        /**
         * Overridden to compute the sum of the flow height and header prefHeight.
         */
        @Override
        protected double computePrefHeight(double width, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            // super hard-codes to 400 .. doooh
            double prefHeight = getFlowPrefHeight(getVisibleRowCount());
            return prefHeight + getTableHeader().prefHeight(width);
        }
        
        /**
         * super getTableHeaderRow didn't make it yet.
         * @return
         */
        protected TableHeaderRow getTableHeader() {
            return headerAlias;
        }
        
        @Override
        protected TableHeaderRow createTableHeaderRow() {
            headerAlias = super.createTableHeaderRow();
            return headerAlias;
        }
        
        /**
         * Reflectively invokes protected getCellLength(i) of flow.
         * @param index the index of the cell.
         * @return the cell height of the cell at index.
         */
        protected double invokeFlowCellLength(int index) {
            double height = 1.0;
            Class<?> clazz = VirtualFlow.class;
            VirtualFlow flow = (VirtualFlow) FXUtils.invokeGetFieldValue(TableViewSkinBase.class, this, "flow");
            try {
                Method method = clazz.getDeclaredMethod("getCellLength", Integer.TYPE);
                method.setAccessible(true);
                return ((double) method.invoke(flow, index));
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return height;
        }

//        /**
//         * Overridden to return custom flow.
//         */
//        @Override
//        protected VirtualFlow createVirtualFlow() {
//            return new MyFlow();
//        }
//        
        /**
         * Extended to expose length calculation per a given # of rows.
         */
//        public static class MyFlow extends VirtualFlow {
//
//            protected double getPrefLength(int rowsPerPage) {
//                double sum = 0.0;
//                int rows = rowsPerPage; //Math.min(rowsPerPage, getCellCount());
//                for (int i = 0; i < rows; i++) {
//                    sum += getCellLength(i);
//                }
//                return sum;
//            }
//
//        }
        
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableViewWithVisibleRowCount.class
            .getName());
}
