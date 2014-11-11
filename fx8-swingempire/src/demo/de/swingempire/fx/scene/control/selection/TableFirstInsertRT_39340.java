    /*
     * Created on 14.07.2014
     *
     */
    package de.swingempire.fx.scene.control.selection;
    
    import java.util.Locale;
    import java.util.logging.Logger;
    
    import javafx.application.Application;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.scene.Scene;
    import javafx.scene.control.Button;
    import javafx.scene.control.SelectionMode;
    import javafx.scene.control.TableColumn;
    import javafx.scene.control.TableView;
    import javafx.scene.control.cell.PropertyValueFactory;
    import javafx.scene.control.cell.TextFieldTableCell;
    import javafx.scene.input.KeyCode;
    import javafx.scene.input.KeyEvent;
    import javafx.scene.layout.BorderPane;
    import javafx.stage.Stage;
    
    /**
     * To reproduce, run and
     * - select any row (third or so, just to better see the inconsistent update of
     *   focused and selected index)
     * - press f1 to insert item at 0
     * - expected: selected and focused index increased by one
     * - actually: selected increased by one, focused on first row
     *    
     */
    public class TableFirstInsertRT_39340 extends Application {
        private final ObservableList<Locale> data =
                FXCollections.observableArrayList(Locale.getAvailableLocales()
                        );
       
        private final TableView<Locale> table = new TableView<>(data);
        
        @Override
        public void start(Stage stage) {
            stage.setTitle("Table FocusedCell Bug");
            TableColumn<Locale, String> language = new TableColumn<>(
                    "Language");
            language.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
            table.setItems(data);
            table.getColumns().addAll(language);
    
            table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.F1) {
                    data.add(0, new Locale("dummy"));
                }
            });
    
            BorderPane root = new BorderPane(table);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(System.getProperty("java.version"));
            stage.show();
        }
    
    
    
        public static void main(String[] args) {
            launch(args);
        }
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger.getLogger(TableFirstInsertRT_39340.class
                .getName());
    }