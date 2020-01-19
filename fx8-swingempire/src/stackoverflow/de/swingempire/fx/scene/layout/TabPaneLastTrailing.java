/*
 * Created on 19.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * glue last tab to the right of header
 * https://stackoverflow.com/q/59807669/203657
 * 
 * can't really interfere with internal layout .. it's the TabHeaderArea's headersRegion 
 * which layouts the tabHeaderSkins (the visual rep) of the tab manually.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabPaneLastTrailing extends Application {

    public static class StickyLastTabSkin extends TabPaneSkin {

        StackPane headerArea;
        StackPane headersRegion;
        
        public StickyLastTabSkin(TabPane control) {
            super(control);
            headerArea = (StackPane) control.lookup(".tab-header-area");
            headersRegion = (StackPane) control.lookup(".headers-region");
            System.out.println(headersRegion.getChildren().size() + "/" + control.getTabs().size());
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            Region last = (Region) headersRegion.getChildren().get(2);
//            last.setPadding(new Insets(0, 0, 0, 100));
            last.setLayoutX(300);
            
            super.layoutChildren(x, y, w, h);
            // hard-coded
//            last.relocate(w - last.prefWidth(-1), y);
        }
        
        
        
    }
    
    private Parent createContent() {
        TabPane pane = new TabPane() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new StickyLastTabSkin(this);
            }
            
        };
        pane.getTabs().addAll(
                new Tab("one"), new Tab("looooo"), new Tab("last"));
        Label compare = new Label("compare oohhh");
        compare.setPrefWidth(200);
//        compare.setContentDisplay(ContentDisplay.RIGHT);
        pane.getTabs().get(0).setContent(compare);
        
        Button styleableNode = new Button("styleable");
        styleableNode.setOnAction(e -> System.out.println("tab styleable: " + pane.getTabs().get(0).getStyleableNode()));

        BorderPane content = new BorderPane(pane);
        content.setBottom(new HBox(10, styleableNode));
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
            .getLogger(TabPaneLastTrailing.class.getName());

}
