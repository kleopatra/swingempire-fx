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
import javafx.collections.MapChangeListener;
import javafx.collections.MapChangeListener.Change;
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
 * A: target not scrolled into viewport
 * B: reorderingLine stuck to incorrect column
 * 
 * win10 explorer: column headers only are scrolled into view, not reliable, though
 * problem: when invoking scrollTo(column), the target column "jumps" because the
 *   header (or entire viewport) is moved below the dragging ..
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8213739
 * 
 * ----
 * quick check: listen for property "TableView.contentWidth"
 * 
 */
public class TableViewScrollHorizontalBug extends Application {

    private Parent getContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales()));
        // quick check: contentWidth listening
        addContentWidthListener(table);
        // end quick check
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

    private final static String SET_CONTENT_WIDTH = "TableView.contentWidth";
    private void addContentWidthListener(TableView<?> table) {
        table.getProperties().addListener(new MapChangeListener<Object, Object>() {
            @Override
            public void onChanged(Change<? extends Object, ? extends Object> c) {
                if (c.wasAdded() && SET_CONTENT_WIDTH.equals(c.getKey())) {
                    LOG.info("contentWidth added: " + c.getValueAdded());
//                    if (c.getValueAdded() instanceof Number) {
//                        setContentWidth((Double) c.getValueAdded());
//                    }
//                    getProperties().remove(SET_CONTENT_WIDTH);
                }
            }
        });

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
