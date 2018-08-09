/*
 * Created on 09.08.2018
 *
 */
package test.combobox;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8209135
 * Incorrect behaviour of contextMenu
 * 
 * Steps:
1. Execute the attached program.
2. Right click on any control to show the related ContextMenu.
=> Observe that both context menu & popup are displayed.

3. Click on the popup.
4. onAction listener is called and the listener remove that control from scene.
=> Observe that the ContextMenu is still shown.

Expected behavior:
1. Popup should be displayed only on left mouse click.
2. ContextMenu should hide when the control is removed from scene. 

 * Reason might be the mouseHandlers registered in comboBoxBase: triggers the corresponding
 * methods in comboBoxBaseBehaviour which do some internal magic to open/close the popup
 * to resolve an old issue (RT-18151, new https://bugs.openjdk.java.net/browse/JDK-8118434) 
 * without checking for left/right mouse button
 * 
 * 
 * 
 * @author ambarish
 */
public class PopupContextMenuTest extends Application {

    public static void main(String[] args) {
        PopupContextMenuTest.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = new VBox();
        final ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().add("Test1");
        comboBox.getItems().add("Test2");
        ContextMenu cmComboBox = new ContextMenu();
        cmComboBox.getItems().addAll(new MenuItem("CM_Item1"), new MenuItem("CM_Item2"));
        comboBox.setContextMenu(cmComboBox);
        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.getChildren().remove(comboBox);
            }
        });

        ColorPicker colorPicker = new ColorPicker();
        ContextMenu cmColorPicker = new ContextMenu();
        cmColorPicker.getItems().addAll(new MenuItem("CM_Item1"), new MenuItem("CM_Item2"));
        colorPicker.setContextMenu(cmColorPicker);
        colorPicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.getChildren().remove(colorPicker);
            }
        });

        DatePicker datePicker = new DatePicker();
        ContextMenu cmDatePicker = new ContextMenu();
        cmDatePicker.getItems().addAll(new MenuItem("CM_Item1"), new MenuItem("CM_Item2"));
        datePicker.setContextMenu(cmDatePicker);
        datePicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                root.getChildren().remove(datePicker);
            }
        });

        root.getChildren().add(comboBox);
        root.getChildren().add(colorPicker);
        root.getChildren().add(datePicker);

        stage.setScene(new Scene(root, 400, 500));
        stage.show();
    }
}