/*
 * Created on 29.07.2015
 *
 */
package de.swingempire.fx.scene.control.pagination;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Pagination;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class BoundPaginationTest extends NavigationTest {

    
    private Pagination pagination;

    @Test
    public void testCurrentOnPagination() {
        pagination.setCurrentPageIndex(5);
        assertEquals(0, pagination.getCurrentPageIndex());
    }
    
    @Test
    public void testCurrentDecreasedSizeOnPagination() {
        pagination.setPageCount(10);
        pagination.setCurrentPageIndex(5);
        pagination.setPageCount(4);
        assertEquals(3, nav.getCurrent());
        assertEquals(3, pagination.getCurrentPageIndex());
    }
    @Override
    public void setup() {
        pagination = new Pagination(1);
        super.setup();
    }

    @Override
    protected NavigationModel createNavigation() {
        return new BoundPaginationNavigationModel(pagination);
    }

    public static class BoundPaginationNavigationModel implements NavigationModel {

        private Pagination pagination;

        private IntegerProperty current;
        private IntegerProperty size;
        
        /**
         * @param pagination
         */
        public BoundPaginationNavigationModel(Pagination pagination) {
            this.pagination = pagination;
            current = new SimpleIntegerProperty() {

                @Override
                protected void invalidated() {
                    int current = get();
                    if (current >= getSize()) {
                        set(getSize() - 1);
                    } else if (current < 0) {
                        set(0);
                    }
                }
                
            };
            current.bindBidirectional(pagination.currentPageIndexProperty());
            size = new SimpleIntegerProperty(this, "size", 1) {

                @Override
                protected void invalidated() {
                    int val = get();
                    if (val < 1) {
                        set(1);
                    }
                    updateCurrent();
                }
            };
            size.bindBidirectional(pagination.pageCountProperty());
        }

        /**
         * called when size is changed: must enforce invariant 0 <= current < size.
         * 
         */
        protected void updateCurrent() {
            if (getCurrent() >= getSize()) {
                setCurrent(getSize() - 1);
            }
        }

        @Override
        public IntegerProperty currentProperty() {
            return current;
        }

        @Override
        public IntegerProperty sizeProperty() {
            return size;
        }
        
    }
}
