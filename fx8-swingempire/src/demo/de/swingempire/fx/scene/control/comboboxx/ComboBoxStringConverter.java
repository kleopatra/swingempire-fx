/*
 * Created on 02.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

    /**
     * Converter must be used always (if available)
     * 
     * Run:
     * - expected: display value "none"
     * - actual: empty
     */
    public class ComboBoxStringConverter extends Application
    {
      public static void main(String[] args)
      {
        Application.launch(args);
      }
    
      @Override
      public void start(Stage stage) throws Exception
      {
        final ComboBox<Integer> cb = new ComboBox<Integer>();
        cb.getItems().setAll(1, 2, 3);
        StringConverter converter = new StringConverter() {
            @Override public String toString(Object arg0) { 
                if (arg0 == null) return "none";
                return "item " + arg0; }
            @Override public Integer fromString(String arg0) { throw new UnsupportedOperationException(); }
        };
        cb.setConverter(converter);    
        VBox c = new VBox(cb);
        stage.setScene(new Scene(c, 500, 400));
        stage.setTitle(System.getProperty("java.version"));
        stage.show();
      }
    }
