package de.swingempire.fx.scene.control.cell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * TestCase for https://javafx-jira.kenai.com/browse/RT-22463
 * automatic table refresh when replacing items with
 * list that equals the old.<p>
 * 
 * added custom rowfactory with overridden isItemChanged plus
 * custom TableRowSkin.
 * 
 * @author Jonathan
 */
public class TestCaseRT_22463 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) {
        primaryStage.setTitle("Test case");

        final TableView<Person> table = new TableView<>();
        table.setTableMenuButtonVisible(true);
        TableColumn c1 = new TableColumn("Id");
        TableColumn c2 = new TableColumn("Name");
        c1.setCellValueFactory(new PropertyValueFactory<Person, Long>("id"));
        c2.setCellValueFactory(new PropertyValueFactory<Person, String>("name"));
        
        Callback<TableView<Person>, TableRow<Person>> rowFactory = p -> {
            return new TableRow<Person>() {

                @Override
                protected boolean isItemChanged(Person oldItem,
                        Person newItem) {
                    return oldItem != newItem;
                }
                
                // needs help of skin to trigger update of cells
                @Override
                protected Skin<?> createDefaultSkin() {
                    return new TableRowSkinX(this);
                }

                
            };
        };
        table.setRowFactory(rowFactory);


        table.getColumns().addAll(c1, c2);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(arg0 -> {
// table.getItems().clear();
            table.setItems(getPerson2());
// table.refresh();
        });

        BorderPane pane = new BorderPane();
        pane.setCenter(table);
        pane.setBottom(refreshButton);

        primaryStage.setScene(new Scene(pane, 400, 400));
        primaryStage.show();

        table.setItems(getPerson1());

    }

    private ObservableList<Person> getPerson1() {
        return FXCollections.observableArrayList(
                new Person(1l, "name1"),
                new Person(2l, "name2"));
    }

    private ObservableList<Person> getPerson2() {
        return FXCollections.observableArrayList(
                new Person(1l, "updated name1"),
                new Person(2l, "updated name2"));
    }




    public static class Person {

        private Long id;
        private String name;

        public Person(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Person other = (Person) obj;
            if (this.id != other.id) {
                return false;
            }
            return true;
        }

        @Override public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
            return hash;
        }
    }
}

