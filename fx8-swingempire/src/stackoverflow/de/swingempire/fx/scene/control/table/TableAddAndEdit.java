/*
 * Created on 13.03.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Start edit on newly added item immediately.
 * https://stackoverflow.com/q/55142786/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class TableAddAndEdit extends Application {

    
    public class ChequeTable
    {

        private final TableView<Cheque> table;
        private final ObservableList<Cheque> data;
        private final SimpleObjectProperty<BigDecimal> chequeTotal;

        public ChequeTable()
        {

            this.table = new TableView<>();
            this.data = FXCollections.observableArrayList();
            this.chequeTotal = new SimpleObjectProperty<>();
            setUpTable();
        }



        public void clear()
        {
            this.data.clear();           
        }



        private void setUpTable()
        {

            this.table.setEditable(true);
            this.table.setItems(this.data);


            TableColumn<Cheque, String> numberCol = new TableColumn<Cheque, String>("Cheque #");
            numberCol.setCellFactory(column -> EditCell.createStringEditCell());
            numberCol.setCellValueFactory(features -> features.getValue().getChequeNumberProperty());
            numberCol.setMinWidth(100);

            TableColumn<Cheque, BigDecimal> chequeAmountCol = new TableColumn<Cheque, BigDecimal>("Total $");
            chequeAmountCol.setCellValueFactory(features -> features.getValue().getChequeAmountProperty());
            chequeAmountCol.setCellFactory(column -> EditCell.createMoneyEditCell());
            chequeAmountCol.setMinWidth(50);

            TableColumn<Cheque, Boolean> addCol = new TableColumn<Cheque, Boolean>();
            addCol.setGraphic(new Button("+"));
            addCol.setPrefWidth(45);
            addCol.setStyle("-fx-alignment: CENTER;");
            addCol.setSortable(false);
            Button addButton = (Button) addCol.getGraphic();
            addButton.setOnAction((ActionEvent e) ->
            {
                Cheque cheque = new Cheque("Cheque#", BigDecimal.ZERO.setScale(2));

                data.add(cheque);
                table.scrollTo(cheque);
                table.requestFocus();
                table.getSelectionModel().select(cheque);
                table.refresh();
                table.edit(table.getSelectionModel().getSelectedIndex(), numberCol);

            });
            this.table.getColumns().addAll(numberCol, chequeAmountCol, addCol);

        }

        public TableView getTableView()
        {
            return this.table;
        }




    }

    public static class EditCell<S, T> extends TableCell<S, T>
    {

        // Text field for editing
        // TODO: allow this to be a plugable control.
        private final TextField textField = new TextField();

        // Converter for converting the text in the text field to the user type, and vice-versa:
        private final StringConverter<T> converter;
        private final TextAlignment alignment;

        /**
         *
         * @param converter
         * @param alignment
         */
        public EditCell(StringConverter<T> converter, TextAlignment alignment)
        {

            this.converter = converter;
            this.alignment = alignment;
            this.textField.setAlignment(getPosition(alignment));
            itemProperty().addListener((ObservableValue<? extends T> obx, T oldItem, T newItem) ->
            {

                if (newItem == null)
                {
                    setText(null);

                } else
                {
                    setText(converter.toString(newItem));
                }

            });

            setGraphic(textField);

            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setAlignment(getPosition(alignment));

            textField.setOnAction(evt ->
            {
                commitEdit(this.converter.fromString(textField.getText()));
            });

            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean wasFocused, Boolean isNowFocused) ->
            {

                if (!isNowFocused)
                {
                    commitEdit(this.converter.fromString(textField.getText()));
                }

            });

            textField.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) ->
            {

                if (null != event.getCode())
                {
                    switch (event.getCode())
                    {
                        case ESCAPE:
                            textField.setText(converter.toString(getItem()));
                            cancelEdit();
                            event.consume();
                            break;
                        case UP:
                            getTableView().getSelectionModel().selectAboveCell();
                            event.consume();
                            break;
                        case DOWN:
                            getTableView().getSelectionModel().selectBelowCell();
                            event.consume();
                            break;
                        default:
                            break;
                    }
                }

            });

        }

        /**
         *
         * Convenience converter that does nothing (converts Strings to themselves
         * and vice-versa...).
         *
         */
        public static final StringConverter<String> IDENTITY_CONVERTER = new StringConverter<String>()
        {

            @Override
            public String toString(String object)
            {
                return object;
            }

            @Override
            public String fromString(String string)
            {
                return string;
            }

        };

        /**
         *
         */
        public static final StringConverter<BigDecimal> BIG_DECIMAL_MONEY_CONVERTER = new StringConverter<BigDecimal>()
        {
            @Override
            public String toString(BigDecimal object)
            {
                if (object == null)
                {
                    return null;
                } else
                {
                    return object.toPlainString();
                }

            }

            @Override
            public BigDecimal fromString(String string)
            {
                BigDecimal test = new BigDecimal(string);

                test = test.setScale(2, RoundingMode.HALF_EVEN);

                return test;
            }
        };

        /**
         *
         * Convenience method for creating an EditCell for a String value.
         *
         * @param <S>
         * @return
         *
         */
        public static <S> EditCell<S, String> createStringEditCell()
        {
            return new EditCell<>(IDENTITY_CONVERTER, TextAlignment.LEFT);

        }

        /**
         *
         * @param <S>
         * @return
         */
        public static <S> EditCell<S, BigDecimal> createMoneyEditCell()
        {
            return new EditCell<>(BIG_DECIMAL_MONEY_CONVERTER, TextAlignment.RIGHT);
        }

        @Override
        public void updateItem(T item, boolean empty)
        {
            super.updateItem(item, empty);
//            if(isEditing()) {
//                textField.requestFocus();
//            }
            TableColumn tableCol = (TableColumn) getTableColumn();
            
            if (item != null && tableCol.getWidth() < new Text(item + "  ").getLayoutBounds().getWidth())
            {
                tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(), itemProperty().isNull())).then((Tooltip) null).otherwise(new Tooltip(item.toString())));
            } else
            {
                tooltipProperty().bind(Bindings.when(Bindings.or(emptyProperty(), itemProperty().isNull())).then((Tooltip) null).otherwise((Tooltip) null));
            }
        }

        // set the text of the text field and display the graphic
        @Override
        public void startEdit()
        {

            super.startEdit();
            textField.setText(converter.toString(getItem()));
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            Platform.runLater(() -> textField.requestFocus());

        }

        // revert to text display
        @Override
        public void cancelEdit()
        {

            super.cancelEdit();
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setAlignment(getPosition(alignment));
        }

        // commits the edit. Update property if possible and revert to text display
        @Override
        public void commitEdit(T item)
        {

            // This block is necessary to support commit on losing focus, because the baked-in mechanism
            // sets our editing state to false before we can intercept the loss of focus.
            // The default commitEdit(...) method simply bails if we are not editing...
            if (!isEditing() && !Objects.equals(item, getItem()))
            {

                TableView<S> table = getTableView();

                if (table != null)
                {

                    TableColumn<S, T> column = getTableColumn();
                    CellEditEvent<S, T> event = new CellEditEvent<>(table,
                            new TablePosition<>(table, getIndex(), column),
                            TableColumn.editCommitEvent(), item);
                    Event.fireEvent(column, event);

                }

            }

            super.commitEdit(item);

            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setAlignment(getPosition(alignment));
        }

        private static Pos getPosition(TextAlignment textAlignment)
        {

            switch (textAlignment)
            {
                case CENTER:
                    return Pos.CENTER;
                case RIGHT:
                    return Pos.CENTER_RIGHT;
                case LEFT:
                    return Pos.CENTER_LEFT;
                default:
                    return Pos.CENTER_LEFT;
            }
        }

    }



    @Override
    public void start(Stage primaryStage)
    {

        ChequeTable chequeTable = new ChequeTable();

        HBox root = new HBox();
        root.getChildren().addAll(chequeTable.getTableView());

        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

            launch(args);

    }

    public class Cheque
    {

        private final SimpleStringProperty chequeNumber;
        private final SimpleObjectProperty<BigDecimal> chequeAmount;
        private final ObservableValue changeProperty;

        public Cheque()
        {
            this.chequeNumber = new SimpleStringProperty();
            this.chequeAmount = new SimpleObjectProperty<>();
            this.changeProperty = Bindings.concat(chequeNumber, chequeAmount);

        }

        public Cheque(String chequeNumber, BigDecimal amount)
        {
            this();
            this.chequeNumber.set(chequeNumber);
            this.chequeAmount.set(amount);
        }

        /**
         *
         * @return
         */
        public ObservableValue getChangedProperty()
        {
            return this.changeProperty;
        }

        /**
         * @return the chequeNumber
         */
        public SimpleStringProperty getChequeNumberProperty()
        {
            return chequeNumber;
        }

        /**
         * @return the chequeAmount
         */

        public SimpleObjectProperty<BigDecimal> getChequeAmountProperty()
        {
            return chequeAmount;
        }

        public String getChequeNumber()
        {
            return chequeNumber.get();
        }

        public void setChequeNumber(String newValue)
        {
            this.chequeNumber.set(newValue);
        }

        public BigDecimal getChequeAmount()
        {
            return chequeAmount.get();
        }

        public void setChequeAmount(BigDecimal newValue)
        {
            this.chequeAmount.set(newValue);
        }



    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableAddAndEdit.class.getName());

}
