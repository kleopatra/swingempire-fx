/*
 * Created on 18.04.2016
 *
 */
package de.swingempire.fx.scene.control.table.invcodebug;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

/**
 * Visual glitch on scrolling
 * http://stackoverflow.com/q/36674184/203657
 * 
 * -scroll to bottom
 * -deselect invite on 74, then on 73
 * - select again in same order
 * - expected: last three should be 73, 74, 75
 * - actual: spuriously, they are 73, 73, 74 (or 73, 74, 74)
 * 
 * OP reported as bug (via Webbug)
 */
public class FXMLDocumentController implements Initializable {
    private ResourceBundle resources = null;
    private CheckBox selectAllCheckBox;
    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Boolean> invitedCol;
    @FXML private TableColumn<Person, String> invCodeCol;
    @FXML private TableColumn<Person, String> nameCol;
    private final ObservableList<Person> persons
            = FXCollections.observableArrayList();

    @FXML private CheckBox invCodeCheckBox;
    @FXML private Label paddingLabel;
    @FXML private Spinner<Integer> paddingSpinner;
    private static final int MIN_PADDING = 1;
    private static final int MAX_PADDING = 5;
    private static final int INIT_PADDING = 3;
    @FXML private Label startAtLabel;
    @FXML private Spinner<Integer> startAtSpinner;
    private static final int MIN_STARTAT = 1;
    private static final int MAX_STARTAT = 10000;
    private static final int INIT_STARTAT = 1;
    private static final int INC_STARTAT = 50;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
//        initInvCodeCheckBox();
        initPersonTable();
//        initPaddingSpinner();
//        initStartAtSpinner();
        populatePersons();
    }

//    private void initInvCodeCheckBox(){
//        invCodeCheckBox.setSelected(true);
//        invCodeCheckBox.setOnAction((ActionEvent e) -> {
//            paddingSpinner.setDisable(!invCodeCheckBox.isSelected());
//            paddingLabel.setDisable(!invCodeCheckBox.isSelected());
//            startAtSpinner.setDisable(!invCodeCheckBox.isSelected());
//            startAtLabel.setDisable(!invCodeCheckBox.isSelected());
//            doInvCode();
//        });
//    }

    private void initPersonTable() {
//        invitedCol.setGraphic(getSelectAllCheckBox());
        invitedCol.setCellValueFactory(new PropertyValueFactory<>("invited"));
        invitedCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Integer param) {
                doInvCode();
// DEBUG my friggin prefix bug
System.out.println("------------------------------------------------------");
for (Person p : persons) {
    System.out.println(p.isInvited() + " " + p.getInvCode() + " : " + p.getName()
    );
}
                return persons.get(param).invitedProperty();
            }
        }));
        invCodeCol.setCellValueFactory(new PropertyValueFactory<>("invCode"));
//        selectAllCheckBox.setSelected(true);


        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
//  WORKS but has the onfocuslost cancels edits problem.  Oh well.
//        nameCol.setCellFactory(TextFieldTableCell.<Person>forTableColumn());
//        nameCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
//            @Override
//            public void handle(TableColumn.CellEditEvent<Person, String> event) {
//                ((Person) event.getTableView().getItems().get(event.getTablePosition().getRow())).setName(event.getNewValue());
//            }
//        });

        personTable.setItems(persons);
    }


//    private void initPaddingSpinner() {
//        paddingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_PADDING, MAX_PADDING, INIT_PADDING));
//        // set prefix to new padded string
//        paddingSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
//            @Override
//            public void changed(ObservableValue<? extends Integer> obs, Integer oldVal, Integer newVal) {
//                doInvCode();
//            }
//        });
//    }

//    private void initStartAtSpinner() {
//        startAtSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN_STARTAT, MAX_STARTAT, INIT_STARTAT, INC_STARTAT));
//        // set prefix to new padded string
//        startAtSpinner.valueProperty().addListener(new ChangeListener<Integer>() {
//            @Override
//            public void changed(ObservableValue<? extends Integer> obs, Integer oldVal, Integer newVal) {
//                doInvCode();
//            }
//        });
//    }

    private String makeInvCode(Integer invNumber) {
        String str = invNumber.toString();
        int padding = 3; //paddingSpinner.getValue();
        StringBuilder sb = new StringBuilder();
        for (int toPrepend = padding - str.length(); toPrepend > 0; toPrepend--) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString();
    }

    private void doInvCode() {
        int invCounter =  1; //startAtSpinner.getValue();
        for (Person p : persons) {
            if (/*invCodeCheckBox.isSelected() &&*/ p.isInvited()) {
                    p.setInvCode(makeInvCode(invCounter));
                    invCounter++;
            } else {
                p.setInvCode("");
            }
        }
        // fixes the visual glitch (see answer)
//        personTable.requestFocus();
    }


    public CheckBox getSelectAllCheckBox() {
        if (selectAllCheckBox == null) {
            final CheckBox selectAllCheckBox = new CheckBox();
            selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for (Person p : persons) {
                        p.setInvited(selectAllCheckBox.isSelected());
                    }
                }
            });
            this.selectAllCheckBox = selectAllCheckBox;
        }
        return selectAllCheckBox;
    }

    private void populatePersons() {
        for (int i = 0; i < 3; i++) {
            persons.addAll(
                    new Person(true, "", "Smith"),
                    new Person(true, "", "Johnson"),
                    new Person(true, "", "Williams"),
                    new Person(true, "", "Jones"),
                    new Person(true, "", "Brown"));
        }
    }
}

