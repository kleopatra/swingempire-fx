/*
 * Created on 25.03.2019
 *
 */
package de.swingempire.testfx.table;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static java.util.stream.Collectors.*;
import static de.swingempire.testfx.table.TablePrefSizeFactory.*;
import static de.swingempire.testfx.util.TableFactory.*;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePrefSizeApp extends Application {

    TableView<Locale> table;
    
    private Parent createContent() {
        table = createTablePrefSize();
        table.getSelectionModel().setCellSelectionEnabled(true);
        
        List<Locale> filtered =         
                Arrays.stream(Locale.getAvailableLocales())
                    .filter(l -> l.getDisplayName() != null && !l.getDisplayName().isBlank())
                    // find and remove duplicates
//                    .collect(collectingAndThen(
//                            toCollection(() -> new TreeSet<>(comparing(Locale::getDisplayName))),
//                            ArrayList::new));
                     .collect(toList());
        ObservableList<Locale> data = FXCollections.observableArrayList(filtered);
        table.setItems(data);
        
        table.getColumns().addAll(createTableColumn("displayName"));
        Pane root = new BorderPane(table);
        return root;
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
            .getLogger(TablePrefSizeApp.class.getName());

}
