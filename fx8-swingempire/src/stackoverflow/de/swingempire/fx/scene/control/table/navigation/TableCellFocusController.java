/*
 * Created on 15.08.2018
 *
 */
package de.swingempire.fx.scene.control.table.navigation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * https://stackoverflow.com/q/51806252/203657
 * navigate to cell and focus (aka: edit) the control that represents the 
 * editing actor
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellFocusController implements Initializable {

    private DAOGenUtil DAOGenUtil = new DAOGenUtil();

    public ObservableList<TestModel> olTestModel = FXCollections
            .observableArrayList(testmodel -> new Observable[] {
                    testmodel.textFieldProperty(),
                    testmodel.comboBoxFieldProperty() });

    ObservableList<DBComboChoice> comboChoices = FXCollections
            .observableArrayList();

    TableColumn<TestModel, String> colTextField = new TableColumn<>("text col");

    TableColumn<TestModel, DBComboChoice> colComboBoxField = DAOGenUtil
            .createComboBoxColumn("combo col", TestModel::comboBoxFieldProperty,
                    comboChoices);

    @FXML
    private TableView<TestModel> tv;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        comboChoices.add(new DBComboChoice("F", "Female"));
        comboChoices.add(new DBComboChoice("M", "Male"));

        olTestModel.add(new TestModel("test row 1", "M"));
        olTestModel.add(new TestModel("test row 2", "F"));
        olTestModel.add(new TestModel("test row 3", "F"));
        olTestModel.add(new TestModel("test row 4", "M"));
        olTestModel.add(new TestModel("test row 5", "F"));

        colTextField
                .setCellValueFactory(new PropertyValueFactory<>("textField"));

        tv.getSelectionModel().setCellSelectionEnabled(true);
        tv.setEditable(true);
        tv.getColumns().addAll(colTextField, colComboBoxField);
        tv.setItems(olTestModel);

        tv.setOnKeyPressed(event -> {

            TableColumn<TestModel, String> firstCol = colTextField;
            TableColumn<TestModel, DBComboChoice> lastCol = colComboBoxField;
            int firstRow = 0;
            int lastRow = tv.getItems().size() - 1;
            int maxCols = 1;
            
            DAOGenUtil.handleTableViewSpecialKeys(tv, event, firstCol, lastCol,
                    firstRow, lastRow, maxCols);

        });

    }

    public static class ComboBoxCell<S, T> extends TableCell<S, T> {

        private final ComboBox<DBComboChoice> combo = new ComboBox<>();

        private final DAOGenUtil DAOGenUtil;

        public ComboBoxCell(ObservableList<DBComboChoice> comboData) {

            this.DAOGenUtil = new DAOGenUtil();

            combo.getItems().addAll(comboData);
            combo.setEditable(false);

            setGraphic(combo);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            combo.setOnAction((ActionEvent event) -> {

                String masterCode = combo.getSelectionModel().getSelectedItem()
                        .getMasterCode();

                S datamodel = getTableView().getItems().get(getIndex());

                try {

                    Method mSetComboBoxField = datamodel.getClass().getMethod(
                            "setComboBoxField", (Class) String.class);
                    mSetComboBoxField.invoke(datamodel, masterCode);

                } catch (NoSuchMethodException | SecurityException
                        | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException ex) {
                    System.err.println(ex);
                }

            });

        }

        @Override
        protected void updateItem(T comboChoice, boolean empty) {
            super.updateItem(comboChoice, empty);
            if (empty) {
                setGraphic(null);
            } else {
                combo.setValue((DBComboChoice) comboChoice);
                setGraphic(combo);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }

    }

    public static class TestModel {

        private StringProperty textField;

        private StringProperty comboBoxField;

        public TestModel() {
            this(null, null);
        }

        public TestModel(String textField, String comboBoxField) {
            this.textField = new SimpleStringProperty(textField);
            this.comboBoxField = new SimpleStringProperty(comboBoxField);
        }

        public String getTextField() {
            return textField.get().trim();
        }

        public void setTextField(String textField) {
            this.textField.set(textField);
        }

        public StringProperty textFieldProperty() {
            return textField;
        }

        public String getComboBoxField() {
            return comboBoxField.get().trim();
        }

        public void setComboBoxField(String comboBoxField) {
            this.comboBoxField.set(comboBoxField);
        }

        public StringProperty comboBoxFieldProperty() {
            return comboBoxField;
        }

    }

    public static class DBComboChoice {

        private StringProperty masterCode;

        private StringProperty masterDescription;

        public DBComboChoice(String masterCode, String masterDescription) {
            this.masterCode = new SimpleStringProperty(masterCode);
            this.masterDescription = new SimpleStringProperty(
                    masterDescription);
        }

        public String getMasterCode() {
            return masterCode.get();
        }

        public StringProperty masterCodeProperty() {
            return masterCode;
        }

        public String getMasterDescription() {
            return masterDescription.get();
        }

        public StringProperty masterDescriptionProperty() {
            return masterDescription;
        }

        public static DBComboChoice getDescriptionByMasterCode(
                String inMasterCode, ObservableList<DBComboChoice> comboData) {
            for (int i = 0; i < comboData.size(); i++) {
                if (comboData.get(i).getMasterCode().equals(inMasterCode)) {
                    return comboData.get(i);
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return this.masterDescription.get();
        }

    }

    public static class DAOGenUtil {

        public <S> TableColumn<S, DBComboChoice> createComboBoxColumn(
                String title,
                Function<S, StringProperty> methodGetComboFieldProperty,
                ObservableList<DBComboChoice> comboData) {

            TableColumn<S, DBComboChoice> col = new TableColumn<>(title);

            col.setCellValueFactory(cellData -> {
                String masterCode = methodGetComboFieldProperty
                        .apply(cellData.getValue()).get();
                DBComboChoice choice = DBComboChoice
                        .getDescriptionByMasterCode(masterCode, comboData);
                return new SimpleObjectProperty<>(choice);
            });

            col.setCellFactory(
                    (TableColumn<S, DBComboChoice> param) -> new ComboBoxCell<>(
                            comboData));

            return col;

        }

        public <S> void handleTableViewSpecialKeys(TableView tv, KeyEvent event,
                TableColumn firstCol, TableColumn lastCol, int firstRow,
                int lastRow, int maxCols) {

            // NB: pos, at this point, is the cell position that the cursor is
            // about to leave
            TablePosition<S, ?> pos = tv.getFocusModel().getFocusedCell();
            LOG.info("inn handle: " + pos);
            if (pos != null) {

                if (event.getCode() == KeyCode.TAB) {

                    tv.getSelectionModel().selectRightCell();
                    endOfRowCheck(tv, event, pos, firstCol, maxCols);

                    event.consume();

                    TablePosition<S, ?> focussedPos = tv.getFocusModel()
                            .getFocusedCell();
                    TableColumn tableColumn = (TableColumn<S, ?>) focussedPos
                            .getTableColumn();
                    TableCell cell = (TableCell) tableColumn.getCellFactory()
                            .call(tableColumn);
                    Node cellGraphic = cell.getGraphic();
                    System.out.println("node cellGraphic is " + cellGraphic);

                    if (cellGraphic instanceof ComboBox<?>) {
                        System.out.println("got a combo");
                        // nbg cellGraphic.requestFocus();
                        Platform.runLater(() -> {
                            ((ComboBox<?>) cellGraphic).requestFocus();
                        });
                    }

                } else if (!event.isShiftDown() && !event.isControlDown()) {
                    // edit the cell
                    tv.edit(pos.getRow(), pos.getTableColumn());
                }

            }

        }

        private void endOfRowCheck(TableView tv, KeyEvent event,
                TablePosition pos, TableColumn col, int maxCols) {

            if (pos.getColumn() == maxCols) {
                // We're at the end of a row so position to the start of the
                // next row
                tv.getSelectionModel().select(pos.getRow() + 1, col);
                event.consume();
            }

        }

    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellFocusController.class.getName());

}

