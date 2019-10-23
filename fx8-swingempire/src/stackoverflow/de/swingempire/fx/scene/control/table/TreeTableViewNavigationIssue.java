/*
 * Created on 23.10.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8232825 cell navigation not working,
 * left/right expand/collapse item, don't navigate
 * 
 * 
 * OP tracked into changes in behavior
 * 
 */
public class TreeTableViewNavigationIssue extends Application {

    public static void main(String[] args) {
        System.err.println(Runtime.version().toString());
        Application.launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        System.err.println(System.getProperty("javafx.version"));
        final VBox parent = new VBox();
        final List<Map<String, String>> data = createData(20);
        final TreeTableView<Map<String, String>> tableView = createTableView(
                data);
        fillTableData(tableView, data);
        parent.getChildren().add(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        final ScrollPane sp = new ScrollPane(parent);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);
        final Scene aScene = new Scene(sp, 800, 600);
        primaryStage.setScene(aScene);
        primaryStage.show();
    }

    private void fillTableData(
            final TreeTableView<Map<String, String>> aTableView,
            final List<Map<String, String>> aData) {
        final TreeItem<Map<String, String>> root = new TreeItem<>();
        root.getChildren().setAll(
                aData.stream().map(TreeItem::new).collect(Collectors.toList()));
        final var firstChild = root.getChildren().get(0);
        final var childData = createData(5);
        firstChild.getChildren().setAll(childData.stream().map(TreeItem::new)
                .collect(Collectors.toList()));
        aTableView.setShowRoot(false);
        aTableView.setRoot(root);
    }

    private TreeTableView<Map<String, String>> createTableView(
            final List<Map<String, String>> aData) {
        final TreeTableView<Map<String, String>> table = new TreeTableView<>();
        final Map<String, String> firstRow = aData.get(0);
        final List<TreeTableColumn<Map<String, String>, String>> columns = firstRow
                .keySet().stream().map(text -> {
                    final TreeTableColumn<Map<String, String>, String> column = new TreeTableColumn<>(
                            text);
                    column.setCellValueFactory(param -> {
                        final TreeItem<Map<String, String>> v = param
                                .getValue();
                        final Map<String, String> map = v.getValue();
                        return new ReadOnlyStringWrapper(map.get(text));
                    });
                    return column;
                }).collect(Collectors.toList());
        table.getColumns().addAll(columns);
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        return table;
    }

    private List<Map<String, String>> createData(final int rowNumber) {
        final int colNumber = 12;
        final List<Map<String, String>> data = new ArrayList<>(rowNumber);
        for (int i = 0; i < rowNumber; i++) {
            final Map<String, String> row = new LinkedHashMap<>(colNumber);
            for (int k = 0; k < colNumber; k++) {
                row.put("Column_" + k, "Value " + UUID.randomUUID().toString()
                        .substring((int) (Math.random() * 20d)));
            }
            data.add(row);
        }
        return data;
    }

}
