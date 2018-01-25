/*
 * Created on 24.01.2018
 *
 */
package control.skin;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Problem: replace empty placeholder and show just empty rows.
 * long standing issue (2013): https://bugs.openjdk.java.net/browse/JDK-8090949
 * 
 * on SO: https://stackoverflow.com/q/16992631/203657
 * 
 * Trying to hack around:
 * 
 * - handled in TableViewSkinBase updatePlaceHolderRegionVisibility which sets the
 *   placeholder visible to true and the flow visible to false
 * - called whenever item/column count changes.
 * - skin manages a stackPane placeHolderRegion, which contains the placeholder as
 *   single child (either default label or property from table)
 *   
 * Idea of the hack:
 * - listen to the visible property of placeholder parent and reset flow visible to true
 *   always
 * - seems to work, 
 * - except for the very beginning: the placeholder is added lazily the
 *   very first time that updatePlaceHolderRegionvisible is called  
 * - and except when resizing window while empty  
 * - can be remedied by faking the itemCount to min of 1 -> problem with
 *   keys to move the selection (cells disappear)  
 * @author Jeanette Winzenburg, Berlin
 */
public class EmptyPlaceholdersInSkin extends Application {

    TableView<Person> table;
    
    
    public static class NoPlaceHolderSkin<T> extends TableViewSkin<T>{

        VirtualFlow<?> flowAlias;
        TableHeaderRow headerAlias;
        Parent placeholderRegion;
        Node placeholderNode; // don't really care it's only the initial placeholder
        ChangeListener<Boolean> visibleListener = (src, ov, nv) -> visibleChanged(nv);
        ChangeListener<Parent> parentListener = (src, ov, nv) -> parentChanged(nv);
        ListChangeListener<Node> childrenListener = c -> childrenChanged(c);
        /**
         * @param table
         */
        public NoPlaceHolderSkin(TableView<T> table) {
            super(table);
            flowAlias = (VirtualFlow<?>) table.lookup(".virtual-flow");
            headerAlias = (TableHeaderRow) table.lookup(".column-header-background");
            // default placeholder - don't really care, interested in parent
            // which is lazily created
            placeholderNode = new StackPane();
            placeholderNode.parentProperty().addListener(parentListener);
            table.setPlaceholder(placeholderNode);
            
//            LOG.info("has parent?" + placeholderNode.getParent());
//            placeholderRegion = placeholderNode.getParent();
//            
//            if (placeholderRegion != null) {
//                placeholderRegion.visibleProperty().addListener(visibleListener);
//                visibleChanged(true);
//                // hmm ... maybe the flow is not yet instantiated with row cells?
////                flow.setVisible(true);
////                placeholderRegion.setVisible(false);
//            }
//            table.sceneProperty().addListener(sceneListener);
//            LOG.info("has scene? " + table.getScene());
//            if (table.getScene() != null) {
//                LOG.info("window" + table.getScene().getWindow());
//                sceneChanged(table.getScene());
//            }
        }
        
        
        /**
         * @param c
         * @return
         */
        protected void childrenChanged(Change<? extends Node> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends Node> addedSubList = c.getAddedSubList();
                    boolean added = hasPlaceHolderRegion(addedSubList);
                    
                }
            }
        }


        /**
         * @param addedSubList
         */
        private boolean hasPlaceHolderRegion(
                List<? extends Node> addedSubList) {
            List<Node> parents = addedSubList.stream()
                    .filter(e -> e.getStyleClass().contains("placeholder"))
                    .collect(Collectors.toList());
            if (!parents.isEmpty()) {
                
            }
            return false;
        }


        @Override
        protected void layoutChildren(double x, double y, double width,
                double height) {
            super.layoutChildren(x, y, width, height);
            if (getItemCount()> 0) return;
            // super didn't layout the flow if empty- do it now
            final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
            double headerHeight = headerAlias.getHeight();
            y += headerHeight;
            double flowHeight = Math.floor(height - headerHeight);
            layoutInArea(flowAlias, x, y,
                    width, flowHeight,
                    baselineOffset, HPos.CENTER, VPos.CENTER);


        }

        private void parentChanged(Parent nv) {
            LOG.info("getting parent: " + nv);
            if (nv == null) return;
            placeholderRegion =  nv;
            placeholderRegion.visibleProperty().addListener(visibleListener);
            visibleChanged(true);
//            placeholderRegion.setVisible(false);
        }

        /**
         * @param nv
         * @return
         */
        private void visibleChanged(Boolean nv) {
            LOG.info("visible: " + nv);
            if (nv) {
                flowAlias.setVisible(true);
                placeholderRegion.setVisible(false);
            }
        }
        
        
    }
    private Parent createContent() {
        // okay if initially populated
        table = new TableView<>() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new NoPlaceHolderSkin<>(this);
            }
            
        };
        TableColumn<Person, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        table.getColumns().addAll(first);
        
        Button clear = new Button("clear");
        clear.setOnAction(e -> table.getItems().clear());
        clear.disableProperty().bind(Bindings.isEmpty(table.getItems()));
        Button fill = new Button("populate");
        fill.setOnAction(e -> table.getItems().setAll(Person.persons()));
        fill.disableProperty().bind(Bindings.isNotEmpty(table.getItems()));
        BorderPane pane = new BorderPane(table);
        pane.setBottom(new HBox(10, clear, fill));
        return pane;
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(EmptyPlaceholdersInSkin.class.getName());

}
