/*
 * Created on 29.07.2015
 *
 */
package de.swingempire.fx.scene.control.pagination;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Pagination;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class DirectPaginationTest extends NavigationTest {
    
    
    private Pagination pagination;

    @Override
    public void setup() {
        pagination = new Pagination(1);
        super.setup();
    }

    @Override
    protected NavigationModel createNavigation() {
        return new PaginationDrivenNavigationModel(pagination);
    }

    /**
     * NavigationModel that delegates its current/size properties directly to
     * the pagination's properties
     */
    public static class PaginationDrivenNavigationModel implements NavigationModel {

        private Pagination pagination;

        public PaginationDrivenNavigationModel(Pagination pagination) {
            this.pagination = pagination;
        }
        @Override
        public IntegerProperty currentProperty() {
            return pagination.currentPageIndexProperty();
        }

        @Override
        public IntegerProperty sizeProperty() {
            return pagination.pageCountProperty();
        }
        
    }
}
