/*
 * Created on 05.10.2019
 *
 */
package de.swingempire.fx.concurrency;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58243099/203657
 * OP argues that the not-throwing of progress binding is fine
 * 
 * actually it is not ... fact is that fx doesn't protect itself
 * against threading violations (only does when changing the scenegraph
 * structure)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FishyThreadingTableScrolling extends Application {

    private Parent createContent() {
        DoubleProperty prop = new SimpleDoubleProperty(0);
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        TableColumn<Locale, String> local = new TableColumn<>("Country");
        local.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        table.getColumns().addAll(local);
        // scrollBar only available after skin is attached
        table.skinProperty().addListener((src, ov, nv) -> {
            ScrollBar vbar = (ScrollBar) table.lookup(".scroll-bar");
            // not throwing, even thow it involves re-creating cells?
            vbar.valueProperty().bind(prop);
        });
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // updating a property of a node that's attached to the
                // scenegraph
                // DONT DONT DONT ... NEVER-EVER!!!
                prop.set(prop.get() + 0.1);
            }
        });
        thread.setDaemon(true);
        thread.start();
        
        
        BorderPane content = new BorderPane(table);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 600, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FishyThreadingTableScrolling.class.getName());

}
