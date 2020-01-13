/*
 * Created on 13.01.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59650736/203657
 * 
 * have contextMenu in filler region of table header
 * this is the answer of M.S. - lookup the filler and install a onContextMenuRequested handler
 * working, with a visual quirk: the item's styling is the same as the header (that is bold text)
 * when the per-column contextMenu items are plain
 * 
 * Reason: there is a special styling for column's contextMenu (removing the bold inherited from
 * the header), need to apply to filler as well.
 * 
 */
public class TableContextMenuFiller extends Application {

    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(
                Arrays.stream(Locale.getAvailableLocales(), 10, 20).collect(Collectors.toList()));
        table = new TableView<>(data);
        table.setTableMenuButtonVisible(true);
        
//        table.skinProperty().addListener((src, ov, nv) -> {
//            installFillerMenu();
//        });
        String property = "displayLanguage";
        TableColumn<Locale, String> col = new TableColumn<>(property);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        ContextMenu colMenu = new ContextMenu();
        colMenu.getItems().addAll(new MenuItem("col item"));
        col.setContextMenu(colMenu);
        
        table.getColumns().addAll(col);
        BorderPane content = new BorderPane(table);
        return content;
    }

    /**
     * 
     */
    protected void installFillerMenu() {
        ContextMenu fillerContextMenu = new ContextMenu(new MenuItem("Do in filler"));
        Region filler = (Region) table.lookup(".filler");
        filler.setOnContextMenuRequested(event -> {
            fillerContextMenu.show(filler, event.getScreenX(), event.getScreenY());
            event.consume();
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        URL uri = getClass().getResource("tablecontextmenufiller.css");
        stage.getScene().getStylesheets().add(uri.toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
        installFillerMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableContextMenuFiller.class.getName());
    private TableView<Locale> table;

}
