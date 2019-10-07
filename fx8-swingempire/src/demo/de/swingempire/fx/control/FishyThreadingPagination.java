/*
 * Created on 22.07.2015
 *
 */
package de.swingempire.fx.control;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 */
public class FishyThreadingPagination extends Application {
    
    private Pagination pagination;
    private List<String> fonts;
 
    public static void main(String[] args) throws Exception {
        launch(args);
    }
 
    public int itemsPerPage() {
        return 15;
    }
 
    public VBox createPage(int pageIndex) {        
        VBox box = new VBox(5);
        int page = pageIndex * itemsPerPage();
        for (int i = page; i< fonts.size() && i < page + itemsPerPage(); i++) {
            Label font = new Label(fonts.get(i));  // (fonts[i]);
            box.getChildren().add(font);
        }
        return box;
    }
 
    @Override
    public void start(final Stage stage) throws Exception {
        fonts = Font.getFamilies(); 
        
        pagination = new Pagination();
        pagination.setPageFactory(pageIndex -> createPage(pageIndex));
 
        IntegerProperty prop = new SimpleIntegerProperty(0);
        pagination.currentPageIndexProperty().bindBidirectional(prop);
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // updating a property that's bound to a property of a node  
                // which is attached to the scenegraph
                // DONT DONT DONT ... NEVER-EVER!!!
                prop.set(prop.get() + 1);
            }
        });
        thread.setDaemon(true);
        thread.start();

        BorderPane content = new BorderPane(pagination);
        Scene scene = new Scene(content, 400, 450);
        stage.setScene(scene);
        stage.setTitle("PaginationSample");
        stage.show();
    }
 

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(FishyThreadingPagination.class
            .getName());
}