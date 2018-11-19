/*
 * Created on 11.08.2017
 *
 */
package test.selection;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableView: incorrect scrolling to parent of nested columns.
 * 
 * reported as
 * https://bugs.openjdk.java.net/browse/JDK-8214043
 */
public class TableViewNestedScrollHorizontalBug extends Application {

    private Parent getContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales()));
        table.getSelectionModel().setCellSelectionEnabled(true);
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        TableColumn<Locale, String> display = new TableColumn<>("DisplayName");
        display.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        TableColumn<Locale, String> nested = new TableColumn<>("Nested");
        nested.getColumns().addAll(countryCode, language);
        table.getColumns().addAll(display, nested, variant, new TableColumn<>("Dummy"));

        Button scrollTo = new Button("ScrollTo Nested");
        scrollTo.setOnAction(e -> table.scrollToColumn(nested));
        BorderPane pane = new BorderPane(table);
        pane.setBottom(scrollTo);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 200, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
    .getLogger(TableViewNestedScrollHorizontalBug.class.getName());

}
