/*
 * Created on 18.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.Stage;

/**
 * Default StringConverter of ComboBoxTableCell (more precisely: CellUtils) can't
 * throws NPE when used in TextFormatter.
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8150178
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxTableCellConverterAndTextFormatter extends Application {

    private Parent getContent() {
        TextField field = new TextField("some dummy");
        TextFormatter formatter = new TextFormatter(new ComboBoxTableCell().getConverter());
        // the default in TextFormatter is okay
        //TextFormatter formatter = new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER);
        field.setTextFormatter(formatter);
        return field;
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
