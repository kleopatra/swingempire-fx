/*
 * Created on 03.09.2018
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52145683/203657
 * ScrollPane resets to 0 on expanding
 * to see: scroll table to right, collapse, expand
 * expected: scrollPane at value on collapse
 * actual: scrollPane at 0
 * 
 * could force a reset of scroll by setting max of container to zero.
 * but no idea why
 * 
 * giving up
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TitledPaneScroll extends Application {

    int counter;
    private Parent createContent() {
        
        TableView<Object> table = new TableView<>(FXCollections.observableArrayList(new Object()));
        table.getColumns().addAll(Stream
                .generate(TableColumn::new)
                .limit(10)
                .map(col -> {
                    col.setPrefWidth(50);
                    col.setText("" + counter++);
                    return col;
                })
                .collect(Collectors.toList())); 
        
//        TitledPane titled = new TitledPane("title", table);
//        titled.setAnimated(false);
        
        // pure stackPane is fine on toggle visible
        StackPane titled = new StackPane();
        titled.getChildren().setAll(table);
//        titled.setMinHeight(0);
//        titled.setPrefHeight(0);
        
        // check whether it's related to visible toggle
        // no
        Button hide =  new Button("toggle visible");
        hide.setOnAction(e -> {
            table.setVisible(!table.isVisible());
        });
        
        double pref = 100;
        Button max = new Button("max");
        max.setOnAction(e -> {
            // this resets the scroll
            boolean isComputed = titled.getMaxHeight() < 0;
            titled.setMaxHeight(isComputed ? 0 : Region.USE_COMPUTED_SIZE);
            table.setVisible(!isComputed);
        });
        
//        Button prefB = new Button("pref");
//        prefB.setOnAction(e -> {
//            // no effect
//            boolean isComputed = titled.getPrefHeight() < 0;
//            titled.setPrefHeight(isComputed ? pref : Region.USE_COMPUTED_SIZE);
//        });
        
//        Button resize = new Button("resize");
//        resize.setOnAction(e -> {
//            double currentHeight = titled.getHeight();
//            double currentWidth = titled.getWidth();
//            // no effect
//            titled.resize(currentWidth, currentHeight == pref ? ((Region) titled.getParent()).getHeight() : pref);
//        });
        HBox buttons = new HBox(10, hide, max/*, prefB , resize*/);
        
//        ScrollPane scroll = new ScrollPane(titled);
//        scroll.setFitToWidth(true);
//        AnchorPane content = new AnchorPane(titled);
        BorderPane content = new BorderPane(titled);
        content.setBottom(buttons);
//        titled.prefHeightProperty().bind(content.heightProperty());
//        titled.prefWidthProperty().bind(content.prefWidthProperty());
//        scroll.prefWidthProperty().bind(content.widthProperty());

        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 400));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TitledPaneScroll.class.getName());

}
