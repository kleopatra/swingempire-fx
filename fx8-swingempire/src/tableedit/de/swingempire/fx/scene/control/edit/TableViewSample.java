/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.scene.control.edit;

import java.util.logging.Logger;

//import com.sun.javafx.css.Stylesheet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.DefaultStringConverter;

/**
 * Example from tutorial. 
 * 
 * Changes:
 * - removed hard-coded stage sizing
 * - use enhanced person bean, thus no need for onEditCommitHandler (removed)
 * - tabs represent variations in tableCells
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableViewSample extends Application {
    private final ObservableList<Person> data = Person.persons();
   
    XTableView<Person> xTable;
    
    /**
     * Creates and returns the content of the stage.
     * 
     * Creates one tab for each type of cellFactory.
     * 
     * @return
     */
    protected Parent createContent() {
        TabPane tabPane = new TabPane();
        // enhanced core textFieldCell with notion of terminate
        // Note: must use XTable!
        Callback xTextFieldCellFactory = p -> new XTextFieldTableCell<>(new DefaultStringConverter());
        addTab(tabPane, "xTextFieldCell", xTextFieldCellFactory, true);

        // plain TextFieldTableCell
        Callback<TableColumn<Person, String>, TableCell<Person, String>> 
            coreTextFieldCellFactory = TextFieldTableCell.forTableColumn();
        addTab(tabPane, "Core", coreTextFieldCellFactory);
        
        // enhanced editing cell from tutorial: commits on focusLost
        Callback<TableColumn<Person, String>, TableCell<Person, String>> editingCellFactory = 
                (TableColumn<Person, String> p) -> new EditingCell();
        addTab(tabPane, "Tutorial editingCell", editingCellFactory);

        BorderPane pane = new BorderPane(tabPane);    
        MenuBar bar = new MenuBar();
        Menu menu = new Menu("Actions");
        bar.getMenus().add(menu);
        MenuItem item = new MenuItem("terminate edit");
        menu.getItems().add(item);
        item.setAccelerator(KeyCombination.keyCombination("F3"));
        item.setOnAction(e ->{
            xTable.terminateEdit();
        });
        pane.setTop(bar);
        return pane;
    }

     
    /**
     * This is original of the tutorial example. 
     * No longer:
     * - changed to create/wire listener only once
     * 
     * Issues: 
     * - commits on focuslost to external control, not when clicking 
     *   inside table
     * - missing keyHandlers for esc/enter (it's an example, after all)
     */
    public static class EditingCell extends TableCell<Person, String> {
        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {

            // trying to go tricky: force commit instead of cancel
            // no luck? throws NPE on commit
            // probably because the tableView already
            // has a null editingCell
            // cancelEdit(false);
            super.cancelEdit();
            setText((String) getItem());
            setGraphic(null);
        }

        // public void cancelEdit(boolean really) {
        // if(really) {
        // new RuntimeException("who's calling? ").printStackTrace();
        // super.cancelEdit();
        // setText((String) getItem());
        // setGraphic(null);
        // } else {
        //
        // commitEdit();
        // }
        // }
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        // not really needed?
                        // must (?) be done in createTextField 
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        public void commitEdit() {
            commitEdit(textField.getText());
        }

        /**
         * Note: changed tutorial
         */
        private void createTextField() {
            // re-create the field on each edit, why?
//            textField = new TextField();//getString());
            // seems to work as expected (modulo not committing
            // when clicking inside the table into another row)
            if (textField == null) {
                textField = new TextField(); //getString());
            // missing keybindings to esc/enter
            textField.focusedProperty().addListener(
                    (ObservableValue<? extends Boolean> arg0, Boolean arg1,
                            Boolean arg2) -> {
                        if (!arg2) {
                            LOG.info("lost focus, editing? " + isEditing());
                            commitEdit();
//                             commitEdit(textField.getText());
                        }
                    });
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap()
                    * 2);
            }
            textField.setText(getString());
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }




    protected Tab addTab(TabPane pane, String title, Callback cellFactory) {
        boolean useExtended = false;
        return addTab(pane, title, cellFactory, useExtended);
    }

    protected Tab addTab(TabPane pane, String title, Callback cellFactory,
            boolean useExtended) {
        Tab tab = new Tab(title);
        pane.getTabs().add(tab); 
        tab.setContent(createTabContent(cellFactory, useExtended));
        return tab;
    }

    protected Parent createTabContent(
            Callback<TableColumn<Person, String>, TableCell<Person, String>> coreTextFieldCellFactory) {
        boolean useExtended = false;
        return createTabContent(coreTextFieldCellFactory, useExtended);
    }

    protected Parent createTabContent(
            Callback<TableColumn<Person, String>, TableCell<Person, String>> coreTextFieldCellFactory,
            boolean useExtended) {
        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));
        TableView<Person> table = createBaseTable(useExtended);
        // button allows to hide all columns, then impossible to show them again
        // http://stackoverflow.com/q/26141262/203657
        table.setTableMenuButtonVisible(true);
        setCellFactories(table, coreTextFieldCellFactory);
        final TextField addFirstName = new TextField();
        addFirstName.setPromptText("First Name");
        addFirstName.setMaxWidth(table.getColumns().get(0).getPrefWidth());
        final TextField addLastName = new TextField();
        addLastName.setMaxWidth(table.getColumns().get(1).getPrefWidth());
        addLastName.setPromptText("Last Name");
        final TextField addEmail = new TextField();
        addEmail.setMaxWidth(table.getColumns().get(2).getPrefWidth());
        addEmail.setPromptText("Email");
        final Button addButton = new Button("Add");
        addButton.setOnAction((ActionEvent e) -> {
            // check: change item is reflected in cell using BoundTableCell
   //            Person selected = table.getSelectionModel().getSelectedItem();
   //            if (selected == null) return;
   //            selected.setFirstName(selected.getFirstName() + "x");
            // original
            // except inserting at top to see problem with incorrect focus on
            // inserting
            // http://stackoverflow.com/q/25559022/203657
            // might be fixed by https://javafx-jira.kenai.com/browse/RT-37632
            data.add(0, new Person(addFirstName.getText() + "x", addLastName.getText() + "x",
                    addEmail.getText()+ "x"));
            addFirstName.clear();
            addLastName.clear();
            addEmail.clear();
            
        });
   
        HBox hb = new HBox(3);
        hb.getChildren().addAll(addFirstName, addLastName, addEmail, addButton);
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, hb);
        return vbox;
    }

    /**
     * Sets the given cellfactory to each column of the table.
     * 
     * @param table2
     * @param factory
     */
    private void setCellFactories(TableView<Person> table, Callback factory) {
        table.getColumns().stream().forEach(c -> c.setCellFactory(factory));
    }

    protected TableView<Person> createBaseTable(boolean useExtended) {
        TableView<Person> table = useExtended ? new XTableView<>() : new TableView<>();
        if (useExtended) {
            xTable = (XTableView) table;
        }
        // quick check for https://javafx-jira.kenai.com/browse/RT-18937
        // requirement: no selection
        // side-effect here: can't start edits
        // table.setSelectionModel(null);
        table.setEditable(true);
        
        // side-testing: focus not updated correctly
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getFocusModel().focusedCellProperty().addListener((p, oldValue, newValue)-> {
//            LOG.info("old/new " + oldValue + "\n  " + newValue);
//            LOG.info("anchor? " + table.getProperties().get("anchor"));
        });
        TableColumn<Person, String> firstNameCol = new TableColumn<>(
                "First Name");
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        firstNameCol.setMinWidth(100);
        firstNameCol
                .setCellValueFactory(new PropertyValueFactory<>("firstName"));
        // changed JW: removed editCommitHandlers
        // only need a custom handler if the bean doesn't expose the property
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);
        table.setItems(data);
        
        return table;
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Table View Sample");
        final Parent vbox = createContent();
        BorderPane pane = new BorderPane(vbox);
        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


    @SuppressWarnings("unused")
    static final Logger LOG = Logger.getLogger(TableViewSample.class
            .getName());
}