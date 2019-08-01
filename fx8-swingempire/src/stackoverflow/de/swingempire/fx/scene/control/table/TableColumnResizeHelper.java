/*
 * Created on 01.08.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sun.javafx.scene.control.TableColumnBaseHelper;

import javafx.scene.control.ResizeFeaturesBase;
import javafx.scene.control.TableColumnBase;

/**
 * C&p of the resize methods from package-private TableUtil in javafx.scene.controls
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnResizeHelper {
    /**
     * The constrained resize algorithm used by TableView and TreeTableView.
     * @param prop
     * @param isFirstRun
     * @param tableWidth
     * @param visibleLeafColumns
     * @return
     */
    static boolean constrainedResize(ResizeFeaturesBase prop,
                                     boolean isFirstRun,
                                     double tableWidth,
                                     List<? extends TableColumnBase<?,?>> visibleLeafColumns) {
        TableColumnBase<?,?> column = prop.getColumn();
        double delta = prop.getDelta();

        /*
         * There are two phases to the constrained resize policy:
         *   1) Ensuring internal consistency (i.e. table width == sum of all visible
         *      columns width). This is often called when the table is resized.
         *   2) Resizing the given column by __up to__ the given delta.
         *
         * It is possible that phase 1 occur and there be no need for phase 2 to
         * occur.
         */

        boolean isShrinking;
        double target;
        double totalLowerBound = 0;
        double totalUpperBound = 0;

        if (tableWidth == 0) return false;

        /*
         * PHASE 1: Check to ensure we have internal consistency. Based on the
         *          Swing JTable implementation.
         */
        // determine the width of all visible columns, and their preferred width
        double colWidth = 0;
        for (TableColumnBase<?,?> col : visibleLeafColumns) {
            colWidth += col.getWidth();
        }

        if (Math.abs(colWidth - tableWidth) > 1) {
            isShrinking = colWidth > tableWidth;
            target = tableWidth;

            if (isFirstRun) {
                // if we are here we have an inconsistency - these two values should be
                // equal when this resizing policy is being used.
                for (TableColumnBase<?,?> col : visibleLeafColumns) {
                    totalLowerBound += col.getMinWidth();
                    totalUpperBound += col.getMaxWidth();
                }

                // We run into trouble if the numbers are set to infinity later on
                totalUpperBound = totalUpperBound == Double.POSITIVE_INFINITY ?
                    Double.MAX_VALUE :
                    (totalUpperBound == Double.NEGATIVE_INFINITY ? Double.MIN_VALUE : totalUpperBound);

                for (TableColumnBase col : visibleLeafColumns) {
                    double lowerBound = col.getMinWidth();
                    double upperBound = col.getMaxWidth();

                    // Check for zero. This happens when the distribution of the delta
                    // finishes early due to a series of "fixed" entries at the end.
                    // In this case, lowerBound == upperBound, for all subsequent terms.
                    double newSize;
                    if (Math.abs(totalLowerBound - totalUpperBound) < .0000001) {
                        newSize = lowerBound;
                    } else {
                        double f = (target - totalLowerBound) / (totalUpperBound - totalLowerBound);
                        newSize = Math.round(lowerBound + f * (upperBound - lowerBound));
                    }

                    double remainder = resize(col, newSize - col.getWidth());

                    target -= newSize + remainder;
                    totalLowerBound -= lowerBound;
                    totalUpperBound -= upperBound;
                }

                isFirstRun = false;
            } else {
                double actualDelta = tableWidth - colWidth;
                List<? extends TableColumnBase<?,?>> cols = visibleLeafColumns;
                resizeColumns(cols, actualDelta);
            }
        }

        // At this point we can be happy in the knowledge that we have internal
        // consistency, i.e. table width == sum of the width of all visible
        // leaf columns.

        /*
         * Column may be null if we just changed the resize policy, and we
         * just wanted to enforce internal consistency, as mentioned above.
         */
        if (column == null) {
//            return true;
            // original: why returning the equivalent of failure?
            return false;
        }

        /*
         * PHASE 2: Handling actual column resizing (by the user). Based on my own
         *          implementation (based on the UX spec).
         */

        isShrinking = delta < 0;

        // need to find the last leaf column of the given column - it is this
        // column that we actually resize from. If this column is a leaf, then we
        // use it.
        TableColumnBase<?,?> leafColumn = column;
        while (leafColumn.getColumns().size() > 0) {
            leafColumn = leafColumn.getColumns().get(leafColumn.getColumns().size() - 1);
        }

        int colPos = visibleLeafColumns.indexOf(leafColumn);
        int endColPos = visibleLeafColumns.size() - 1;

        // we now can split the observableArrayList into two subobservableArrayLists, representing all
        // columns that should grow, and all columns that should shrink
        //    var growingCols = if (isShrinking)
        //        then table.visibleLeafColumns[colPos+1..endColPos]
        //        else table.visibleLeafColumns[0..colPos];
        //    var shrinkingCols = if (isShrinking)
        //        then table.visibleLeafColumns[0..colPos]
        //        else table.visibleLeafColumns[colPos+1..endColPos];


        double remainingDelta = delta;
        while (endColPos > colPos && remainingDelta != 0) {
            TableColumnBase<?,?> resizingCol = visibleLeafColumns.get(endColPos);
            endColPos--;

            // if the column width is fixed, break out and try the next column
            if (! resizingCol.isResizable()) continue;

            // for convenience we discern between the shrinking and growing columns
            TableColumnBase<?,?> shrinkingCol = isShrinking ? leafColumn : resizingCol;
            TableColumnBase<?,?> growingCol = !isShrinking ? leafColumn : resizingCol;

            //        (shrinkingCol.width == shrinkingCol.minWidth) or (growingCol.width == growingCol.maxWidth)

            if (growingCol.getWidth() > growingCol.getPrefWidth()) {
                // growingCol is willing to be generous in this case - it goes
                // off to find a potentially better candidate to grow
                List<? extends TableColumnBase> seq = visibleLeafColumns.subList(colPos + 1, endColPos + 1);
                for (int i = seq.size() - 1; i >= 0; i--) {
                    TableColumnBase<?,?> c = seq.get(i);
                    if (c.getWidth() < c.getPrefWidth()) {
                        growingCol = c;
                        break;
                    }
                }
            }
            //
            //        if (shrinkingCol.width < shrinkingCol.prefWidth) {
            //            for (c in reverse table.visibleLeafColumns[colPos+1..endColPos]) {
            //                if (c.width > c.prefWidth) {
            //                    shrinkingCol = c;
            //                    break;
            //                }
            //            }
            //        }



            double sdiff = Math.min(Math.abs(remainingDelta), shrinkingCol.getWidth() - shrinkingCol.getMinWidth());

//                System.out.println("\tshrinking " + shrinkingCol.getText() + " and growing " + growingCol.getText());
//                System.out.println("\t\tMath.min(Math.abs("+remainingDelta+"), "+shrinkingCol.getWidth()+" - "+shrinkingCol.getMinWidth()+") = " + sdiff);

            double delta1 = resize(shrinkingCol, -sdiff);
            double delta2 = resize(growingCol, sdiff);
            remainingDelta += isShrinking ? sdiff : -sdiff;
        }
        return remainingDelta == 0;
    }

    // function used to actually perform the resizing of the given column,
    // whilst ensuring it stays within the min and max bounds set on the column.
    // Returns the remaining delta if it could not all be applied.
    static double resize(TableColumnBase column, double delta) {
        if (delta == 0) return 0.0F;
        if (! column.isResizable()) return delta;

        final boolean isShrinking = delta < 0;
        final List<TableColumnBase<?,?>> resizingChildren = getResizableChildren(column, isShrinking);

        if (resizingChildren.size() > 0) {
            return resizeColumns(resizingChildren, delta);
        } else {
            double newWidth = column.getWidth() + delta;

            if (newWidth > column.getMaxWidth()) {
//                column.doSetWidth(column.getMaxWidth());
                doSetColumnWidth(column, column.getMaxWidth());
                return newWidth - column.getMaxWidth();
            } else if (newWidth < column.getMinWidth()) {
//                column.doSetWidth(column.getMinWidth());
                doSetColumnWidth(column, column.getMinWidth());
                return newWidth - column.getMinWidth();
            } else {
//                column.doSetWidth(newWidth);
                doSetColumnWidth(column, newWidth);
                return 0.0F;
            }
        }
    }

    private static void doSetColumnWidth(TableColumnBase column, double width) {
        TableColumnBaseHelper.setWidth(column, width);
    }
    
    // Returns all children columns of the given column that are able to be
    // resized. This is based on whether they are visible, resizable, and have
    // not space before they hit the min / max values.
    private static List<TableColumnBase<?,?>> getResizableChildren(TableColumnBase<?,?> column, boolean isShrinking) {
        if (column == null || column.getColumns().isEmpty()) {
            return Collections.emptyList();
        }

        List<TableColumnBase<?,?>> tablecolumns = new ArrayList<TableColumnBase<?,?>>();
        for (TableColumnBase c : column.getColumns()) {
            if (! c.isVisible()) continue;
            if (! c.isResizable()) continue;

            if (isShrinking && c.getWidth() > c.getMinWidth()) {
                tablecolumns.add(c);
            } else if (!isShrinking && c.getWidth() < c.getMaxWidth()) {
                tablecolumns.add(c);
            }
        }
        return tablecolumns;
    }

    private static double resizeColumns(List<? extends TableColumnBase<?,?>> columns, double delta) {
        // distribute space between all visible children who can be resized.
        // To do this we need to work out if we're shrinking or growing the
        // children, and then which children can be resized based on their
        // min/pref/max/fixed properties. The results of this are in the
        // resizingChildren observableArrayList above.
        final int columnCount = columns.size();

        // work out how much of the delta we should give to each child. It should
        // be an equal amount (at present), although perhaps we'll allow for
        // functions to calculate this at a later date.
        double colDelta = delta / columnCount;

        // we maintain a count of the amount of delta remaining to ensure that
        // the column resize operation accurately reflects the location of the
        // mouse pointer. Every time this value is not 0, the UI is a teeny bit
        // more inaccurate whilst the user continues to resize.
        double remainingDelta = delta;

        // We maintain a count of the current column that we're on in case we
        // need to redistribute the remainingDelta among remaining sibling.
        int col = 0;

        // This is a bit hacky - often times the leftOverDelta is zero, but
        // remainingDelta doesn't quite get down to 0. In these instances we
        // short-circuit and just return 0.0.
        boolean isClean = true;
        for (TableColumnBase<?,?> childCol : columns) {
            col++;

            // resize each child column
            double leftOverDelta = resize(childCol, colDelta);

            // calculate the remaining delta if the was anything left over in
            // the last resize operation
            remainingDelta = remainingDelta - colDelta + leftOverDelta;

            //      println("\tResized {childCol.text} with {colDelta}, but {leftOverDelta} was left over. RemainingDelta is now {remainingDelta}");

            if (leftOverDelta != 0) {
                isClean = false;
                // and recalculate the distribution of the remaining delta for
                // the remaining siblings.
                colDelta = remainingDelta / (columnCount - col);
            }
        }

        // see isClean above for why this is done
        return isClean ? 0.0 : remainingDelta;
    }

}
