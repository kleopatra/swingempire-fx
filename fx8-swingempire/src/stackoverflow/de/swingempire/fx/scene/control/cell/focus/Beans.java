/*
 * Created on 19.02.2019
 *
 */
package de.swingempire.fx.scene.control.cell.focus;

import java.io.Serializable;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class Beans {

    public static class TestBean implements Serializable {

        /**
         * 
         */
        private static final long    serialVersionUID = 1L;

        private SimpleStringProperty name;

        private List<RowBean>        lstRow;

        public TestBean(SimpleStringProperty name, List<RowBean> lstRow) {
           super();
           this.name = name;
           this.lstRow = lstRow;
        }

        /**
         * @return the lstRow
         */
        public List<RowBean> getLstRow() {
           return lstRow;
        }

        /**
         * @param lstRow
         *           the lstRow to set
         */
        public void setLstRow(List<RowBean> lstRow) {
           this.lstRow = lstRow;
        }

        /**
         * @return the name
         */
        public SimpleStringProperty getName() {
           return name;
        }

        /**
         * @param name
         *           the name to set
         */
        public void setName(SimpleStringProperty name) {
           this.name = name;
        }

     }


    public static class ColBean implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private SimpleStringProperty colValue;

        private boolean editable = false;

        public ColBean() {

        }

        public ColBean(SimpleStringProperty colValue) {
           super();
           this.colValue = colValue;
        }

        /**
         * @return the colValue
         */
        public SimpleStringProperty getColValue() {
           return colValue;
        }

        /**
         * @param colValue the colValue to set
         */
        public void setColValue(SimpleStringProperty colValue) {
           this.colValue = colValue;
        }

        /**
         * @return the editable
         */
        public boolean isEditable() {
           return editable;
        }

        /**
         * @param editable the editable to set
         */
        public void setEditable(boolean editable) {
           this.editable = editable;
        }

     } 

     public static class RowBean implements Serializable {

        /**
         * 
         */
        private static final long    serialVersionUID = 1L;

        private SimpleStringProperty nameRow;

        private List<ColBean>        lstColBean;

        public RowBean() {

        }

        public RowBean(SimpleStringProperty nameRow, List<ColBean> lstColBean) {
           super();
           this.nameRow = nameRow;
           this.lstColBean = lstColBean;
        }

        /**
         * @return the lstColBean
         */
        public List<ColBean> getLstColBean() {
           return lstColBean;
        }

        /**
         * @param lstColBean
         *           the lstColBean to set
         */
        public void setLstColBean(List<ColBean> lstColBean) {
           this.lstColBean = lstColBean;
        }

        /**
         * @return the nameRow
         */
        public SimpleStringProperty getNameRowProperty() {
           return nameRow;
        }

        /**
         * @param nameRow
         *           the nameRow to set
         */
        public void setNameRowProperty(SimpleStringProperty nameRow) {
           this.nameRow = nameRow;
        }

        public String getNameRow() {
           return nameRow.get();
        }

        public void setNameRow(String nameRow) {
           this.nameRow = new SimpleStringProperty(nameRow);
        }

     }


}
