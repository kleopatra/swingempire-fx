/*
 * Created on 11.02.2018
 *
 */
package de.swingempire.fx.scene.control.table.navigation;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.util.SkinUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * problem: (unusual - textField is data, used as node in cell), tabbing moves to
 * next cell (== textField as data) until it reaches the end of the table, then
 * moves to stand-alone textfield.
 * 
 * https://stackoverflow.com/q/48724049/203657
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    TableView table;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

//        TableViewSkin s;
        initTableWithRawData();
//        initTableWithTableData();
    }
    /**
     * 
     */
    protected void initTableWithRawData() {
        ObservableList<RawData> data = FXCollections.observableArrayList();
    
        table.setEditable(true);
        TableColumn<RawData, String> col1 = new TableColumn<>("Data1");
        col1.setCellValueFactory(new PropertyValueFactory<>("c1"));
        col1.setCellFactory(TextFieldTableCell.forTableColumn());
        
        TableColumn<RawData, String> col2 = new TableColumn<>("Data2");
        col2.setCellValueFactory(new PropertyValueFactory<>("c2"));
        col2.setCellFactory(TextFieldTableCell.forTableColumn());
    
        
        table.getColumns().add(col1);
        table.getColumns().add(col2);
        table.setItems(data);
        for (int i = 0; i < 360; i++) {
            table.getItems().add(new RawData("" + i, "" + i * 2));
        }
        col2.setSortable(false);
        
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                if (table.getEditingCell() != null) {
                    // make certain to commit the edit ..
                    
                }
                table.getSelectionModel().selectNext();
                
                // not exactly: this keeps the scrolled to cell at the top
                //table.scrollTo(table.getSelectionModel().getSelectedIndex());
                TableViewSkin<?> skin = (TableViewSkin<?>) table.getSkin();
                if (skin != null) {
                    VirtualFlow<?> flow = getVirtualFlow(skin);
                    // position range is [0, 1], count is the number of items
                    int selectedIndex = table.getSelectionModel().getSelectedIndex();
                    LOG.info("position: " + flow.getPosition() +  " / " + flow.getCellCount() + " / " + selectedIndex);
                    // also scrolls about a complete page (after adjusting the very last
                    // to completely be shown)
                    flow.scrollTo(selectedIndex);
                }
                e.consume();
            }
        });
    }
    /**
     * @param skin
     * @return
     */
    private VirtualFlow<?> getVirtualFlow(TableViewSkin skin) {
        return SkinUtils.getVirtualFlow(skin);
//        return (VirtualFlow<?>) FXUtils.invokeGetFieldValue(TableViewSkinBase.class, skin, "flow");
        // following throws NoSuchMethodDeclaration - was wrong import (from swingempire.patch)
//        return (VirtualFlow<?>) FXUtils.invokeGetMethodValue(VirtualContainerBase.class, skin, "getVirtualFlow");
    }
    /**
     * 
     */
    protected void initTableWithTableData() {
        ObservableList<TableData> data = FXCollections.observableArrayList();
        
        table.setEditable(true);
        TableColumn<TableData, MyTextfield> col1 = new TableColumn<>("Data1");
        
        col1.setCellValueFactory(new PropertyValueFactory<>("c1"));
        TableColumn<TableData, MyTextfield> col2 = new TableColumn<>("Data2");
        
        col2.setCellValueFactory(new PropertyValueFactory<>("c2"));
        
        table.getColumns().add(col1);
        table.getColumns().add(col2);
        table.setItems(data);
        for (int i = 0; i < 360; i++) {
            table.getItems().add(new TableData("" + i, "" + i * 2));
        }
        col2.setSortable(false);
    }
    
    public static class RawData {
        private StringProperty c1;
        private StringProperty c2;
        public RawData(String c1, String c2) {
            super();
            this.c1 = new SimpleStringProperty(c1);
            this.c2 = new SimpleStringProperty(c2);
        }
        
        public StringProperty c1Property() {
            return c1;
        }
        /**
         * @return the c1
         */
        public String getC1() {
            return c1Property().get();
        }
        /**
         * @param c1 the c1 to set
         */
        public void setC1(String c1) {
            c1Property().set(c1);;
        }
        
        public StringProperty c2Property() {
            return c2;
        }
        
        /**
         * @return the c2
         */
        public String getC2() {
            return c2Property().get();
        }
        /**
         * @param c2 the c2 to set
         */
        public void setC2(String c2) {
            c2Property().set(c2);
        }
        
    }
    public static class TableData {

        public TableData(String a, String b) {
            c1 = new MyTextfield(a);
            c2 = new MyTextfield(b);
        }

        MyTextfield c1;

        MyTextfield c2;

        public MyTextfield getC1() {
            return c1;
        }

        public void setC1(MyTextfield c1) {
            this.c1 = c1;
        }

        public MyTextfield getC2() {
            return c2;
        }

        public void setC2(MyTextfield c2) {
            this.c2 = c2;
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FXMLDocumentController.class.getName());
}

