/*
 * Created on 19.02.2019
 *
 */
package de.swingempire.fx.scene.control.cell.focus;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.swingempire.fx.scene.control.cell.focus.Beans.ColBean;
import de.swingempire.fx.scene.control.cell.focus.Beans.RowBean;
import de.swingempire.fx.scene.control.cell.focus.Beans.TestBean;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

/**
 * all in this package related to 
 * https://stackoverflow.com/q/54760208/203657
 * 
 * start edit from menuItem (and requestFocus on the textField)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SyncrTwoTablesController implements Initializable {

//    public class SyncrTwoTablesController implements Initializable {

    @FXML
    private ScrollPane scPane;

    @FXML
    private HBox hBox;

    @FXML
    private TableView<RowBean> tableNoScroll;

    @FXML
    private TableView<RowBean> tableScroll;

    private TestBean testBean;

    @FXML
    private TableColumn<RowBean, String> tcName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Controller");
        initializeBean();
        fillTables();

    }

    private void fillTables() {
        tableNoScroll
                .setItems(FXCollections.observableList(testBean.getLstRow()));
        tableScroll
                .setItems(FXCollections.observableList(testBean.getLstRow()));
        tcName.setCellValueFactory(
                new PropertyValueFactory<RowBean, String>("nameRow"));

        List<TableColumn<RowBean, String>> lstColums = new ArrayList<TableColumn<RowBean, String>>();
        // TableColumn<RowBean, String> col = null;
        tableScroll.getColumns().clear();
        if (testBean.getLstRow().size() > 0) {
            for (int i = 0; i < testBean.getLstRow().get(0).getLstColBean()
                    .size(); i++) {
                TableColumn<RowBean, String> col = new TableColumn<RowBean, String>(
                        "col" + i);
                int id = i;
                col.setCellValueFactory(
                        new Callback<CellDataFeatures<RowBean, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(
                                    CellDataFeatures<RowBean, String> p) {
                                return p.getValue().getLstColBean()
                                        .get(id) != null
                                                ? p.getValue().getLstColBean()
                                                        .get(id).getColValue()
                                                : new SimpleStringProperty("");
                            }
                        });
                col.setCellFactory(
                        new Callback<TableColumn<RowBean, String>, TableCell<RowBean, String>>() {
                            @Override
                            public TableCell<RowBean, String> call(
                                    TableColumn<RowBean, String> param) {
                                EditingCell<RowBean, String> cell = new EditingCell(
                                        id);
                                cell.setOnMouseClicked(
                                        new EventHandler<MouseEvent>() {
                                            @Override
                                            public void handle(
                                                    MouseEvent event) {
                                                addMenuMonthColumns(param, cell,
                                                        id);

                                            }

                                        });
                                return cell;
                            }
                        });
                lstColums.add(col);
            }
            tableScroll.getColumns().addAll(lstColums);
        }
    }

    private void addMenuMonthColumns(TableColumn<RowBean, String> param,
            EditingCell<RowBean, String> cell, int i) {
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(optionOne(param, i), optionTwo());
        cell.setContextMenu(menu);

    }

    private MenuItem optionOne(TableColumn<RowBean, String> param, int i) {
        MenuItem menuPlan = new MenuItem("Option 1");
        menuPlan.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int row = tableScroll.getSelectionModel().getSelectedIndex();
                RowBean rowBean = tableScroll.getItems().get(row);
                ColBean colBean = rowBean.getLstColBean().get(i);
                colBean.setEditable(true);

                tableScroll.getFocusModel().focus(row, param);
                tableScroll.requestFocus();
                refresh(tableScroll, tableScroll.getItems());
            }
        });
        return menuPlan;
    }

    private MenuItem optionTwo() {
        MenuItem menuPlan = new MenuItem("Option 2");
        menuPlan.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

            }
        });
        return menuPlan;
    }

    private void initializeBean() {
        ColBean colBean = new ColBean(new SimpleStringProperty("hola"));
        ColBean colBean2 = new ColBean(new SimpleStringProperty("hola"));
        ColBean colBean3 = new ColBean(new SimpleStringProperty("hola"));
        ColBean colBean4 = new ColBean(new SimpleStringProperty("hola"));
        ColBean colBean5 = new ColBean(new SimpleStringProperty("hola"));
        ColBean colBean6 = new ColBean(new SimpleStringProperty("hola"));
        List<ColBean> lstColBean = new ArrayList<ColBean>();
        lstColBean.add(colBean);
        lstColBean.add(colBean2);
        lstColBean.add(colBean3);
        lstColBean.add(colBean4);
        lstColBean.add(colBean5);
        lstColBean.add(colBean6);
        ColBean colBean7 = new ColBean(new SimpleStringProperty("adios"));
        ColBean colBean8 = new ColBean(new SimpleStringProperty("adios"));
        ColBean colBean9 = new ColBean(new SimpleStringProperty("adios"));
        ColBean colBean10 = new ColBean(new SimpleStringProperty("adios"));
        ColBean colBean11 = new ColBean(new SimpleStringProperty("adios"));
        ColBean colBean12 = new ColBean(new SimpleStringProperty("adios"));
        List<ColBean> lstColBean2 = new ArrayList<ColBean>();
        lstColBean2.add(colBean7);
        lstColBean2.add(colBean8);
        lstColBean2.add(colBean9);
        lstColBean2.add(colBean10);
        lstColBean2.add(colBean11);
        lstColBean2.add(colBean12);
        RowBean rowBean = new RowBean(new SimpleStringProperty("hola"),
                lstColBean);
        RowBean rowBean2 = new RowBean(new SimpleStringProperty("adios"),
                lstColBean2);

        List<RowBean> lstRow = new ArrayList<RowBean>();
        lstRow.add(rowBean);
        lstRow.add(rowBean2);

        testBean = new TestBean(new SimpleStringProperty("test"), lstRow);

    }

    /**
     * Method that refresh the contains of the table.
     * 
     * @param table of type <code>TableView<T></code>
     * @param tableList of type <code>List<T></code>
     */
    public static <T> void refresh(final TableView<T> table,
            final List<T> tableList) {
//           table.setItems(null);
//           table.layout();
//           table.setItems(FXCollections.observableList(tableList));
        FXCollections.copy(table.getItems(), tableList);
    }

    public static class EditingCell<S, T> extends TableCell<RowBean, String> {

        private TextField textField;

        private int col;

        public EditingCell(int col) {

            this.col = col;

        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (item != null) {
                    if (getTableView().getItems().get(getIndex())
                            .getLstColBean().get(col).isEditable()) {
                        if (textField == null) {
                            textField = new TextField();
                        }
                        textField.setText(item);
                        textField.focusedProperty()
                                .addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(
                                            ObservableValue<? extends Boolean> arg0,
                                            Boolean arg1, Boolean newValue) {
                                        if (newValue) {
                                            System.out.println("requested");
                                            textField.selectAll();
                                        }
                                    }
                                });
                        setGraphic(textField);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        textField.requestFocus();

                    } else {
                        setText(item);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        }

    }

}
