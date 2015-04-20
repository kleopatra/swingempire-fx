/*
 * Created on 20.04.2015
 *
 */
package de.swingempire.fx.scene.control.text;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

/**
 * Example of how-to use a TextFormatter in a editing TableCell.
 * 
 * http://stackoverflow.com/a/29743588/203657
 */
public class CellFormatting extends Application {
    
    private Parent getContent() {
        ObservableList<IntData> data = FXCollections.observableArrayList(
                new IntData(1), new IntData(2), new IntData(3)
                );
        TableView<IntData> table = new TableView<>(data);
        table.setEditable(true);
        TableColumn<IntData, Integer> column = new TableColumn<>("Data");
        column.setCellValueFactory(new PropertyValueFactory("data"));
        // core default: will throw exception on illegal values
        // column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c -> {
            if (c.isContentChange()) {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 ||
                        parsePosition.getIndex() < c.getControlNewText().length()) {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };
        column.setCellFactory(c -> new ValidatingTextFieldTableCell<>(
                new TextFormatter<Integer>(
                // note: should use local-aware converter instead of core!
                new IntegerStringConverter(), 0,
                filter)));
        table.getColumns().add(column);
        VBox box = new VBox(table);
        return box;
    }
    
    /**
     * TextFieldTableCell that validates input with a TextFormatter.
     * <p>
     * Extends TextFieldTableCell, accesses super's private field reflectively.
     * 
     */
    public static class ValidatingTextFieldTableCell<S, T> extends TextFieldTableCell<S, T> {

        private TextFormatter<T> formatter;
        private TextField textAlias;

        public ValidatingTextFieldTableCell() {
            this((StringConverter<T>)null);
        }

        public ValidatingTextFieldTableCell(StringConverter<T> converter) {
            super(converter);
        }
             
        public ValidatingTextFieldTableCell(TextFormatter<T> formatter) {
            super(formatter.getValueConverter());
            this.formatter = formatter;
        }

        /**
         * Overridden to install the formatter. <p>
         * 
         * Beware: implementation detail! super creates and configures
         * the textField lazy on first access, so have to install after
         * calling super.
         */
        @Override
        public void startEdit() {
            super.startEdit();
            installFormatter();
        }

        private void installFormatter() {
            if (formatter != null && isEditing() && textAlias == null) {
                textAlias = invokeTextField();
                textAlias.setTextFormatter(formatter);
            }
        }

        private TextField invokeTextField() {
            Class<?> clazz = TextFieldTableCell.class;
            try {
                Field field = clazz.getDeclaredField("textField");
                field.setAccessible(true);
                return (TextField) field.get(this);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
        
        
    }
    
    public static class IntData {
        IntegerProperty data = new SimpleIntegerProperty(this, "data");
        public IntData(int value) {
            setData(value);
        }
        
        public void setData(int value) {
            data.set(value);
        }
        
        public int getData() {
            return data.get();
        }
        
        public IntegerProperty dataProperty() {
            return data;
        }
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(CellFormatting.class
            .getName());
}
