/*
 * Created on 27.09.2016
 *
 */
package de.swingempire.fx.scene.control.table;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.ControlAcceleratorSupport;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * Problem: accelerator in table's corner menu not working.
 * 
 * Asked on SO: http://stackoverflow.com/q/39721544/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class TableViewAccelerator extends Application {
    private Parent getContent() {
        TableView table = new TableView<>();
        TableColumn first = new TableColumn<>("first");
        table.getColumns().addAll(first);
        
        table.setTableMenuButtonVisible(true);
        
        Button addMenu = new Button("add MenuItem to corner - F3");
        addMenu.setOnAction(e -> {
            TableViewSkin skin = (TableViewSkin) table.getSkin();
            
            TableHeaderRow header = //skin.getTableHeaderRow();
                    (TableHeaderRow) invokeGetMethodValue(
                            TableViewSkinBase.class, skin, "getTableHeaderRow");
            ContextMenu menu = (ContextMenu) invokeGetFieldValue(
                    TableHeaderRow.class, 
                    header, "columnPopupMenu");
            MenuItem item = new MenuItem("do stuff");
            item.setOnAction(me -> {
                LOG.info("from corner - F3");
            });
            item.setAccelerator(KeyCombination.valueOf("F3"));
            menu.getItems().add(item);
            ControlAcceleratorSupport.addAcceleratorsIntoScene(menu.getItems(), table);
            addMenu.setDisable(true);
        });
        
        ContextMenu menu = new ContextMenu();
        MenuItem contextItem = new MenuItem("initial");
        contextItem.setOnAction(e -> {
            LOG.info("from initial");
        });
        contextItem.setAccelerator(KeyCombination.valueOf("F4"));
        menu.getItems().addAll(contextItem);
        table.setContextMenu(menu);
        Button addToContext = new Button("add MenuItem to context - F5");
        addToContext.setOnAction(e -> {
            MenuItem added = new MenuItem("added");
            added.setOnAction(me -> LOG.info("from added - F5"));
            added.setAccelerator(KeyCombination.valueOf("F5"));
            menu.getItems().addAll(added);
            addToContext.setDisable(true);
        });
        
        BorderPane pane = new BorderPane(table);
        FlowPane buttons = new FlowPane(10, 10);
        buttons.getChildren().addAll(addMenu, addToContext);
        pane.setBottom(buttons);
        return pane;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 400));
//        LOG.info(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public static Object invokeGetMethodValue(Class declaringClass, Object target, String name) {
        try {
            Method field = declaringClass.getDeclaredMethod(name);
            field.setAccessible(true);
            return field.invoke(target);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeGetFieldValue(Class declaringClass, Object target, String name) {
        try {
            Field field = declaringClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewAccelerator.class.getName());
}
