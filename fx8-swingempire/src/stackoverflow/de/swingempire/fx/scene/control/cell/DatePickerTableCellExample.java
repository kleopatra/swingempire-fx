/*
 * Created 28.06.2022
 */

package de.swingempire.fx.scene.control.cell;

import java.time.LocalDate;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


/**
 *
 */
public class DatePickerTableCellExample extends Application {



    public static class DatePickerTableCell<S> extends TableCell<S, LocalDate> {
        private final DatePicker datePicker = new DatePicker();

        public DatePickerTableCell() {
            datePicker.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    commitEdit(datePicker.getValue());
                }
            });

        }

        @Override
        protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.toString());
            }
//            if (isEditing()) {
//                setText(null);
//                setGraphic(datePicker);
//            } else {
//                setGraphic(null);
//                if (item != null) {
//                    setText(item.toString());
//                } else {
//                    setText(null);
//                }
//            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            setGraphic(datePicker);
//            setText(null);
            // this adds a new handler each time the edit is started
            // move to constructor
//            datePicker.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent actionEvent) {
//                    commitEdit(datePicker.getValue());
//                }
//            });
        }

        @Override
        public void commitEdit(LocalDate s) {
            super.commitEdit(s);
            setText(s.toString());
            setGraphic(null);
            // not recommended (see api doc)
            // either use updateItem(s) or do nothing, super should handle the update
//              setItem(s);
        }


        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(datePicker.getValue() == null ? null : datePicker.getValue().toString());
            setGraphic(null);
        }
    }

    public static class DatePickerTableCellEx<S> extends TableCell<S, LocalDate> {
        private final DatePicker datePicker = new DatePicker();

        public DatePickerTableCellEx() {
            datePicker.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    commitEdit(datePicker.getValue());
                }
            });
            setGraphic(datePicker);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                datePicker.setValue(null);
            } else {
                setText(item.toString());
                datePicker.setValue(item);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (!isEditing()) return;
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

         @Override
          public void commitEdit(LocalDate s) {
              super.commitEdit(s);
              if (isEditing()) return;
              setContentDisplay(ContentDisplay.TEXT_ONLY);
         }


        @Override
        public void cancelEdit() {
            super.cancelEdit();
            if (isEditing()) return;
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    // original, incorrect pattern
    public static class SetterGetter {
        int id;
        String name;
        ObjectProperty<LocalDate> date;
        int status;

        public SetterGetter(int id, String name, ObjectProperty<LocalDate> date, int status) {
            this.id = id;
            this.name = name;
            this.date = date != null ? date : new SimpleObjectProperty<>();
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ObjectProperty<LocalDate> getDate() {
            return date;
        }

//        public void setDate(ObjectProperty<LocalDate> date) {
//            this.date = date;
//        }

        @Override
        public String toString() {
            LocalDate current = getDate() != null ? getDate().get() : null;
            return name + getDate();
        }



    }

    public static class SetterGetterCorrect {
        int id;
        String name;
        ObjectProperty<LocalDate> date;
        int status;

        public SetterGetterCorrect(int id, String name, LocalDate date, int status) {
            this.id = id;
            this.name = name;
            this.date = new SimpleObjectProperty<>(date);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ObjectProperty<LocalDate> dateProperty() {
            return date;
        }

        public LocalDate getDate() {
            return dateProperty().get();
        }
        public void setDate(LocalDate date) {
            dateProperty().set(date);
        }
    }

    private TableView<SetterGetter> createTable() {
        TableView<SetterGetter> table = new TableView<>();

        TableColumn<SetterGetter, String> name = new TableColumn<>("Name");
        // quick check: PropertyValueFactory returns ReadOnlyProperty even if "property" has setter!
//        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        // adding properties on-the-fly also requires a commit handler
        name.setCellValueFactory(b -> new SimpleStringProperty(b.getValue().getName()));
        name.onEditCommitProperty().set(e -> {
            SetterGetter row = e.getRowValue();
            row.setName(e.getNewValue());
        });
        name.setCellFactory(TextFieldTableCell.forTableColumn());

        TableColumn<SetterGetter, LocalDate> colDueDate = new TableColumn<>("Date");
        colDueDate.setCellValueFactory(b -> b.getValue().getDate());
//        colDueDate.setOnEditCommit(e -> {
//            SetterGetter row = e.getRowValue();
//            row.setDate(new SimpleObjectProperty(e.getNewValue()));
//        });
//        colDueDate.setCellFactory(cc -> new DatePickerTableCell());
        colDueDate.setCellFactory(cc -> new DatePickerTableCellEx());
        colDueDate.setPrefWidth(200);

        table.getColumns().addAll(name, colDueDate);
        table.setEditable(true);
        return table;
    }

    ObservableList<SetterGetter> mainTaskList = FXCollections.observableArrayList();
    FilteredList<SetterGetter> tableTaskList = new FilteredList<>(mainTaskList, p -> true);

    CheckBox filter;

    private Parent createContent() {
        TableView<SetterGetter> tableTasks = createTable();
        mainTaskList.addAll(
                new SetterGetter(0, "one", null, 0),
                new SetterGetter(1, "two", null, 1)
        );

        tableTasks.setItems(tableTaskList);

        filter = new CheckBox("filter done");
        filter.selectedProperty().addListener((src, ov, nv) -> {
            tableTaskList.setPredicate(sg -> {
                if (nv) {
                    return sg.getStatus() == 1;
                }
                return true;
            });
        });

        Button button = new Button("log data");
        button.setOnAction(e -> {
            System.out.println(mainTaskList);
        });
        BorderPane content = new BorderPane(tableTasks);
        content.setBottom(new HBox(10, filter, button));
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
//        stage.setTitle(FXUtils.version());
        stage.setX(100);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DatePickerTableCellExample.class.getName());

}
