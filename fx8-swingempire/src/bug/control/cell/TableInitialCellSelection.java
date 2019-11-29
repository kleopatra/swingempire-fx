/*
 * Created on 25.02.2019
 *
 */
package control.cell;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Initial selected cell is unexpected:
 * 
 * - compile, run
 * - navigate into table by key-down
 * - expected: first cell in first row selected
 * - actual: last cell in first row selected 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableInitialCellSelection extends Application {

    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(
                Arrays.stream(Locale.getAvailableLocales(), 10, 200).collect(Collectors.toList()));
        TableView<Locale> table = new TableView<>(data);
        table.getColumns().addAll(createTableColumn("displayLanguage"), createTableColumn("displayCountry"));
        table.getSelectionModel().setCellSelectionEnabled(true);
        return new BorderPane(table);
    }

    private <T> TableColumn<T, String> createTableColumn(String property) {
        TableColumn<T, String> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableInitialCellSelection.class.getName());

}
