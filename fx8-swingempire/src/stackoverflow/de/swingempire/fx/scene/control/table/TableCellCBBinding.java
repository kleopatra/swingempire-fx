/*
 * Created on 04.03.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/60523457/203657
 * 
 * bind CheckBoxTablecell to ObservableValue - not supported properly,
 * 
 * @see TableBindSelectCell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellCBBinding extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        init(primaryStage);
        primaryStage.show();
    }

    private void init(Stage primaryStage) {
        primaryStage.setScene(new Scene(buildContent()));
    }

    private Parent buildContent() {
        TableView<ViewModel> tableView = new TableView<>();
        tableView.setItems(sampleEntries());
        tableView.setEditable(true);
        tableView.getColumns().add(buildRequiredColumn());
        tableView.getColumns().add(buildNameColumn());

        // Add a Textfield to show the values for the first item
        // As soon as the name is set to "X", the effectiveRequiredProperty should evaluate to true and the CheckBox should reflect this but it does not
        TextField text = new TextField();
        ViewModel firstItem = tableView.getItems().get(0);
        text.textProperty()
            .bind(Bindings.format("%s | %s | %s", firstItem.nameProperty(), firstItem.isRequiredProperty(), firstItem.effectiveRequiredProperty()));

        return new HBox(text, tableView);
    }

    private TableColumn<ViewModel, String> buildNameColumn() {
        TableColumn<ViewModel, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setEditable(true);
        return nameColumn;
    }

    private TableColumn<ViewModel, Boolean> buildRequiredColumn() {
        TableColumn<ViewModel, Boolean> requiredColumn = new TableColumn<>(
                "isEffectiveRequired");
        requiredColumn.setMinWidth(50);
        // This is should bind my BindingExpression from to ViewModel to the
        // CheckBox
        requiredColumn.setCellValueFactory(
                p -> p.getValue().effectiveRequiredProperty());
        requiredColumn.setCellFactory( CheckBoxTableCell.forTableColumn(requiredColumn));

        requiredColumn.setCellFactory(cc -> {
            TableCell<ViewModel, Boolean> cell = new TableCell<>() {
                CheckBox check = new CheckBox() {

                    @Override
                    public void fire() {
                        // do nothing - visualizing read-only property
                        // could do better, like actually changing the table's
                        // selection
                    }

                };
                {
                    getStyleClass().add("check-box-table-cell");
                    check.setOnAction(e -> {
                        e.consume();
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        check.setSelected(item);
                        setGraphic(check);
                    }
                }

            };
            return cell;
        });

        return requiredColumn;
    }

    private ObservableList<ViewModel> sampleEntries() {
        return FXCollections.observableArrayList(
                new ViewModel(false, "A"),
                new ViewModel(true,  "B"),
                new ViewModel(false, "C"),
                new ViewModel(true,  "D"),
                new ViewModel(false, "E"));
    }

    public static class ViewModel {
        public static final String SPECIAL_STRING = "X";

        private final StringProperty name;
        private final BooleanProperty isRequired;

        public ViewModel(boolean isRequired, String name) {
            this.name = new SimpleStringProperty(this, "name", name);
            this.isRequired = new SimpleBooleanProperty(this, "isRequired", isRequired);
            this.name.addListener((observable, oldValue, newValue) -> System.out.println(newValue));
        }

        public StringProperty nameProperty() {return name;}
        public final String getName(){return name.get();}
        public final void setName(String value){
            name.set(value);}

        public boolean isRequired() {
            return isRequired.get();
        }
        public BooleanProperty isRequiredProperty() {
            return isRequired;
        }
        public void setRequired(final boolean required) {
            this.isRequired.set(required);
        }

        public ObservableBooleanValue effectiveRequiredProperty() {
            // Bindings with this work:
            // return isRequired;
            // with this not
            return isRequired.or(name.isEqualTo(SPECIAL_STRING));
        }
    }
}