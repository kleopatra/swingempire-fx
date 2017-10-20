/*
 * Created on 11.08.2017
 *
 */
package binding;

//import java.lang.System.Logger;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Binding.selectXX(observable, steps...) logs a warning if value null - 
 * though doc'ed to be valid state
 * https://bugs.openjdk.java.net/browse/JDK-8091396 and related
 * 
 */
public class BindingLogsWarning extends Application {
/*
 * 
 * From https://stackoverflow.com/q/20815048/203657
 // Log all focus messages.
final Logger rootLogger = Logger.getLogger("");
rootLogger.setLevel(Level.ALL);
final ConsoleHandler consoleHandler = new ConsoleHandler();
consoleHandler.setLevel(Level.ALL);
// Because there are a lot of focus messages, make them
// a little easier to read by logging only the message.
consoleHandler.setFormatter(new Formatter() {
    @Override
    public String format(LogRecord record) {
        return "FOCUS: " + record.getMessage() + '\n';
    }
});
final Logger logger = Logger.getLogger("java.awt.focus.Component");
logger.setLevel(Level.ALL);
logger.setUseParentHandlers(false);
logger.addHandler(consoleHandler);
 */
    static {
        // disable logging of everything from beans packages
        Logger.getLogger("javafx.beans").setLevel(Level.SEVERE);
        }
    private int counter;

    private Parent getContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales()));
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        table.getColumns().addAll(countryCode, language, variant);
        Label selected = new Label();
        selected.textProperty().bind(Bindings.select(table.getSelectionModel().selectedItemProperty(), "displayLanguage"));
        HBox buttons = new HBox(10, selected);
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 800, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
        LOG.info("want to grab this ...");

    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(BindingLogsWarning.class.getName());
}
