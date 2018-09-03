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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52145683/203657
 * ScrollPane resets to 0 on expanding
 * to see: scroll table to right, collapse, expand
 * expected: scrollPane at value on collapse
 * actual: scrollPane at 0
 * 
 * It's special to handling in VirtualFlow: my comment on SO
 * "looks like the VirtualFlow is the culprit: it does some hearty optimization of 
 * cell layout - in particular, short-circuits if height or width is 0 
 * (done in collapse by TitledPaneSkin) and sets the hbar's visibility to 
 * false which in turn triggers resetting its value to 0 ..."
 * 
 * Here: try a custom skin with memory of last known scroll position.
 * Seems to work, but needs more logic to distinguish from real hiding ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TitledPaneTableScroll extends Application {

    public static class TableViewScrollSkin<T> extends TableViewSkin<T> {

        DoubleProperty hvalue = new SimpleDoubleProperty();
        
         public TableViewScrollSkin(TableView<T> control) {
            super(control);
            installHBarTweak();
        }

        private void installHBarTweak() {
            // Note: flow and bar could be legally retrieved via lookup 
            // protected api pre-fx9 and post-fx9
            VirtualFlow<?> flow = getVirtualFlow();
            // access scrollBar via reflection 
            ScrollBar bar = (ScrollBar) FXUtils
                    .invokeGetFieldValue(VirtualFlow.class, flow, "hbar");
            bar.valueProperty().addListener((s, o, n) -> {
                if (n.intValue() == 0) {
                    bar.setValue(hvalue.get());
                    // debugging
                    //  new RuntimeException("who is calling? \n").printStackTrace();
                } else {
                    hvalue.set(n.doubleValue());
                }
                //LOG.info("hbar value: " + n + "visible? " + bar.isVisible());
            });
            
            bar.visibleProperty().addListener((s, o, n) -> {
                if (n) {
                    bar.setValue(hvalue.get());
                } 
            });
        }
    }
    
    int counter;
    private Parent createContent() {
        
        TableView<Object> table = new TableView<>(FXCollections.observableArrayList(new Object()) ) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new TableViewScrollSkin<>(this);
            }
            
        };
        table.getColumns().addAll(Stream
                .generate(TableColumn::new)
                .limit(10)
                .map(col -> {
                    col.setPrefWidth(50);
                    col.setText("" + counter++);
                    return col;
                })
                .collect(Collectors.toList())); 
        

        TitledPane titled = new TitledPane("title", table);
        titled.setAnimated(true);
        
        BorderPane content = new BorderPane(titled);
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
            .getLogger(TitledPaneTableScroll.class.getName());

}
