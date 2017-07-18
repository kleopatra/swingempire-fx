/*
 * Created on 10.12.2015
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Acceleartors expected to work on current (focused/selected) tab
 * http://stackoverflow.com/q/34198022/203657
 * 
 * it's a bug: 
 * https://bugs.openjdk.java.net/browse/JDK-8088068
 * 
 * It's a rather crude bug:
 * <li> all accelerators of all contextMenus are collected into a single
 * Map (on the Scene) with the accelerator as key
 * <li> implies that only the last installed is ever active
 * <li> implies that actions from unrelated contextMenues can be invoke,
 *  if the accelerator isn't in this
 *  <li> doesn't help to disable (the formerly overridden are lost)
 *  <li> install happens at the time of control.setContextMenu
 *  <li> the workaround in the bug report handles duplicates, but not foreign
 *  
 *  <p>
 *  Note though, that accelerators in Swing's popup don't work at all!
 *  https://docs.oracle.com/javase/tutorial/uiswing/components/menu.html
 */
public class AcceleratorOnTab extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Menu m;
        Tab t1 = new Tab("Tab 1");
        TableView<Void> tv1 = new TableView<>();
        t1.setContent(tv1);
        MenuItem mi1 = new MenuItem("Action 1");
        mi1.setAccelerator(KeyCombination.valueOf("F3"));
        mi1.setOnAction(event -> System.out.println("Action 1!"));
        ContextMenu ctx1 = new ContextMenu(mi1);
        tv1.setContextMenu(ctx1);

        Tab t2 = new Tab("Tab 2");
        TableView<Void> tv2 = new TableView<>();
        t2.setContent(tv2);
        MenuItem mi2 = new MenuItem("Action 2");
        mi2.setAccelerator(KeyCombination.valueOf("F3"));
        mi2.setOnAction(event -> System.out.println("Action 2!"));
        ContextMenu ctx2 = new ContextMenu(mi2);
        tv2.setContextMenu(ctx2);

        TabPane tabPane = new TabPane(t1, t2);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        primaryStage.setScene(new Scene(tabPane));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
