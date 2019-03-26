/*
 * Created on 26.03.2019
 *
 */
package de.swingempire.testfx.table;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableView;
import static de.swingempire.fx.scene.control.skin.TableFactory.*;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableEditFactory {

    /**
     * Creates and returns a table configured with columns for all properties
     * of a EditLineItem. Creates and sets items if createData is true.
     * 
     * @param createData
     * @return
     */
    public static TableView<EditLineItem> createEditLineItemTable(boolean createData) {
        TableView<EditLineItem> table = new TableView<>();
        table.getColumns().addAll(createTableColumn("countText"), createTableColumn("text"));
        return table;
    }
    
    /**
     * Creates and returns a table configured with column for all properties
     * of a EditLineItem. Does not add any data.
     * 
     * @return
     */
    public static TableView<EditLineItem> createEditLineItemTable() {
        return createEditLineItemTable(false);
    }
    
    public static class EditLineItem {
        private static int count;
        
        private StringProperty countText;
        private StringProperty text;
        
        public EditLineItem() {
            countText = new SimpleStringProperty(this, "countText", "" + count++);
            text = new SimpleStringProperty(this, "text", null);
        }
        
        public StringProperty countTextProperty() {
            return countText;
        }
        
        public StringProperty textProperty() {
            return text;
        }
        
    }
    private TableEditFactory() {}
}
