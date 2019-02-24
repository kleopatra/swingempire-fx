/*
 * Created on 19.02.2019
 *
 */
package de.swingempire.fx.scene.control.cell.focus;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableEditWithFocusRequest extends Application {
    

    private Parent createContent() {
        TableView<TreeItem<String>> table =  new TableView<>(
            FXCollections.observableArrayList(new TreeItem<>("first"), new TreeItem<>("second"))    
        );
        
        return null;
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
            .getLogger(TableEditWithFocusRequest.class.getName());

}
