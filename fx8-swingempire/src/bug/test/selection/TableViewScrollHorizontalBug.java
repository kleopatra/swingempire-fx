/*
 * Created on 11.08.2017
 *
 */
package test.selection;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import de.swingempire.fx.util.VirtualFlowTestUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * TableView:  focus left/right via keyboard must scroll cell into viewport
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8213540
 * 
 * --- 
 * TableView: issues when reordering columns by dragging off viewport
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8213739
 * 
 */
public class TableViewScrollHorizontalBug extends Application {

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
        table.getColumns().addAll(display, countryCode, language, variant);

        BorderPane pane = new BorderPane(table);
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
            .getLogger(TableViewScrollHorizontalBug.class.getName());

}
