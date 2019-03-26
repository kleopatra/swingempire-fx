/*
 * Created on 25.03.2019
 *
 */
package de.swingempire.testfx.table;

import java.util.logging.Logger;

import de.swingempire.testfx.util.TableViewSkinBaseDecorator;
import static de.swingempire.testfx.util.TableFactory.*;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Utility class to configure a TableView for improved pref (height)
 * calculation. The idea is to delegate pref height calc to the table's flow.
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePrefSizeFactory {

    private static class TTableViewSkin<T> extends TableViewSkin<T>
            implements TableViewSkinBaseDecorator {

        public TTableViewSkin(TableView<T> control) {
            super(control);
            // this is fix of bug
            // https://bugs.openjdk.java.net/browse/JDK-8221334
            // configure cell count
            updateItemCount();
        }

        /**
         * delegating to the flow without the config fix doesn't show any rows
         * at all (because the flow has not yet any cells)
         */
        @Override
        protected double computePrefHeight(double width, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            // delegate to flow for pref of viewport
            double pref = ((TVirtualFlow) getVirtualFlow())
                    .computePrefHeight(width);
            double header = getTableHeader().prefHeight(width);
//        LOG.info("skin: " + pref + getSkinnable().getScene().getWindow()); 
//        new RuntimeException("who is calling? " + pref + "\n").printStackTrace();
            return pref + header;
//        double superResult = super.computePrefHeight(width, topInset, rightInset, bottomInset,
//                leftInset);
//        return superResult;
        }

        @Override
        protected VirtualFlow<TableRow<T>> createVirtualFlow() {
            return new TVirtualFlow();
        }

    }

    private static class TVirtualFlow extends VirtualFlow {

        /**
         * Overridden to allow access to super
         */
        @Override
        protected double computePrefHeight(double width) {
            double pref = super.computePrefHeight(width);
//        LOG.info("flow: " + pref + " / " + getParent().getClass());
            return pref;
        }

    }

    public static <T> TableView<T> createTablePrefSize() {
        return createTable(TTableViewSkin<T>::new);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TablePrefSizeFactory.class.getName());
}
