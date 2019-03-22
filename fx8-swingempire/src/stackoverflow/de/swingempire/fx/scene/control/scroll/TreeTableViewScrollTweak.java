/*
 * Created on 21.03.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.scene.control.scroll.TableViewScrollTweak.TVirtualFlow;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.control.skin.TreeTableViewSkin;
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
public class TreeTableViewScrollTweak extends Application {

    public static class TTreeTableViewSkin<T> extends TreeTableViewSkin<T> {

        public TTreeTableViewSkin(TreeTableView<T> control) {
            super(control);
//            updateItemCount();
            LOG.info("" + getVirtualFlow().getCellCount());
        }

        
        // no rows at all ... only header taken
        @Override
        protected double computePrefHeight(double width, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            double pref = ((TTVirtualFlow) getVirtualFlow()).computePrefHeight(width);
//            LOG.info("skin: " + pref + getSkinnable().getScene().getWindow()); 
//            new RuntimeException("who is calling? " + pref + "\n").printStackTrace();
            return  pref;
//            return super.computePrefHeight(width, topInset, rightInset, bottomInset,
//                    leftInset);
        }


        @Override
        protected VirtualFlow<TreeTableRow<T>> createVirtualFlow() {
            return new TTVirtualFlow();
        }
        
        
    }
    
    public static class TTVirtualFlow extends VirtualFlow {

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
//        List<String> data = Stream
//                .iterate(1, c -> c+1)
//                .limit(100)
//                .map(c -> "item " + c)
//                .collect(Collectors.toList());
        List<TreeItem<Locale>> treeItems = Arrays.stream(Locale.getAvailableLocales()).map(TreeItem::new).collect(Collectors.toList());
        TreeItem<Locale> root = new TreeItem<>(new Locale("dummy"));
        root.setExpanded(true);
        root.getChildren().addAll(treeItems);
        TreeTableView<Locale> control = new TreeTableView<>(root) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new TTreeTableViewSkin(this);
            }
            
        };
        TreeTableColumn<Locale, String> name = new TreeTableColumn<>("DisplayName");
        name.setCellValueFactory(new TreeItemPropertyValueFactory<Locale, String>("displayName"));
        control.getColumns().addAll(name);
        
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
            .getLogger(TableViewScrollTweak.class.getName());

}
