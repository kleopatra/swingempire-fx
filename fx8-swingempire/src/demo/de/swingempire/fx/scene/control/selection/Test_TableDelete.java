/*
 * Created on 02.04.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Last row unselectable by mouse if second last is deleted
 * 
 * Scroll down fully to the last row.
Select the row before the last one.
Click on the Delete button.
Try to select the last row with the mouse.

 * https://javafx-jira.kenai.com/browse/RT-40401
 * http://stackoverflow.com/q/29334706/203657
 * 
 * reported to be intermittant - couldn't reproduce any time with exact example
 * but always with:
 * 
 * - cell selection enabled
 * - run: focus rect on first cell
 * - END to scroll to end (focus unchanged? shouldn't the selection be moved? 
 *   at least that's what happens in !cellSelectionEnabled)
 * - ctrl-END to move focus to first cell of last row
 * - ctrl-up to focus first cell of second last row
 * - click delete
 * - click into last row: focus rect unchanged on first cell of second last row
 * 
 * Note: need a minimal # of rows, something like 1.3 - 1.5 pages, related
 * to creating new cells on scrolling?
 *   
 */
public class Test_TableDelete extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final TableView<String> tableView = new TableView();
//        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int index = 0; index < 10; index++) {
            final TableColumn<String, String> tableColumn = new TableColumn(String.valueOf(index));
            tableColumn.setCellValueFactory(param -> {
                return new SimpleStringProperty(param.getValue());
            });
            tableView.getColumns().add(tableColumn);
        }
        final Button populateButton = new Button("Populate");
        populateButton.setOnAction(actionEvent -> populate(tableView));
        final Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(actionEvent -> removeSelected(tableView));
        final ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(populateButton, new Separator(Orientation.VERTICAL), deleteButton);
        final BorderPane root = new BorderPane();
// root.setBottom(tableView);
        root.setCenter(tableView);
        root.setTop(toolBar);
        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        //
        populate(tableView);
    }

    private void populate(final TableView<String> tableView) {
        tableView.getItems().clear();
        for (int value = 0; value < 18; value++) {
            tableView.getItems().add(String.valueOf(value));
        }
    }

    private void removeSelected(final TableView<String> tableView) {
        final int selectedIndex = tableView.getSelectionModel().getFocusedIndex();
        tableView.getItems().remove(selectedIndex);
    }
}
