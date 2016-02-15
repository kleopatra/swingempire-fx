/*
 * Created on 14.07.2014
 *
 */
package de.swingempire.fx.control;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * http://stackoverflow.com/q/35392740/203657
 * detect when virtualized onctrol scrolled to bottom
 * 
 * Would expect a scrollTo eventHandler to be notified, but isn't?
 */
public class ScrollToHandlerList extends Application {
    private final ObservableList<Locale> data =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
   
    private final ListView<Locale> list = new ListView<>(data);
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("List ScrollTo Handler " + FXUtils.version());
        Button leftScrollTo = new Button("scrollto last");
        leftScrollTo.setOnAction(e -> {
            // explicit calling notifies the onScrollHandler
            list.scrollTo(list.getItems().size() -1);
        });
        // Would expect a onScrollTo handler getting notified
        // but isn't ...
        list.setOnScrollTo(e -> {
            LOG.info("target/size" + e.getScrollTarget() + " / " + data.size());
        });
        // nor a onScroll handler ...
        list.setOnScroll(e -> {
            LOG.info("" + e);
        });
        // nor a onScroll handler ...
        list.setOnScrollStarted(e -> {
            LOG.info("" + e);
        });
        // nor a onScroll handler ...
        list.setOnScrollFinished(e -> {
            LOG.info("" + e);
        });
        list.addEventFilter(ScrollEvent.ANY, e -> {
            LOG.info("in filter " + e);
        });

        HBox root = new HBox(leftScrollTo, list);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        
        // dirty: lookup the scrollBar and listener to its value property
        ScrollBar bar = (ScrollBar) list.lookup(".scroll-bar");
        bar.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("change on value " + nv);
            if (nv.doubleValue() == 1.) {
                LOG.info("at max");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ScrollToHandlerList.class
            .getName());
}