/*
 * Created on 01.11.2014
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.skin.TableRowSkin;

import de.swingempire.fx.control.TableViewSample.PlainTableCell;

/**
 * http://stackoverflow.com/q/26677385/203657
 * 
 * Cell selection broken for merged cells
 * 
 * 
 */
public class TableSkinTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 500, 200);
        stage.setScene(scene);

        ObservableList<Person> data = FXCollections.observableArrayList(
                new Person("This should span columns because it is long", "", ""),
                new Person("Isabella", "Johnson", "079155882"),
                new Person("Ethan", "Williams", "079155883"),
                new Person("Emma", "Jones", "079155884"),
                new Person("Michael", "Brown", "079155885")
        );

        TableView<Person> table = new TableView<>();

        table.getSelectionModel().setCellSelectionEnabled(true);
        
        table.setRowFactory(param -> new TableRow<Person>() {
            @Override
            protected Skin<?> createDefaultSkin() {
                return new TableRowSkin<TableSkinTest.Person>(this) {
                    @Override
                    protected void layoutChildren(double x, double y, double w, double h) {
                        checkState();
                        if (cellsMap.isEmpty()) return;
                        ObservableList<? extends TableColumnBase> visibleLeafColumns = getVisibleLeafColumns();
                        if (visibleLeafColumns.isEmpty()) {
                            super.layoutChildren(x, y, w, h);
                            return;
                        }
                        TableRow<TableSkinTest.Person> control = getSkinnable();
                        // layout the individual column cells
                        double width;
                        double height;

                        final double verticalPadding = snappedTopInset() + snappedBottomInset();
                        final double horizontalPadding = snappedLeftInset() + snappedRightInset();
                        final double controlHeight = control.getHeight();

                        int index = control.getIndex();

                        for (int column = 0, max = cells.size(); column < max; column++) {
                            TableCell<TableSkinTest.Person, ?> tableCell = cells.get(column);
                            width = snapSize(tableCell.prefWidth(-1)) - snapSize(horizontalPadding);
                            height = Math.max(controlHeight, tableCell.prefHeight(-1));
                            height = snapSize(height) - snapSize(verticalPadding);
                            if (index == 0 && column > 0) {
                                width = 0; // Hard code for simplification
                            } else if (index == 0) {
                                double width1 = snapSize(cells.get(1).getTableColumn().getWidth());
                                double width2 = snapSize(cells.get(2).getTableColumn().getWidth());
                                width += width1;
                                width += width2;
                            }
                            tableCell.resize(width, height);
                            tableCell.relocate(x, snappedTopInset());
                            x += width;
                        }
                    }
                };
            }
        });

        TableColumn<Person,String> firstNameCol = new TableColumn<>("First Name");
        TableColumn<Person,String> lastNameCol = new TableColumn<>("Last Name");
        TableColumn<Person,String> numberCol = new TableColumn<>("Number");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));

        Callback factory = p -> {
            int index = table.getItems().indexOf(p);
//            return 0 == index ? new SpanTableCell<Person, Object>() : new PlainTableCell<Person, Object> ();
            return new SpanTableCell();
        };

        table.getColumns().addAll(firstNameCol, lastNameCol, numberCol);
        table.getColumns().forEach(col -> col.setPrefWidth(100));
        table.getColumns().forEach(col -> col.setCellFactory(factory));
        table.setItems(data);

        pane.setCenter(table);

        stage.show();
    }

    public static class SpanTableCell<S, T> extends PlainTableCell<S, T> {
        
        private ListChangeListener<TablePosition> selectedListener = c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    updateSelection();
                }
            }
        };
        
        private WeakListChangeListener weakSelectedListener = new WeakListChangeListener<>(selectedListener);
        
        private ChangeListener<TableView> tableViewListener = (t, old, value) -> {
            if (old != null && old.getSelectionModel() != null) {
                old.getSelectionModel().getSelectedIndices().removeListener(weakSelectedListener);
            }
            if (value != null && value.getSelectionModel() != null) {
                value.getSelectionModel().getSelectedIndices().addListener(weakSelectedListener);
            }
            updateSelection();
        };
        
        private WeakChangeListener weakTableViewListener = new WeakChangeListener(tableViewListener);
        
        public SpanTableCell() {
            tableViewProperty().addListener(weakTableViewListener);
        }
        
        private void updateSelection() {
            // super will handle
            if (!isInCellSelectionMode() || getIndex() != 0)
                return;
            // TableViewSelectionModel doesn't support row selection in
            // cellSelectionMode
            TableViewSelectionModel<S> selectionModel = getTableView()
                    .getSelectionModel();
            boolean rowSelected = false; // selectionModel.isSelected(getIndex());
            ObservableList<TableColumn<S, ?>> columns = getTableView()
                    .getVisibleLeafColumns();
            for (TableColumn<S, ?> tableColumn : columns) {
                rowSelected = rowSelected
                        || selectionModel.isSelected(getIndex(), tableColumn);
            }
            boolean finalSelected = rowSelected;
            Platform.runLater(() -> {
                // need to defer, super gets into the way
                updateSelected(finalSelected);
            });
        }
        
        private boolean isInCellSelectionMode() {
            TableView<S> tableView = getTableView();
            if (tableView == null) return false;
            TableSelectionModel<S> sm = tableView.getSelectionModel();
            return sm != null && sm.isCellSelectionEnabled();
        }
        
    }
    
    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty number;
        private Person(String fName, String lName, String nNumber) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.number = new SimpleStringProperty(nNumber);
        }
        public String getFirstName() {return firstName.get();}
        public void setFirstName(String fName) {firstName.set(fName);}
        public String getLastName() {return lastName.get();}
        public void setLastName(String fName) {lastName.set(fName);}
        public String getNumber() {return number.get();}
        public void setNumber(String number) {this.number.set(number);}
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(TableSkinTest.class
            .getName());
}