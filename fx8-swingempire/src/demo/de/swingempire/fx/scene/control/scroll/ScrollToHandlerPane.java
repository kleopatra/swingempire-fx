/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/q/35392740/203657
 * detect when virtualized onctrol scrolled to bottom
 * 
 * ScrollEvent is low-level! So listeners are notified only if triggered
 * by mouse wheel (f.i.)
 */
public class ScrollToHandlerPane extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
   
    private final Pane pane = new VBox();

    private int count;
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Node Scroll Handler " + FXUtils.version());
        ScrollPane scroller = new ScrollPane(pane);
        Button leftScrollTo = new Button("scrollto last");
        leftScrollTo.setOnAction(e -> {
            scroller.setVvalue(1.);
        });
        pane.getChildren().addAll(Stream.generate(() -> new Label("item " + count++))
                .limit(50).collect(Collectors.toList()));
        
        pane.addEventFilter(ScrollEvent.ANY, e -> {
            LOG.info("in filter " + e);
        });
        // nor a onScroll handler ...
        pane.setOnScroll(e -> {
            LOG.info("" + e);
        });
        // nor a onScroll handler ...
        pane.setOnScrollStarted(e -> {
            LOG.info("" + e);
        });
        // nor a onScroll handler ...
        pane.setOnScrollFinished(e -> {
            LOG.info("" + e);
        });
        BorderPane root = new BorderPane(scroller);
        Scene scene = new Scene(root, 300, 400);
        stage.setScene(scene);
        stage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ScrollToHandlerPane.class
            .getName());
}