/*
 * Created on 26.03.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * keep table always editing (aka: write-through to backing property on 
 * typing) and allow navigation
 * https://stackoverflow.com/q/55330430/203657
 * 
 * working with a couple of caveats:
 * - UX: typed chars are inserted at the start of the textfield
 * - UX: can't navigate inside the text field
 * 
 * issue with selection:
 * - have an extractor is added to the items
 * - select a row
 * - edit a field that is in the column of the extracted value and is not the selected (?)
 * -> throws IOOB on scrollToColumnIndex
 * 
 * reason:
 * - impl of tableViewSelectionModel doesn't handle updates correctly
 * - should do nothing
 * - instead, it's reselecting to selected row with select(row)
 * - which calls select(row, null) in rowSelectionMode
 * - which updates the focusedCell to (row, null)
 * - which produces the -1 in scrolling
 * 
 * hot fix:
 * - force focusedCell back to cell in keyHandler
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewAlwaysEditing extends Application {

    /**
     * Custom cell in answer by slaw
     */
    public static class CustomTableCell<S, T> extends TableCell<S, T> {

        public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn(
                IntFunction<Property<String>> extractor) {
            return forTableColumn(extractor, new DefaultStringConverter());
        }

        public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
                IntFunction<Property<T>> extractor, StringConverter<T> converter) {
            Objects.requireNonNull(extractor);
            Objects.requireNonNull(converter);
            return column -> new CustomTableCell<>(extractor, converter);
        }

        private final ObjectProperty<IntFunction<Property<T>>> extractor = new SimpleObjectProperty<>(this, "extractor");
        public final void setExtractor(IntFunction<Property<T>> callback) { extractor.set(callback); }
        public final IntFunction<Property<T>> getExtractor() { return extractor.get(); }
        public final ObjectProperty<IntFunction<Property<T>>> extractorProperty() { return extractor; }

        private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter");
        public final void setConverter(StringConverter<T> converter) { this.converter.set(converter); }
        public final StringConverter<T> getConverter() { return converter.get(); }
        public final ObjectProperty<StringConverter<T>> converterProperty() { return converter; }

        private Property<T> property;
        private TextField textField;

        public CustomTableCell(IntFunction<Property<T>> extractor, StringConverter<T> converter) {
            setExtractor(extractor);
            setConverter(converter);

            // Assumes this TableCell will never become part of a different TableView
            // after the first one. Also assumes the focus model of the TableView will
            // never change. These are not great assumptions (especially the latter),
            // but this is only an example.
            tableViewProperty().addListener((obs, oldTable, newTable) ->
                    newTable.getFocusModel().focusedCellProperty().addListener((obs2, oldPos, newPos) -> {
                        if (getIndex() > -1 && getIndex() < 2) {
                            LOG.info("listener to cell: " + newPos);
                        }

                        if (getIndex() == newPos.getRow() && getTableColumn() == newPos.getTableColumn()) {
                            textField.requestFocus();
                        }
                    })
            );
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                cleanUpProperty();
            } else {
                initializeTextField();
                cleanUpProperty();

                property = getExtractor().apply(getIndex());
                Bindings.bindBidirectional(textField.textProperty(), property, getConverter());

                setGraphic(textField);
                if (getTableView().getFocusModel().isFocused(getIndex(), getTableColumn())) {
                    textField.requestFocus();
                }
            }
        }

        private void cleanUpProperty() {
            if (property != null) {
                Bindings.unbindBidirectional(textField.textProperty(), property);
                property = null;
            }
        }

        private void initializeTextField() {
            if (textField == null) {
                textField = new TextField();
                textField.addEventFilter(KeyEvent.KEY_PRESSED, this::processArrowKeys);
                textField.focusedProperty().addListener((observable, wasFocused, isFocused) -> {
                    if (isFocused) {
                        TableViewFocusModel<S> focusModel = getTableView().getFocusModel();
                        focusModel.focus(getIndex(), getTableColumn());
                        if (getIndex() > -1 && getIndex() < 2) {
                            LOG.info("focusListener: " + focusModel.getFocusedCell());
                        }
                    }
                });
            }
        }

        private void processArrowKeys(KeyEvent event) {
            if (event.getCode().isArrowKey()) {
                event.consume();

                TableViewFocusModel<S> model = getTableView().getFocusModel();
                LOG.info("before nav:" + model.getFocusedCell());
                if (model.getFocusedCell().getTableColumn() == null) {
                    model.focus(getIndex(), getTableColumn());
                }
                switch (event.getCode()) {
                    case UP:
                        model.focusAboveCell();
                        break;
                    case RIGHT:
                        model.focusRightCell();
                        break;
                    case DOWN:
                        model.focusBelowCell();
                        break;
                    case LEFT:
                        model.focusLeftCell();
                        break;
                    default:
                        throw new AssertionError(event.getCode().name());
                }
                TablePosition focusedCell = model.getFocusedCell();
                getTableView().scrollTo(focusedCell.getRow());
                LOG.info("after nav:" + focusedCell);
                getTableView().scrollToColumnIndex(focusedCell.getColumn());
            }
        }

    }


    private Parent createContent() {
        TableView<LineItem> table = new TableView<>();

//                Callback<TableColumn<LineItem, String>, TableCell<LineItem, String>> textFactoryEditable = 
//                (TableColumn<LineItem, String> p) -> new EditableTextCell();

        TableColumn<LineItem, String> column1 = new TableColumn<>("Test1");
        column1.setCellValueFactory(cellData -> cellData.getValue().getString1Property());
        column1.setEditable(true);
        column1.setCellFactory(CustomTableCell.forTableColumn(i -> table.getItems().get(i).getString1Property()));
//        column1.setCellFactory(textFactoryEditable);

        table.getColumns().add(column1);

        TableColumn<LineItem, String> column2 = new TableColumn<>("Test2");
        column2.setCellValueFactory(cellData -> cellData.getValue().getString2Property());
        column2.setEditable(true);
//        column2.setCellFactory(textFactoryEditable);
        column2.setCellFactory(CustomTableCell.forTableColumn(i -> table.getItems().get(i).getString2Property()));

        table.getColumns().add(column2);

//        table.getItems().add(new LineItem());
//        table.getItems().add(new LineItem());
//        table.getItems().add(new LineItem());
        List<LineItem> items = Stream.generate(LineItem::new).limit(10).collect(Collectors.toList());
        // extractor on items
        ObservableList<LineItem> data = FXCollections.observableList(items, 
//                item -> new Observable[] {item.getString1Property()});
                item -> new Observable[] {item.getString2Property()});
//        table.getItems().addAll(items);
        table.setItems(data);
        table.setPrefWidth(500);

        
        
        HBox root = new HBox(10);
        root.getChildren().addAll(table);

        return root;
    }

    // just some data
    public static class LineItem
    {
        private static int count;
        private final StringProperty string1;
        private final StringProperty string2;

        public LineItem()
        {
            this.string1 = new SimpleStringProperty(""+ count++);
            this.string2 = new SimpleStringProperty();
        }

        public final StringProperty getString1Property()
        {
            return this.string1;
        }

        public final StringProperty getString2Property()
        {
            return this.string2;
        }
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.setX(0);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewAlwaysEditing.class.getName());

}
