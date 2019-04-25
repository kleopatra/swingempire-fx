/*
 * Created on 24.04.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowValidMarker extends Application {

    public class RowTest {

        private SimpleIntegerProperty a;
        private SimpleIntegerProperty b;
        private SimpleBooleanProperty validRow;

        public RowTest(int a, int b) {
            this.a = new SimpleIntegerProperty(a);
            this.b = new SimpleIntegerProperty(b);

            this.validRow = new SimpleBooleanProperty();

            this.validRow.bind(Bindings.createBooleanBinding(() -> this.a.get() == this.b.get(), this.a, this.b));
        }

        public int getA() {
            return a.get();
        }

        public SimpleIntegerProperty aProperty() {
            return a;
        }

        public void setA(int a) {
            this.a.set(a);
        }

        public int getB() {
            return b.get();
        }

        public SimpleIntegerProperty bProperty() {
            return b;
        }

        public void setB(int b) {
            this.b.set(b);
        }

        public boolean isValidRow() {
            return validRow.get();
        }

        public SimpleBooleanProperty validRowProperty() {
            return validRow;
        }

        public void setValidRow(boolean validRow) {
            this.validRow.set(validRow);
        }

        @Override
        public String toString() {
            return "A: " + getA() + " B: " + getB() + " " + isValidRow();
        }
        
        
    }

    TableView<RowTest> tableView;
    ObservableList<RowTest> data;
    
    private void setColumnsEditable() {
        tableView.setRowFactory(tv -> new TableRow<RowTest>() {
            
            
            @Override
            protected boolean isItemChanged(RowTest oldItem, RowTest newItem) {
                boolean changed = super.isItemChanged(oldItem, newItem);
                if (oldItem != null && newItem != null && newItem == oldItem) {
                    changed = changed || (oldItem.isValidRow() != newItem.isValidRow());
                }
                return changed;
            }

            @Override
            public void updateItem(RowTest item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (item.isValidRow()) {
                    LOG.info("" + getIndex() + item);
                    setStyle("-fx-background-color: LightGreen; -fx-text-fill: Black;");
                } else if (!item.isValidRow()) {
                    LOG.info("" + item);
                    setStyle("-fx-background-color: Red; -fx-text-fill: Black;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<RowTest, Integer> colA =  new TableColumn<>("value A");
        colA.setCellValueFactory(new PropertyValueFactory<>("a"));
        
        TableColumn<RowTest, Integer> colB =  new TableColumn<>("value B");
        colB.setCellValueFactory(new PropertyValueFactory<>("b"));
        
        colB.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
            @Override
            public String toString(java.lang.Integer object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public java.lang.Integer fromString(String string) {
                return java.lang.Integer.parseInt(string);
            }

        }));
        tableView.getColumns().addAll(colA, colB);
//        colB.setOnEditCommit(
//                (TableColumn.CellEditEvent<RowTest, Integer> t) -> {
//                    ((RowTest) t.getTableView().getItems().get(
//                            t.getTablePosition().getRow())
//                    ).setB(t.getNewValue());
//                });
//
        data.addListener(new ListChangeListener<RowTest>() {
            @Override
            public void onChanged(Change<? extends RowTest> c) {
                while (c.next()) {
                    FXUtils.prettyPrint(c);
                    if (c.wasUpdated()) {
                   }
                }
            }
        });
    }

    private void populateData() {
        data = FXCollections.observableArrayList(e -> new Observable[] {e.validRowProperty()});
        
        data.add(new RowTest(1, 1));
        data.add(new RowTest(2, 0));

        data.get(1).validRowProperty().addListener(o -> LOG.info("getting change: " + o));
        tableView.setItems(data);
    }

    private Parent createContent() {
        tableView = new TableView<>();
        tableView.setEditable(true);
        populateData();
        setColumnsEditable();
        
        BorderPane content =  new BorderPane(tableView);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableRowValidMarker.class.getName());

}
