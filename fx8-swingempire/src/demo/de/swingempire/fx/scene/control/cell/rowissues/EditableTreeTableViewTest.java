/*
 * Created on 01.04.2016
 *
 */
package de.swingempire.fx.scene.control.cell.rowissues;

import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Regression guard:
 * https://bugs.openjdk.java.net/browse/JDK-8119094
 * (was: RT-29849)
 * 
 * Too many edit events fired:
 * Run the app.
 * Switch on editing
 * Try to edit some cell.
 * Sometimes, I see 2-3 coming onEditStart events. As many onEditCancel 
 * events I see, when I cancel edit.
 */
public class EditableTreeTableViewTest extends Application {

    TreeTableView<Person> treeTableView;
    TreeTableColumn<Person, String> firstNameCol;

    @Override
    public void start(Stage primaryStage) {

        initTreeTableView();

        HBox root = new HBox(10.0);
        root.getChildren().add(treeTableView);

        Button btnEditing = new Button("Enable editing");
        btnEditing.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                enableEditing();
            }
        });

        Button btnCustomEditing = new Button("Enable custom editing");
        btnCustomEditing.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                enableCustomEditing();
            }
        });

        VBox vb = new VBox(10.0);
        vb.getChildren().addAll(btnEditing, btnCustomEditing);

        root.getChildren().add(vb);

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle(VersionInfo.getRuntimeVersion());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initTreeTableView() {
        treeTableView = new TreeTableView<>();
        treeTableView.setMinSize(200, 150);
        treeTableView.setPrefSize(200, 200);
        treeTableView.setMaxSize(200, 250);
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        TreeItem<Person> root = new TreeItem<>(new Person("Anna"));
        root.setExpanded(true);
        treeTableView.setRoot(root);
        treeTableView.showRootProperty().set(true);

        firstNameCol = new TreeTableColumn<>("First name");
        firstNameCol.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Person, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Person, String> p) {
                return p.getValue().getValue().firstName;
            }
        });
        firstNameCol.setMinWidth(80d);

        treeTableView.getColumns().addAll(firstNameCol);

        Person p = new Person("Bob");
        treeTableView.getRoot().getChildren().add(new TreeItem<>(p));

        p = new Person("Cindy");
        treeTableView.getRoot().getChildren().add(new TreeItem<>(p));

        p = new Person("Zack");
        treeTableView.getRoot().getChildren().add(new TreeItem<>(p));
    }

    private void enableEditing() {
        treeTableView.setEditable(true);

        firstNameCol.setCellFactory(TextFieldTreeTableCell.<Person>forTreeTableColumn());
        firstNameCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditCommit");
                TreeItem<Person> rowValue = t.getRowValue();
                rowValue.getValue().firstName.set(t.getNewValue());
            }
        });

        firstNameCol.setOnEditStart(new EventHandler<CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditStart");
            }
        });

        firstNameCol.setOnEditCancel(new EventHandler<CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditCancel");
            }
        });
    }

    private void enableCustomEditing() {
        treeTableView.setEditable(true);

        firstNameCol.setCellFactory(new Callback<TreeTableColumn<Person, String>, TreeTableCell<Person, String>>() {
            @Override
            public TreeTableCell<Person, String> call(TreeTableColumn<Person, String> p) {
                return new TreeTableCellImpl();
            }
        });

        firstNameCol.setOnEditCommit(new EventHandler<TreeTableColumn.CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditCommit");
                TreeItem<Person> rowValue = t.getRowValue();
                rowValue.getValue().firstName.set(t.getNewValue());
            }
        });

        firstNameCol.setOnEditStart(new EventHandler<CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditStart");
            }
        });

        firstNameCol.setOnEditCancel(new EventHandler<CellEditEvent<Person, String>>() {
            @Override
            public void handle(CellEditEvent<Person, String> t) {
                System.out.println("OnEditCancel");
            }
        });
    }
}

class Person {

    public final SimpleStringProperty firstName;

    public Person(String name) {
        this.firstName = new SimpleStringProperty(name);
    }
}

class TreeTableCellImpl extends TreeTableCell<Person, String> {

    private TextField textField;

    public TreeTableCellImpl() {
        System.out.println("Ctor");
        textField = new TextField();
        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                switch (ke.getCode()) {
                    case ENTER:
                        commitEdit(getString());
                        break;
                    case ESCAPE:
                        cancelEdit();
                        break;
                }
            }
        });
    }

    @Override
    protected void updateItem(String t, boolean bln) {
        super.updateItem(t, bln);

        System.out.println("updateItem");

        if (isEmpty()) {
            setGraphic(null);
            setText(null);
        } else {
            if (isEditing()) {
                setGraphic(textField);
                setText(null);
            } else {
                setGraphic(getTableColumn().getGraphic());
                setText(getString());
            }
        }

    }

    @Override
    public void startEdit() {
        super.startEdit();

        System.out.println("Start edit");

        setText(null);
        setGraphic(textField);
        textField.setText(getString());
        textField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        System.out.println("Cancel edit");

        setGraphic(getTableColumn().getGraphic());
        setText((String) getItem());
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}