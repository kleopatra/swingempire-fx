/*
 * Created on 07.10.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.cell.TableViewCheckEditable.TestModel;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * https://stackoverflow.com/q/52675725/203657
 * make editable depend on state of checkbox (is also property)
 * 
 * This is the solution (provided by OP) with additional 
 * problem: reported to need listening to selected in the callback,
 * not reproducable, working as expected
 * 
 */
public class TableViewCheckEditable extends Application {

    private Parent createContent() {

        //********************************************************************************************
        //Declare the TableView and its underlying ObservableList and change listener
        TableView<TestModel> table = new TableView<>();

        ObservableList<TestModel> olTestModel = FXCollections.observableArrayList(testmodel -> new Observable[] {
                testmodel.checkboxProperty()
        });

        olTestModel.addListener((ListChangeListener.Change<? extends TestModel > c) -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    //...
                    System.out.println(c.getList().get(c.getFrom()));
                } 
            }
        });

        olTestModel.add(new TestModel(false, "A"));
        olTestModel.add(new TestModel(false, "B"));
        olTestModel.add(new TestModel(false, "C"));

        table.setItems(olTestModel);

        //********************************************************************************************
        //Declare the text column whose editability needs to change depending on whether or
        //not the CheckBox is ticked
        TableColumn<TestModel, String> colText = new TableColumn<>("text");
        colText.setCellValueFactory(cellData -> cellData.getValue().textProperty());
        //Don't setEditable() to false here, otherwise updateItem(), updateIndex() and startEdit() won't fire
        colText.setEditable(true);
        colText.setCellFactory(cb -> {

            DefaultStringConverter converter = new DefaultStringConverter();
            TableCell<TestModel, String> cell = new TextFieldTableCell<TestModel, String>(converter) {
                //Per https://stackoverflow.com/questions/52528697/treetableview-setting-a-row-not-editable
                //Set the TextFieldTableCell's editability by either:
                //a) binding its editableProperty() to the checkboxProperty() in updateItem() and/or updateIndex(); OR
                //b) overriding startEdit() to do nothing unless the checkbox has been selected.

                //Per https://stackoverflow.com/questions/35279377/javafx-weird-keyeventbehavior?rq=1
                //When a cell is reused for a different item, updateItem() is invoked.
                //When the index of the cell changes eg. because a row was inserted, updateIndex() is invoked.
                //If the cell is reused AND its index is changed at the same time, there is no guarantee of the order in which
                //updateItem() and updateIndex() will be invoked.
                //So, to ensure the cell is updated consistently when it's likely the the cell will be reused AND its index
                //changed at the same time, it's best to delegate the the updated to a private method that's called from
                //both updateItem() and updateIndex().

                //==> Seems safest to trigger the binding of the editableProperty() from both updateItem() and updateIndex(),
                //as both cell re-use and index changes are likely to happen in my app.
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    doUpdate(item, getIndex(), empty);
                }

                @Override
                public void updateIndex(int index) {
                    super.updateIndex(index);
                    doUpdate(getItem(), index, isEmpty());
                }

                private void doUpdate(String item, int index, boolean empty) {
                    if ( empty || index == getTableView().getItems().size() ) {
                        setText(null);
                    } else {
                        BooleanProperty checkboxProperty = getTableView().getItems().get(getIndex()).checkboxProperty();
                        editableProperty().bind(checkboxProperty);
                    }
                }

            };

            return cell;

        });


        //********************************************************************************************
        //Declare the CheckBox column
        TableColumn<TestModel, Boolean> colCheckbox = new TableColumn<>("checkbox");

        colCheckbox.setCellValueFactory(cellData -> cellData.getValue().checkboxProperty());

        colCheckbox.setCellFactory(CheckBoxTableCell.forTableColumn(colCheckbox));
//        colCheckbox.setCellFactory((TableColumn<TestModel, Boolean> cb) -> {
//
//            CheckBoxTableCell<TestModel, Boolean> cbCell = new CheckBoxTableCell<>();
//            BooleanProperty selected = new SimpleBooleanProperty();
//
//            cbCell.setSelectedStateCallback((Integer index) -> selected);
//
//            selected.addListener((ObservableValue<? extends Boolean> obs, Boolean wasSelected, Boolean isSelected) -> {
//                //Set the value in the data model
//                olTestModel.get(cbCell.getIndex()).setCheckbox(isSelected);
//            });
//
//            return cbCell;
//        });

        //********************************************************************************************
        //Column to show what's actually in the TableView's data model for the checkbox
        TableColumn<TestModel, Boolean> colDMVal = new TableColumn<>("data model value");
        colDMVal.setCellValueFactory(cb -> cb.getValue().checkboxProperty());
        colDMVal.setEditable(false);

        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setEditable(true);

        table.getColumns().add(colCheckbox);
        table.getColumns().add(colDMVal);
        table.getColumns().add(colText);

        BorderPane content = new BorderPane(table);

        return content;

    }

    public class TestModel {

        private BooleanProperty checkbox;
        private StringProperty text;

        public TestModel() {
            this(false, "");
        }

        public TestModel(
            boolean checkbox,
            String text
        ) {
            this.checkbox = new SimpleBooleanProperty(checkbox);
            this.text = new SimpleStringProperty(text);
        }

        public Boolean getCheckbox() {
            return checkbox.get();
        }

        public void setCheckbox(boolean checkbox) {
            this.checkbox.set(checkbox);
        }

        public BooleanProperty checkboxProperty() {
            return checkbox;
        }

        public String getText() {
            return text.get();
        }

        public void setText(String text) {
            this.text.set(text);
        }

        public StringProperty textProperty() {
            return text;
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle("Test");
        stage.setWidth(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}

