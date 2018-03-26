/*
 * Created on 04.10.2017
 *
 */
package test.combobox;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * slight visual glitch the very first time of opening: 
 * height too small
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8188249
 */
public class ListViewHeightTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<Integer> items = FXCollections.observableArrayList();
        LoaderService service = new LoaderService();

        Label pi = new Label("please wait...");
        ComboBox<Integer> cb = new ComboBox<>(items);
        cb.setPrefWidth(150);
        cb.setVisibleRowCount(20);
        cb.setPlaceholder(pi);
        cb.setOnShown(ev -> {
            ListView<?> lv = getListView(cb);
            lv.setMinHeight(70);
            lv.autosize();
            System.out.println("onShown " + lv.getHeight() + ev);
            if (items.isEmpty())
                service.restart();
        });
        service.setOnSucceeded(ev -> {
            items.setAll(service.getValue());
            ListView<?> lv = getListView(cb);
            lv.autosize();
            System.out.println("onSucceeded " + lv.getHeight());
        });

        Button reset = new Button("reset");
        reset.setOnAction(event -> items.clear());

        TilePane root = new TilePane();
        Scene scene = new Scene(root, 500, 300);
        root.getChildren().addAll(cb, reset);
        primaryStage.setScene(scene);
        primaryStage.show();
        // play with access to popup - can be useful to configure popup
        // which is not accessible
        // getWindows is new to fx9, how to in fx8?
        Stage.getWindows().addListener(this::windowsChanged);
        // listView is added as child initially by skin, then removed when showing in popup
        cb.getChildrenUnmodifiable().addListener(this::childrenChanged);
    }

    /**
     * ListView is added as child to combo in constructor of ComboBoxListViewSkin (why?).
     * Later when showing the popup for the first time, it is removed again, indirectly
     * by setting the popupControl's skin to an adhoc skin that returns the list
     * as getNode(). Doing so will set the node as single child of the CSSbridge, thus
     * removing it from the combo again.
     * 
     * <p>
     * adding the listView was done to fix
     * https://bugs.openjdk.java.net/browse/JDK-8115587 (formerly RT-21207)
     * but meant to be reverted again? it's still in ...
     * <p>
     * same add/remove implementation in fx8, so we can get hold of the listView by
     * using public api only (that is, no com.sun.xx)
     * 
     * @param c
     */
    protected void childrenChanged(Change<? extends Node> c) {
        while (c.next()) {
            if (c.wasRemoved()) {
                Node node = c.getRemoved().get(0);
                if (node instanceof ListView) {
                    System.out.println("parent: " + node.getParent());
                    node.parentProperty().addListener((src, ov, nv) -> System.out.println("parent changed: " + nv));
                }
            }
        }
        new RuntimeException("who? ").printStackTrace();
//        FXUtils.prettyPrint(c);
    }
    protected void windowsChanged(Change<? extends Window> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                Window popup = c.getAddedSubList().get(0);
                if (popup instanceof PopupControl) {
                    PopupControl popupControl = (PopupControl) popup;
                    Skin<?> skin = popupControl.getSkin();
                    System.out.println(skin.getSkinnable().getClass());
                    
                }
            }
        }
//        FXUtils.prettyPrint(c);
    }
    protected ListView<?> getListView(ComboBox<Integer> cb) {
        return (ListView<?>) ((ComboBoxListViewSkin<?>) cb.getSkin())
                .getPopupContent();
    }

    class LoaderService extends Service<List<Integer>> {

        Random rd = new Random();

        int counter = 5;

        @Override
        protected Task<List<Integer>> createTask() {
            return new Task<List<Integer>>() {

                @Override
                protected List<Integer> call() throws Exception {
                    Thread.sleep(1500);
//                    return Stream.iterate(1, i -> i+1).limit(rd.nextInt(6) + 5)
//                            .collect(Collectors.toList());
                    return Stream.iterate(1, i -> i + 1).limit(counter++)
                            .collect(Collectors.toList());
                }
            };
        }

    }
} 
