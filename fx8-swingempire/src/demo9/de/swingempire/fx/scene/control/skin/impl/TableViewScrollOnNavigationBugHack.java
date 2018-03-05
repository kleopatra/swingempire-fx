/*
 * Created on 11.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.Locale;

import de.swingempire.fx.scene.control.skin.XTableViewSkin;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * TableView/ListView:  unexpected scrolling behaviour on down/up
 * 
 * not tested: TreeView, TreeTableView, might have similar issue
 * 
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8197536
 * 
 * Here trying to hack with a custom TableSkin.
 */
public class TableViewScrollOnNavigationBugHack extends Application {

    private Parent getContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(
                Locale.getAvailableLocales())) {

                    @Override
                    protected Skin<?> createDefaultSkin() {
                        return new XTableViewSkin<>(this);
                    }
            
        };
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        table.getColumns().addAll(countryCode, language, variant);
        
        ListView<Locale> list = new ListView<>(table.getItems());
        BorderPane pane = new BorderPane(table);
        pane.setRight(list);
        
        return pane;
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

}
