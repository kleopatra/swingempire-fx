/*
 * Created on 13.04.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.time.LocalDate;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55660520/203657
 * cell's tableRow is null "early" in its life-cycle
 * 
 * it's specified in cell's tableRowProperty:
 * 
 * <p>
 * "The TableRow may be null early in the TableCell lifecycle, 
 * in the period between the TableCell being instantiated and being set 
 * into an owner TableRow."
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ConditionalColumnSample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Simple Interface
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        // TableView
        TableView<Bill> tableView = new TableView<>();
        TableColumn<Bill, Number> colAmountDue = new TableColumn<>("Amount Due");
        TableColumn<Bill, LocalDate> colDatePaid = new TableColumn<>("Date Paid");
        tableView.getColumns().addAll(colAmountDue, colDatePaid);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // CellValueProperties
        colAmountDue.setCellValueFactory(tf -> tf.getValue().amountDueProperty());
        colDatePaid.setCellValueFactory(tf -> tf.getValue().datePaidProperty());

        // CellFactory for Date Paid column
        colDatePaid.setCellFactory(col -> new TableCell<Bill, LocalDate>() {

            final DatePicker datePicker = new DatePicker();
            final Label dateLabel = new Label();
            
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                System.out.println(getTableRow()); // <-- This prints 'null' only once upon execution
                // tableRow may be null early in the life-cycle
                Bill bill = getTableRow() !=null ? (Bill) getTableRow().getItem() : null;
                // was also logic problem in data ..
                
                if (empty || bill == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    if (item == null || bill.isDateChanged()) {
                       
                        bill.datePaidProperty().bind(datePicker.valueProperty());
                        setText(null);
                        setGraphic(datePicker);
                    } else {
                        String value = bill.getDatePaid().toString();
//                        dateLabel.setText(value);
                        setGraphic(null);
                        setText(value);
//                        setGraphic(new Label(bill.getDatePaid().toString()));
                    }
                }
                
                
            }
        });

        // Button to test data
        Button button = new Button("Print Values");
        button.setOnAction(event -> {
            for (Bill bill : tableView.getItems()) {
                System.out.println(bill);
            }
        });

        // Sample data
        tableView.getItems().addAll(
                new Bill(42.34, null),
                new Bill(894.99, null),
                new Bill(54.87, LocalDate.now()),
                new Bill(5.00, null),
                new Bill(123.68, LocalDate.of(2019, 12, 25)),
                new Bill(42.34, null),
                new Bill(894.99, null),
                new Bill(54.87, LocalDate.now()),
                new Bill(5.00, null),
                new Bill(123.68, LocalDate.of(2019, 12, 25)),
                new Bill(42.34, null),
                new Bill(894.99, null),
                new Bill(54.87, LocalDate.now()),
                new Bill(5.00, null),
                new Bill(123.68, LocalDate.of(2019, 12, 25)),
                new Bill(42.34, null),
                new Bill(894.99, null),
                new Bill(54.87, LocalDate.now()),
                new Bill(5.00, null),
                new Bill(123.68, LocalDate.of(2019, 12, 25)),
                new Bill(42.34, null),
                new Bill(894.99, null),
                new Bill(54.87, LocalDate.now()),
                new Bill(5.00, null),
                new Bill(123.68, LocalDate.of(2019, 12, 25))
        );

        root.getChildren().addAll(tableView, button);

        // Show the stage
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Sample");
        primaryStage.show();
    }
}

class Bill {

    private DoubleProperty amountDue = new SimpleDoubleProperty();
    private ObjectProperty<LocalDate> datePaid = new SimpleObjectProperty<>();

    private BooleanProperty dateChanged = new SimpleBooleanProperty();

    public Bill(double amountDue, LocalDate datePaid) {
        this.amountDue.set(amountDue);
        this.datePaid.set(datePaid);

        // Listener to determine if the date has been changed
        this.datePaidProperty().addListener((observable, oldValue, newValue) -> {
            dateChanged.set(true);
        });

    }

    public double getAmountDue() {
        return amountDue.get();
    }

    public void setAmountDue(double amountDue) {
        this.amountDue.set(amountDue);
    }

    public DoubleProperty amountDueProperty() {
        return amountDue;
    }

    public LocalDate getDatePaid() {
        return datePaid.get();
    }

    public void setDatePaid(LocalDate datePaid) {
        this.datePaid.set(datePaid);
    }

    public ObjectProperty<LocalDate> datePaidProperty() {
        return datePaid;
    }

    public boolean isDateChanged() {
        return dateChanged.get();
    }

    public void setDateChanged(boolean dateChanged) {
        this.dateChanged.set(dateChanged);
    }

    public BooleanProperty dateChangedProperty() {
        return dateChanged;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "amountDue=" + amountDue +
                ", datePaid=" + datePaid +
                ", dateChanged=" + dateChanged +
                '}';
    }
}

