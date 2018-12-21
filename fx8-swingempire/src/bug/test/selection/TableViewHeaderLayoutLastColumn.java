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
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableView: incorrect layout of last column
 * happens if the menuButton is visible and no vertical scrollbar.
 * 
 * To reproduce, compile and run the example. 
 * - make sure both scrollbars are visible and scroll to max
 * - expected and actual: last header is fully visible and can be resized by mouse
 * - extend height until vertical scrollbar is hidden
 * - expected is same as with vertical scrollbar
 * - actual: last header is cut off (text not fully visible) and can't be resized
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8215148
 * TableView: incorrect layout of last column
 */
public class TableViewHeaderLayoutLastColumn extends Application {

    private Parent getContent() {
        ObservableList<Locale> locales = FXCollections.observableArrayList(
                Locale.getAvailableLocales());
        locales.remove(10, locales.size());
        TableView<Locale> table = new TableView<>(locales);
        table.getSelectionModel().setCellSelectionEnabled(true);
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        TableColumn<Locale, String> display = new TableColumn<>("DisplayName");
        display.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        table.getColumns().addAll(display, countryCode, language, variant);

        table.setTableMenuButtonVisible(true);
        CheckBox toggle = new CheckBox("toggle menubutton");
        toggle.selectedProperty().bindBidirectional(table.tableMenuButtonVisibleProperty());
        BorderPane pane = new BorderPane(table);
        pane.setBottom(toggle);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 300, 300));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewHeaderLayoutLastColumn.class.getName());

}
