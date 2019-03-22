/*
 * Created on 21.03.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Answer to 
 * https://stackoverflow.com/q/55249695/203657
 * Disallow half-visible rows
 * 
 * Has visual quirks ..
 */
public class ListViewScrollTweakAnswer  extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
        Scene sc = new Scene(root); //, 600, 200);
        stage.setScene(sc);
        stage.show();
        List<String> items = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            items.add("Item "+i);
        }
        ObservableList<String> itemList = FXCollections.observableArrayList();
        itemList.addAll(items);
        ScrollListView<String> list = new ScrollListView<>();
//        list.setFixedCellSize(20);
        list.setItems(itemList);
        root.getChildren().add(list);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    class ScrollListView<T> extends ListView<T>{
        boolean firstRender = false;
        public ScrollListView() {
            needsLayoutProperty().addListener((obs,old,needsLayout)->{
                if(!firstRender && !needsLayout){
                    firstRender = true;
                    VirtualFlow<?> virtualFlow = (VirtualFlow<?>) lookup(".virtual-flow");

                    // Keeping vertical scrollBar node reference and tweaking the vertical scroll bar behavior to
                    // scroll by row and not by pixel.
                    ScrollBar vScrollBar = (ScrollBar) queryAccessibleAttribute(AccessibleAttribute.VERTICAL_SCROLLBAR);
                    vScrollBar.valueProperty().addListener((obs1, oldVal1, newVal) -> {
                        int visibleRowCount = virtualFlow.getLastVisibleCell().getIndex()-virtualFlow.getFirstVisibleCell().getIndex();
                        final double scrollVal = newVal.doubleValue();
                        final int size = getItems().size();
                        if (scrollVal < 1.0) {
                            final int virtualRowCount = size - visibleRowCount;
                            final double eachRowBuff = 1d / virtualRowCount;
                            for (int index = 0; index < virtualRowCount; index++) {
                                final double start = eachRowBuff * index;
                                final double end = start + eachRowBuff;
                                if (start <= scrollVal && scrollVal < end) {
                                    scrollTo(index);
                                    vScrollBar.setValue(start);
                                    break;
                                }
                            }
                        } else {
                            scrollTo(size - 1);
                        }
                    });
                }
            });
        }
    }
}

