/*
 * Created 17.03.2022
 */

package de.swingempire.fx.scene.control.table;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/25870593/203657
 * column headers misaligned with columns initially
 *
 * division of horizontal space:
 * - for headerst the completed table width (including the width of the scrollbar)
 * - for columns the viewport width (excluding the width of the scrollbar)
 *
 * happens if
 * - table has constrained resize policy
 * - vertical scrollbar visible
 *
 * headers snap into the correct width after scrolling or resizing a bit
 *
 * Note: all fine if items added before showing the stage
 * Note: the setup is slightly unusual, table -> vbox -> scrollpane - without the scrollPane
 *   all is fine as well (except fx 8, when adding items after showing)
 *
 * Same for all versions fx8, fx11, fx18+
 *
 */
public class MisalignedColumnHeaders extends Application
{
    public static void main(String[] args) throws Exception
    {
        launch(args);
    }

    @Override
    public void start(Stage primarystage) throws Exception
    {
        // Create layout
        VBox root = new VBox();

        TableView<TableObject> table = new TableView<TableObject>();
        TableColumn<TableObject, String> col1 = new TableColumn<TableObject, String>("Column 1");
        TableColumn<TableObject, String> col2 = new TableColumn<TableObject, String>("Column 2");
        table.getColumns().addAll(col1, col2);

        col1.setCellValueFactory(new PropertyValueFactory<TableObject, String>("column1"));
        col2.setCellValueFactory(new PropertyValueFactory<TableObject, String>("column2"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button set = new Button("setItems");
        set.setOnAction(e -> {
            addItems(table);
        });

        root.getChildren().addAll(table, set);


//        ScrollPane scrollpane = new ScrollPane();
//        scrollpane.setFitToWidth(true);
//        scrollpane.setFitToHeight(true);
//        scrollpane.setPrefSize(500, 200);
//        scrollpane.setContent(root);

        // Create and show scene
//        Scene scene = new Scene(scrollpane);
        Scene scene = new Scene(root, 500, 200);
        primarystage.setScene(scene);
        primarystage.show();

        addItems(table);
    }

    private void addItems(TableView<TableObject> table) {
        // Populate table
        ArrayList<TableObject> data = new ArrayList<TableObject>();
        for (int i = 0; i < 20;)
        {
            TableObject entry = new TableObject(String.valueOf(i++), String.valueOf(i++));
            data.add(entry);
        }

        table.setItems(FXCollections.observableArrayList(data));
    }

    public class TableObject
    {
        private StringProperty column1;
        private StringProperty column2;

        public TableObject(String col1, String col2)
        {
            column1 = new SimpleStringProperty(col1);
            column2 = new SimpleStringProperty(col2);
        }

        public StringProperty column1Property()
        {
            return column1;
        }

        public StringProperty column2Property()
        {
            return column2;
        }
    }
}