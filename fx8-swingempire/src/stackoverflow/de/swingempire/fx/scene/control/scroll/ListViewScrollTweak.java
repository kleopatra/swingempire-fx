/*
 * Created on 21.03.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55249695/203657
 * Disallow half-visible rows
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ListViewScrollTweak extends Application {

    public static class LListViewSkin<T> extends ListViewSkin<T> {

        /**
         * @param control
         */
        public LListViewSkin(ListView<T> control) {
            super(control);
            LOG.info("" + getVirtualFlow().getCellCount());
        }

        
        @Override
        protected double computePrefHeight(double width, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            double pref = ((LVirtualFlow) getVirtualFlow()).computePrefHeight(width);
//            return super.computePrefHeight(width, topInset, rightInset, bottomInset,
//                    leftInset);
            return pref;
        }


        @Override
        protected VirtualFlow<ListCell<T>> createVirtualFlow() {
            return new LVirtualFlow();
        }
        
        
        
    }
    
    public static class LVirtualFlow extends VirtualFlow {

        /**
         * Overridden to allow access to super
         */
        @Override
        protected double computePrefHeight(double width) {
            double pref = super.computePrefHeight(width);
            LOG.info("flow: " + pref + " / " + getParent().getClass());
            return pref;
        }
        
    }

    private Parent createContent() {
        List<String> data = Stream
                .iterate(1, c -> c+1)
                .limit(100)
                .map(c -> "item " + c)
                .collect(Collectors.toList());
        ListView<String> control = new ListView<>(FXCollections.observableList(data)) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new LListViewSkin<>(this);
            }
            
        };
        control.setFixedCellSize(25);
        
        Button logSizes = new Button("Log Sizes");
        logSizes.setOnAction(e -> {
            LOG.info("pref/actual: " + control.prefHeight(-1) + " / " + control.getHeight());
        });
//        BorderPane content = new BorderPane(list);
        
        VBox content = new VBox(10, control, logSizes);
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
            .getLogger(ListViewScrollTweak.class.getName());

}
