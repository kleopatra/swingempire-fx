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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * cell content not updated on selection change
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8219656
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellContentOnSelection extends Application {

    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(
                Arrays.stream(Locale.getAvailableLocales(), 10, 20).collect(Collectors.toList()));
        TableView<Locale> table = new TableView<>(data);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getColumns().addAll(createTableColumn("displayLanguage"));
        return new BorderPane(table);
    }

    private <T> TableColumn<T, String> createTableColumn(String property) {
        TableColumn<T, String> column = new TableColumn<>(property);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(cc -> {
            // custom cell with text depending on selection state
            TableCell<T, String> cell = new TableCell<>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText("");
                    } else {
                        if (isSelected()) {
                            item = "winner!! " + item;
                        }
                        setText(item);
                    }
                }

                /**
                 * Hack around https://bugs.openjdk.java.net/browse/JDK-8145588
                 * variant for TableCell
                 * @param selected
                 */
//                @Override
//                public void updateSelected(boolean selected) {
//                    super.updateSelected(selected);
//                    updateItem(getItem(), isEmpty());
//                }
                
            };
            return cell;
        });
        return column;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellContentOnSelection.class.getName());

}
