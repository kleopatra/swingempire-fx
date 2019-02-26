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
 * TableView/ListView:  unexpected scrolling behaviour on down/up
 * 
 * not tested: TreeView, TreeTableView, might have similar issue
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8197536
 */
public class TableViewScrollOnNavigationBug extends Application {

    private Parent getContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales()));
        // quick check: initial selection + navigation in cellSelectionMode?
        // table focused, navigate with key-down -> last column in first row selected
//        table.getSelectionModel().setCellSelectionEnabled(true);
        // --- end quick check
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        table.getColumns().addAll(countryCode, language, variant);
        
        ListView<Locale> list = new ListView<>(table.getItems());
        
        //----------- further digging
        Button scrollTo = new Button("scroll");
        scrollTo.setOnAction(e -> {
            int delta = -2;
            scrollAndLog(table, delta);
            scrollAndLog(list, delta);
        });
        
        Button logAllCells = new Button("log cells");
        logAllCells.setOnAction(e -> {
            logCells(table);
            logCells(list);
        });
        Slider slider =  new Slider(0, table.getItems().size(), 0);
        // scrollTo working fine:
        slider.valueProperty().addListener((src, ov, nv) -> {
            table.getSelectionModel().select(nv.intValue());
            table.scrollTo(table.getSelectionModel().getSelectedIndex());
            
            list.getSelectionModel().select(table.getSelectionModel().getSelectedIndex());
            list.scrollTo(list.getSelectionModel().getSelectedIndex());
        });
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        
        HBox buttons = new HBox(10, scrollTo, slider, logAllCells);
        HBox.setHgrow(slider, Priority.ALWAYS);

        //---------
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        pane.setRight(list);
        
        return pane;
    }

    /**
     * @param table
     */
    protected void logCells(Control table) {
        // Note: this is breaking version-encapsulation
        VirtualFlow flow = VirtualFlowTestUtils.getVirtualFlow(table);
        IndexedCell first = flow.getFirstVisibleCell();
        IndexedCell last = flow.getLastVisibleCell();
        
        LOG.info("first/last: " + first + first.getLayoutY() + "\n"
                + last + last.getLayoutY());
        
    }

    /**
     * @param table
     */
    protected void scrollAndLog(TableView<Locale> table, int delta) {
        int selected = table.getSelectionModel().getSelectedIndex();
        int scrolled = selected + delta;
        if (scrolled < 0) return;
        table.scrollTo(scrolled);
        IndexedCell row = VirtualFlowTestUtils.getCell(table, selected);
        LOG.info("row: " + row + row.getLayoutY());
    }
    
    /**
     * @param table
     */
    protected void scrollAndLog(ListView<Locale> table, int delta) {
        int selected = table.getSelectionModel().getSelectedIndex();
        int scrolled = selected + delta;
        if (scrolled < 0) return;
        table.scrollTo(scrolled);
        IndexedCell row = VirtualFlowTestUtils.getCell(table, selected);
        LOG.info("row: " + row + row.getLayoutY());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 800, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewScrollOnNavigationBug.class.getName());

}
