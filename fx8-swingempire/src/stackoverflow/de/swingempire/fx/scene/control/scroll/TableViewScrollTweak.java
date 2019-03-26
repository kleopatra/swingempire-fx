/*
 * Created on 21.03.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.skin.TableViewSkinBaseDecorator;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/55249695/203657
 * Disallow half-visible rows
 * 
 * The exact requirement is unclear, but the initial vertical pref of both list/table
 * seem to be off by half a row or so. Hard-coded to 400?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewScrollTweak extends Application {

    public static class TTableViewSkin<T> extends TableViewSkin<T> 
        implements TableViewSkinBaseDecorator {

        public TTableViewSkin(TableView<T> control) {
            super(control);
            // this is fix of bug https://bugs.openjdk.java.net/browse/JDK-8221334
            // configure cell count
            updateItemCount();
        }

        
        /**
         *  delegating to the flow without the config fix doesn't show any rows at all
         *  (because the flow has not yet any cells)
         */
        @Override
        protected double computePrefHeight(double width, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            // delegate to flow for pref of viewport
            double pref = ((TVirtualFlow) getVirtualFlow()).computePrefHeight(width);
            double header = getTableHeader().prefHeight(width);
//            LOG.info("skin: " + pref + getSkinnable().getScene().getWindow()); 
//            new RuntimeException("who is calling? " + pref + "\n").printStackTrace();
            return  pref + header;
//            double superResult = super.computePrefHeight(width, topInset, rightInset, bottomInset,
//                    leftInset);
//            return superResult;
        }


        @Override
        protected VirtualFlow<TableRow<T>> createVirtualFlow() {
            return new TVirtualFlow();
        }
        
        
    }
    
    public static class TVirtualFlow extends VirtualFlow {

        /**
         * Overridden to allow access to super
         */
        @Override
        protected double computePrefHeight(double width) {
            LOG.info("parent: " + getParent());
            double pref = super.computePrefHeight(width);
//            LOG.info("flow: " + pref + " / " + getParent().getClass());
            return pref;
        }
        
    }
    private Parent createContent() {
//        List<String> data = Stream
//                .iterate(1, c -> c+1)
//                .limit(100)
//                .map(c -> "item " + c)
//                .collect(Collectors.toList());
        TableView<Locale> control = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales())) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new TTableViewSkin(this);
            }
            
        };
        TableColumn<Locale, String> name = new TableColumn<>("DisplayName");
        name.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        control.getColumns().addAll(name);
        
//        control.setFixedCellSize(31);
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
        stage.setX(1000);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewScrollTweak.class.getName());

}
