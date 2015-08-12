/*
 * Created on 22.07.2015
 *
 */
package de.swingempire.fx.control;

import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.Skin;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.sun.javafx.scene.control.skin.PaginationSkin;

import de.swingempire.fx.scene.control.pagination.PaginationRefSkin;

/**
 * from oracle tutorial
 * http://docs.oracle.com/javase/8/javafx/user-interface-tutorial/pagination.htm#JFXUI459
 * 
 * modified as test driver for PaginationXSkin
 */
public class PaginationXExample extends Application {
    
    private Pagination pagination;
//    String[] fonts = new String[]{};
    List<String> fonts;
 
    public static void main(String[] args) throws Exception {
        launch(args);
    }
 
    public int itemsPerPage() {
        return 15;
    }
 
    public VBox createPage(int pageIndex) {        
        VBox box = new VBox(5);
        int page = pageIndex * itemsPerPage();
        for (int i = page; i < fonts.size() && i < page + itemsPerPage(); i++) {
            Label font = new Label(fonts.get(i));  // (fonts[i]);
            box.getChildren().add(font);
        }
        return box;
    }
 
    @Override
    public void start(final Stage stage) throws Exception {
        // restrict to 4 pages
        fonts = Font.getFamilies(); //.subList(0, 60); //.toArray(fonts);
        
//        pagination = new Pagination(fonts.size()/itemsPerPage(), 0){
        pagination = new Pagination(){
            
            // support custom navigation controls
            // http://stackoverflow.com/q/31540001/203657
            @Override
            protected Skin createDefaultSkin() {
                return new PaginationRefSkin(this);
//                return new CustomPaginationSkin(this);
            }
        };
        pagination.setStyle("-fx-border-color:red;");
        pagination.setPageFactory(pageIndex -> createPage(pageIndex));
 
        AnchorPane anchor = new AnchorPane();
        AnchorPane.setTopAnchor(pagination, 10.0);
        AnchorPane.setRightAnchor(pagination, 10.0);
        AnchorPane.setBottomAnchor(pagination, 10.0);
        AnchorPane.setLeftAnchor(pagination, 10.0);
        anchor.getChildren().addAll(pagination);
        Scene scene = new Scene(anchor, 400, 450);
        stage.setScene(scene);
        stage.setTitle("PaginationSample");
        stage.show();
        
    }
 
    /**
     * Hack for custom navigation control
     * http://stackoverflow.com/q/31540001/203657
     * https://bugs.openjdk.java.net/browse/JDK-8132131
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class CustomPaginationSkin extends PaginationSkin {

        private HBox controlBox;
        private Button prev;
        private Button next;
        private Button first;
        private Button last;

        private void patchNavigation() {
            Pagination pagination = getSkinnable();
            Node control = pagination.lookup(".control-box");
            if (!(control instanceof HBox))
                return;
            controlBox = (HBox) control;
            prev = (Button) controlBox.getChildren().get(0);
            next = (Button) controlBox.getChildren().get(controlBox.getChildren().size() - 1);
            
            first = new Button("A");
            first.setOnAction(e -> {
                pagination.setCurrentPageIndex(0);
            });
            first.disableProperty().bind(
                    pagination.currentPageIndexProperty().isEqualTo(0));
            
            last = new Button("Z");
            last.setOnAction(e -> {
                pagination.setCurrentPageIndex(pagination.getPageCount());
            });
            last.disableProperty().bind(
                    pagination.currentPageIndexProperty().isEqualTo(
                            pagination.getPageCount() - 1));
            
            ListChangeListener childrenListener = c -> {
                while (c.next()) {
                    // implementation detail: when nextButton is added, the setup is complete
                    if (c.wasAdded() && !c.wasRemoved() // real addition
                            && c.getAddedSize() == 1 // single addition
                            && c.getAddedSubList().get(0) == next) { 
                        addCustomNodes();
                    }
                }
            };
            controlBox.getChildren().addListener(childrenListener);
            addCustomNodes();
        }

        protected void addCustomNodes() {
            // guarding against duplicate child exception (some weird internals...)
            if (first.getParent() == controlBox) return;
            controlBox.getChildren().add(0, first);
            controlBox.getChildren().add(last);
        }
        
        /**
         * @param pagination
         */
        public CustomPaginationSkin(Pagination pagination) {
            super(pagination);
            patchNavigation();
        }
         
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(PaginationXExample.class
            .getName());
}