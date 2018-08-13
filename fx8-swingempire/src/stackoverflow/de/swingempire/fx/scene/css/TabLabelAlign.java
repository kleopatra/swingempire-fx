/*
 * Created on 31.07.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Align label text on tab via css
 * https://stackoverflow.com/q/51818703/203657
 * 
 * configuring content-display doesn't matter because the label seems to be centered
 * 
 * TabHeaderArea 
 *    contains .headers-regions 
 *       contains TabHeaderSkin per tab (no style)
 *          contains .tab-container (field name inner is-a StackPane)
 *              contains .tab-label .tab-close-button .focus-indicator
 * 
 * .tab-container is doing the actual layout of the .tab-label
 * it sizes the label at its prefSize and positions it centered in itself (modulo close button)
 * 
 * But: content-display is the wrong property - 
 *   it controls the relative positioning of text and graphic
 *   
 * To make the label appear left/right aligned there are two steps necessary:
 *    - use alignement property: controls the position of (text + graphic) if label is 
 *      wider than that what they need
 *    - make sure there's excess width     
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabLabelAlign extends Application {

    
    private Parent createContent() {
        TabPane pane = new TabPane();
        pane.setTabMinWidth(140);
        pane.getTabs().addAll(
                new Tab("one"), new Tab("looooooongish"), new Tab("last"));
        pane.getTabs().forEach(t -> t.setGraphic(new Button("o")));
        Label compare = new Label("compare");
        compare.setPrefWidth(200);
        compare.setContentDisplay(ContentDisplay.RIGHT);
        BorderPane content = new BorderPane(pane);
        content.setTop(compare);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        scene.getStylesheets().add(getClass().getResource("tablabel.css").toExternalForm());
        stage.setScene(scene);

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TabLabelAlign.class.getName());

}
