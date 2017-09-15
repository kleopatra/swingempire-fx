/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package test.tableeditcore;


import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.ChoiceBoxListCell;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ChoiceBoxTreeCell;
import javafx.scene.control.cell.ChoiceBoxTreeTableCell;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.ComboBoxTreeCell;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * from fix in fx10 to
 * https://bugs.openjdk.java.net/browse/JDK-8089514
 * webrev http://cr.openjdk.java.net/~jgiles/8089514.1/
 * 
 * Can't really test, too many changes too deep in bowels..
 */
public class HelloCommitOnFocusLoss extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override public void start(Stage stage) {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(buildTableViewTab());
        tabPane.getTabs().add(buildListViewTab());
        tabPane.getTabs().add(buildTreeViewTab());
        tabPane.getTabs().add(buildTreeTableViewTab());

        StackPane stackPane = new StackPane(tabPane);

        final Scene scene = new Scene(stackPane, 875, 300);
        scene.setFill(Color.LIGHTGRAY);

        stage.setTitle("Hello Commit On Focus Loss");
        stage.setScene(scene);
        stage.show();
    }



    /*****************************************************************************************************
     *
     * Utility methods
     *
     ****************************************************************************************************/

    private Label createLabel(String s) {
        Label label = new Label(s);
        label.setWrapText(true);
        return label;
    }

    private Button createEditButton(TableView table) {
        Button btn = new Button("Edit (3, 0)");
        btn.setOnAction(e -> {
            table.edit(3, (TableColumn) table.getColumns().get(0));
        });
        return btn;
        
    }
    private Button createDumpButton(Control c) {
        Button btn = new Button("Print to console");
        btn.setOnAction(e -> dumpToConsole(c));
        return btn;
    }

    private void dumpToConsole(Control c) {
        System.out.println("\n============================================");
        System.out.println("Printing content of control: " + c);
        if (c instanceof ListView) {
            ((ListView)c).getItems().stream().forEach(System.out::println);
        } else if (c instanceof TableView) {
            ((TableView)c).getItems().stream().forEach(System.out::println);
        } else if (c instanceof TreeView) {
            TreeView treeView = (TreeView) c;
            System.out.println(treeView.getRoot());
            treeView.getRoot().getChildren().stream().forEach(System.out::println);
        } else if (c instanceof TreeTableView) {
            TreeTableView treeTableView = (TreeTableView) c;
            System.out.println(treeTableView.getRoot());
            treeTableView.getRoot().getChildren().stream().forEach(System.out::println);
        }
        System.out.println("============================================");
    }

    private ObservableList<Person> createTestData() {
        ObservableList<Person> data = FXCollections.observableArrayList();
        data.addAll(
                new Person("Jonathan","Smith" ),
                new Person("Julia","Johnson" ),
                new Person("Henry","Williams" ),
                new Person("Pippa","Jones" ),
                new Person("Susan","Brown" ),
                new Person("Ian","Davis" ),
                new Person("Matthew","Miller" ),
                new Person("Hannah","Wilson" )
        );
        return data;
    }


    /*****************************************************************************************************
     *
     * ListView
     *
     ****************************************************************************************************/

    private Tab buildListViewTab() {
        TabPane innerTabPane = new TabPane();
        innerTabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        innerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        innerTabPane.setPadding(new Insets(10));

        innerTabPane.getTabs().add(buildSimpleListViewTab("ChoiceBox", ChoiceBoxListCell.forListView("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleListViewTab("ComboBox", ComboBoxListCell.forListView("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleListViewTab("TextField", TextFieldListCell.forListView()));
        innerTabPane.getTabs().add(buildSimpleListViewTab("Custom", listView -> new NestedTextFieldListCell()));

        Tab tab = new Tab("ListView");
        tab.setContent(innerTabPane);
        return tab;
    }

    private Tab buildSimpleListViewTab(String title, Callback<ListView<String>, ListCell<String>> cellFactory) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setHgap(5);
        grid.setVgap(5);

        ObservableList<String> data = FXCollections.observableArrayList();
        data.addAll(IntStream.range(0, 10).mapToObj(i -> "Row " + i).collect(Collectors.toList()));

        // simple textfield list view
        // Works because the TextFieldListCell supports the new API, and because the TextField is the root graphic
        // in the cell.
        final ListView<String> textFieldListView = new ListView<>(data);
        textFieldListView.setEditable(true);
        textFieldListView.setMaxHeight(Double.MAX_VALUE);
        textFieldListView.setCellFactory(cellFactory);
        textFieldListView.addEventHandler(ListView.editStartEvent(), e -> System.out.println("On Edit Start: " + e));
        textFieldListView.addEventHandler(ListView.editCancelEvent(), e -> System.out.println("On Edit Cancel: " + e));
        textFieldListView.addEventHandler(ListView.editCommitEvent(), e -> System.out.println("On Edit Commit: " + e));

        grid.add(createLabel("This ListView has a cell factory. It should commit in the three " +
                        "primary ways: on clicking another row, on clicking the dummy button, and on tabbing to the dummy button."),
                0, 0, 2, 1);
        grid.add(textFieldListView, 0, 1);
        GridPane.setVgrow(textFieldListView, Priority.ALWAYS);
        GridPane.setHgrow(textFieldListView, Priority.ALWAYS);
        // --- simple listview

        // control buttons
        grid.add(new VBox(10, new Button("Dummy"), createDumpButton(textFieldListView)), 1, 1);

        Tab tab = new Tab(title);
        tab.setContent(grid);
        return tab;
    }

    private static class NestedTextFieldListCell extends ListCell<String> {
        final TextField textField;
        final HBox container;

        public NestedTextFieldListCell() {
            textField = new TextField();
            Rectangle rectangle = new Rectangle(10, 10, Color.GREEN);
            container = new HBox(10, textField, rectangle);
        }

        /** {@inheritDoc} */
        @Override public void startEdit() {
            if (! isEditable() || ! getListView().isEditable()) {
                return;
            }
            super.startEdit();

            if (isEditing()) {
                textField.setText(getText());
                setText("");
                setGraphic(container);
            }
        }

        /** {@inheritDoc} */
        @Override public void cancelEdit() {
            super.cancelEdit();
            setText(textField.getText());
            setGraphic(null);
        }

        /** {@inheritDoc} */
        @Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    textField.setText(getText());
                    setText(null);
                    setGraphic(container);
                } else {
                    setText(getItem() == null ? "" : getItem().toString());
                    setGraphic(null);
                }
            }
        }

        protected Optional<String> getEditorValue() {
            return Optional.of(textField.getText());
        }

        protected boolean validateEditorValue(String newValue) {
            return true;
        }
    }


    /*****************************************************************************************************
     *
     * TableView
     *
     ****************************************************************************************************/

    private Tab buildTableViewTab() {
        TabPane innerTabPane = new TabPane();
        innerTabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        innerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        innerTabPane.setPadding(new Insets(10));

        innerTabPane.getTabs().add(buildSimpleTableViewTab("TextField", TextFieldTableCellJon.forTableColumn()));
        innerTabPane.getTabs().add(buildSimpleTableViewTab("Custom", tableView -> new NestedTextFieldTableCell()));
        innerTabPane.getTabs().add(buildSimpleTableViewTab("ChoiceBox", ChoiceBoxTableCell.forTableColumn("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleTableViewTab("ComboBox", ComboBoxTableCell.forTableColumn("Option 1", "Option 2", "Option 3")));

        Tab tab = new Tab("TableView");
        tab.setContent(innerTabPane);
        return tab;
    }

    private Tab buildSimpleTableViewTab(String title, Callback<TableColumn<Person, String>, TableCell<Person, String>> callback) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setHgap(5);
        grid.setVgap(5);

        ObservableList<Person> data = createTestData();

        TableColumn<Person, String> firstNameCol = new TableColumn<>();
        firstNameCol.setText("First");
        firstNameCol.setCellFactory(callback);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.addEventHandler(TableColumn.editStartEvent(), e -> System.out.println("On Edit Start: " + e));
        firstNameCol.addEventHandler(TableColumn.editCancelEvent(), e -> System.out.println("On Edit Cancel: " + e));
        firstNameCol.addEventHandler(TableColumn.editCommitEvent(),e -> System.out.println("On Edit Commit: " + e));

        // simple table view
        final TableViewJon<Person> tableView = new TableViewJon<>(data);
        tableView.setSelectionModel(null);
        tableView.getColumns().addAll(firstNameCol);
        tableView.setEditable(true);
        tableView.setMaxHeight(Double.MAX_VALUE);

        grid.add(createLabel("This TableView has a cell factory. It should commit in the three " +
                        "primary ways: on clicking another row, on clicking the dummy button, and on tabbing to the dummy button."),
                0, 0, 2, 1);
        grid.add(tableView, 0, 1);
        GridPane.setVgrow(tableView, Priority.ALWAYS);
        GridPane.setHgrow(tableView, Priority.ALWAYS);
        // --- simple tableview

        // control buttons
        grid.add(new VBox(10, new Button("Dummy"), createEditButton(tableView), createDumpButton(tableView))
                , 1, 1);

        
        Tab tab = new Tab(title);
        tab.setContent(grid);
        return tab;
    }

    private static class NestedTextFieldTableCell extends TableCellJon<Person, String> {
        final TextField textField;
        final HBox container;

        public NestedTextFieldTableCell() {
            textField = new TextField();
            Rectangle rectangle = new Rectangle(10, 10, Color.GREEN);
            container = new HBox(10, textField, rectangle);
        }

        /** {@inheritDoc} */
        @Override public void startEdit() {
            if (! isEditable() || ! getTableView().isEditable()) {
                return;
            }
            super.startEdit();

            if (isEditing()) {
                textField.setText(getText());
                setText("");
                setGraphic(container);
            }
        }

        /** {@inheritDoc} */
        @Override public void cancelEdit() {
            super.cancelEdit();
            setText(textField.getText());
            setGraphic(null);
        }

        /** {@inheritDoc} */
        @Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    textField.setText(getText());
                    setText(null);
                    setGraphic(container);
                } else {
                    setText(getItem() == null ? "" : getItem().toString());
                    setGraphic(null);
                }
            }
        }

        protected Optional<String> getEditorValue() {
            return Optional.of(textField.getText());
        }

        protected boolean validateEditorValue(String newValue) {
            return true;
        }
    }


    /*****************************************************************************************************
     *
     * TreeView
     *
     ****************************************************************************************************/

    private Tab buildTreeViewTab() {
        TabPane innerTabPane = new TabPane();
        innerTabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        innerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        innerTabPane.setPadding(new Insets(10));

        innerTabPane.getTabs().add(buildSimpleTreeViewTab("ChoiceBox", ChoiceBoxTreeCell.forTreeView("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleTreeViewTab("ComboBox", ComboBoxTreeCell.forTreeView("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleTreeViewTab("TextField", TextFieldTreeCell.forTreeView()));
        innerTabPane.getTabs().add(buildSimpleTreeViewTab("Custom", treeView -> new NestedTextFieldTreeCell()));

        Tab tab = new Tab("TreeView");
        tab.setContent(innerTabPane);
        return tab;
    }

    private Tab buildSimpleTreeViewTab(String title, Callback<TreeView<String>, TreeCell<String>> callback) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setHgap(5);
        grid.setVgap(5);

        TreeItem<String> root = new TreeItem<>("Root");
        root.setExpanded(true);
        IntStream.range(1, 10).forEach(i -> root.getChildren().add(new TreeItem<>("Child " + i)));

        TreeView<String> treeView = new TreeView<>(root);
        treeView.setEditable(true);
        treeView.setMaxHeight(Double.MAX_VALUE);
        treeView.setCellFactory(callback);
        treeView.addEventHandler(ListView.editStartEvent(), e -> System.out.println("On Edit Start: " + e));
        treeView.addEventHandler(ListView.editCancelEvent(), e -> System.out.println("On Edit Cancel: " + e));
        treeView.addEventHandler(ListView.editCommitEvent(), e -> System.out.println("On Edit Commit: " + e));

        grid.add(createLabel("This TreeView has a cell factory. It should commit in the three " +
                        "primary ways: on clicking another row, on clicking the dummy button, and on tabbing to the dummy button."),
                0, 0, 2, 1);
        grid.add(treeView, 0, 1);
        GridPane.setVgrow(treeView, Priority.ALWAYS);
        GridPane.setHgrow(treeView, Priority.ALWAYS);

        // control buttons
        grid.add(new VBox(10, new Button("Dummy"), createDumpButton(treeView)), 1, 1);

        Tab tab = new Tab(title);
        tab.setContent(grid);
        return tab;
    }

    private static class NestedTextFieldTreeCell extends TreeCell<String> {
        final TextField textField;
        final HBox container;

        public NestedTextFieldTreeCell() {
            textField = new TextField();
            Rectangle rectangle = new Rectangle(10, 10, Color.GREEN);
            container = new HBox(10, textField, rectangle);
        }

        /** {@inheritDoc} */
        @Override public void startEdit() {
            if (! isEditable() || ! getTreeView().isEditable()) {
                return;
            }
            super.startEdit();

            if (isEditing()) {
                textField.setText(getText());
                setText("");
                setGraphic(container);
            }
        }

        /** {@inheritDoc} */
        @Override public void cancelEdit() {
            super.cancelEdit();
            setText(textField.getText());
            setGraphic(null);
        }

        /** {@inheritDoc} */
        @Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    textField.setText(getText());
                    setText(null);
                    setGraphic(container);
                } else {
                    setText(getItem() == null ? "" : getItem().toString());
                    setGraphic(null);
                }
            }
        }

        protected Optional<String> getEditorValue() {
            return Optional.of(textField.getText());
        }

        protected boolean validateEditorValue(String newValue) {
            return true;
        }
    }



    /*****************************************************************************************************
     *
     * TreeTableView
     *
     ****************************************************************************************************/

    private Tab buildTreeTableViewTab() {
        TabPane innerTabPane = new TabPane();
        innerTabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        innerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        innerTabPane.setPadding(new Insets(10));

        innerTabPane.getTabs().add(buildSimpleTreeTableViewTab("ChoiceBox", ChoiceBoxTreeTableCell.forTreeTableColumn("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleTreeTableViewTab("ComboBox", ComboBoxTreeTableCell.forTreeTableColumn("Option 1", "Option 2", "Option 3")));
        innerTabPane.getTabs().add(buildSimpleTreeTableViewTab("TextField", TextFieldTreeTableCell.forTreeTableColumn()));
        innerTabPane.getTabs().add(buildSimpleTreeTableViewTab("Custom", treeTableView -> new NestedTextFieldTreeTableCell()));

        Tab tab = new Tab("TreeTableView");
        tab.setContent(innerTabPane);
        return tab;
    }

    private Tab buildSimpleTreeTableViewTab(String title, Callback<TreeTableColumn<Person, String>, TreeTableCell<Person, String>> callback) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.setHgap(5);
        grid.setVgap(5);

        TreeItem<Person> root = new TreeItem<>(new Person("People", ""));
        root.setExpanded(true);
        createTestData().stream().map(TreeItem::new).forEach(root.getChildren()::add);

        TreeTableColumn<Person, String> firstNameCol = new TreeTableColumn<>();
        firstNameCol.setText("First");
        firstNameCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("firstName"));
        firstNameCol.setCellFactory(callback);
        firstNameCol.addEventHandler(TableColumn.editStartEvent(), e -> System.out.println("On Edit Start: " + e));
        firstNameCol.addEventHandler(TableColumn.editCancelEvent(), e -> System.out.println("On Edit Cancel: " + e));
        firstNameCol.addEventHandler(TableColumn.editCommitEvent(),e -> System.out.println("On Edit Commit: " + e));

        final TreeTableView<Person> treeTableView = new TreeTableView<>(root);
        treeTableView.getColumns().addAll(firstNameCol);
        treeTableView.setEditable(true);
        treeTableView.setMaxHeight(Double.MAX_VALUE);

        grid.add(createLabel("This TreeView has a cell factory. It should commit in the three " +
                        "primary ways: on clicking another row, on clicking the dummy button, and on tabbing to the dummy button."),
                0, 0, 2, 1);
        grid.add(treeTableView, 0, 1);
        GridPane.setVgrow(treeTableView, Priority.ALWAYS);
        GridPane.setHgrow(treeTableView, Priority.ALWAYS);

        // control buttons
        grid.add(new VBox(10, new Button("Dummy"), createDumpButton(treeTableView)), 1, 1);

        Tab tab = new Tab("Simple");
        tab.setContent(grid);
        return tab;
    }

    private static class NestedTextFieldTreeTableCell extends TreeTableCell<Person, String> {
        final TextField textField;
        final HBox container;

        public NestedTextFieldTreeTableCell() {
            textField = new TextField();
            Rectangle rectangle = new Rectangle(10, 10, Color.GREEN);
            container = new HBox(10, textField, rectangle);
        }

        /** {@inheritDoc} */
        @Override public void startEdit() {
            if (! isEditable() || ! getTreeTableView().isEditable()) {
                return;
            }
            super.startEdit();

            if (isEditing()) {
                textField.setText(getText());
                setText("");
                setGraphic(container);
            }
        }

        /** {@inheritDoc} */
        @Override public void cancelEdit() {
            super.cancelEdit();
            setText(textField.getText());
            setGraphic(null);
        }

        /** {@inheritDoc} */
        @Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    textField.setText(getText());
                    setText(null);
                    setGraphic(container);
                } else {
                    setText(getItem() == null ? "" : getItem().toString());
                    setGraphic(null);
                }
            }
        }

        protected Optional<String> getEditorValue() {
            return Optional.of(textField.getText());
        }

        protected boolean validateEditorValue(String newValue) {
            return true;
        }
    }
}

