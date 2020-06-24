/*
 * Created on 21.06.2020
 *
 */
package de.swingempire.fx.scene.control.table;


import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/62498594/203657
 * header auto-size incorrect with graphics
 * 
 * can reproduce, fx11 and current dev (shortly before fx15 release)
 */
public class TableHeaderWithGraphicSize extends Application {

    private Parent createContent() {
        ObservableList<Locale> data = FXCollections.observableArrayList(Locale.getAvailableLocales());
        TableView<Locale> table = new TableView<>(data);
        TextField headerTextField = new TextField();
        Label label = new Label("MyText which is longer than content");
        VBox headerGraphic = new VBox();
        headerGraphic.setAlignment(Pos.CENTER);
        headerGraphic.getChildren().addAll(label, headerTextField);
//        headerGraphic.setMinWidth(Region.USE_PREF_SIZE);
        TableColumn<Locale, String> tableColumn = new TableColumn<>();
//        tableColumn.setMinWidth(Region.USE_PREF_SIZE);
        tableColumn.setGraphic(headerGraphic);
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("displayCountry"));
        table.getColumns().addAll(tableColumn);
        
        BorderPane content = new BorderPane(table);
        return content;
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
            .getLogger(TableHeaderWithGraphicSize.class.getName());

}
