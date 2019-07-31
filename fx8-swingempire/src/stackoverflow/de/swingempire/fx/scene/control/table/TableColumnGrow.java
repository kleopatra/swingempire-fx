/*
 * Created on 29.07.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57245216/203657
 * resize one column to fill all available width
 * 
 * This is the answer of Sai
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnGrow extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        HGrowTableView<Person> tableView = new HGrowTableView<>();
        TableColumn<Person, String> colFirstName = new HGrowTableColumn<>("First Name");
        TableColumn<Person, String> colLastName = new HGrowTableColumn<>("Last Name");
        TableColumn<Person, String> colLastName2 = new HGrowTableColumn<>("Last Name");

        colFirstName.setCellValueFactory(tf -> tf.getValue().firstNameProperty());
        colLastName.setCellValueFactory(tf -> tf.getValue().lastNameProperty());
        colLastName2.setCellValueFactory(tf -> tf.getValue().lastNameProperty());

        tableView.setColumnToResize(colLastName);
        tableView.getColumns().addAll(colFirstName, colLastName, colLastName2);

        tableView.getItems().addAll(
                new Person("Martin", "Brody"),
                new Person("Matt", "Hooper"),
                new Person("Samll", "Quint")
        );
        
        root.getChildren().add(tableView);

        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("TableColumnGrow Sample");
        primaryStage.show();

    }

    /**
     * Custom Table View
     *
     * @param <S>
     */
    class HGrowTableView<S> extends TableView<S> {
        TableColumn<?, ?> columnToResize;
        ChangeListener<Number> tableWidthListener = (obs, old, width) -> {
            double otherColsWidth = getColumns().stream().filter(tc -> tc != columnToResize).mapToDouble(tc -> tc.getWidth()).sum();
            double padding = getPadding().getLeft() + getPadding().getRight();
            columnToResize.setPrefWidth(width.doubleValue() - otherColsWidth - padding);
        };

        public void setColumnToResize(TableColumn<?, ?> columnToResize) {
            widthProperty().removeListener(tableWidthListener); // Ensuring to remove any previous listener
            this.columnToResize = columnToResize;
            if (this.columnToResize != null) {
                widthProperty().addListener(tableWidthListener);
            }
        }

        public TableColumn<?, ?> getColumnToResize() {
            return columnToResize;
        }
    }

    /**
     * Custom TableColumn
     *
     * @param <S>
     * @param <T>
     */
    class HGrowTableColumn<S, T> extends TableColumn<S, T> {
        public HGrowTableColumn(String title) {
            super(title);
            init();
        }

        private void init() {
            widthProperty().addListener((obs, old, width) -> {
                if (getTableView() instanceof HGrowTableView) {
                    TableColumn<?, ?> columnToResize = ((HGrowTableView) getTableView()).getColumnToResize();
                    if (columnToResize != null && HGrowTableColumn.this != columnToResize) {
                        double diff = width.doubleValue() - old.doubleValue();
                        columnToResize.setPrefWidth(columnToResize.getWidth() - diff);
                    }
                }
            });
        }
    }

    class Person {
        private final StringProperty firstName = new SimpleStringProperty();
        private final StringProperty lastName = new SimpleStringProperty();

        public Person(String firstName, String lastName) {
            this.firstName.set(firstName);
            this.lastName.set(lastName);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public StringProperty firstNameProperty() {
            return firstName;
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public StringProperty lastNameProperty() {
            return lastName;
        }
    }
}

