/*
 * Created on 25.03.2019
 *
 */
package de.swingempire.testfx.table;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static de.swingempire.testfx.table.TablePrefSizeFactory.*;
import static de.swingempire.testfx.util.TableFactory.*;
import static de.swingempire.testfx.matcher.VirtualContainerMatchers.*;
import static de.swingempire.testfx.matcher.CellMatchers.*;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TablePrefSizeTest extends ApplicationTest {

    TableView<Locale> table;
    
    @Test
    public void testFlowPrefIsSumOfRows() {
        VirtualFlow flow = getVirtualFlow(table);
        double width = table.getWidth();
        double sum = 0;
        // implementation detail: flow measures hard-coded first 10 rows
        for (int i = 0; i < 10; i++) {
            TableRow<Locale> row = tableRowFor(table, i);
            sum += row.prefHeight(width);
        }
        // default measures hbar height
        if (sum < flow.prefHeight(width)) {
            ScrollBar hbar = getHorizontalScrollBar(table);
            sum += hbar.prefHeight(width);
        }
        assertEquals("viewport height must be same as sum of rows", sum, flow.prefHeight(width), .1);
        
    }
    
    @Test
    public void testInitialPrefHeight() {
        VirtualFlow flow = getVirtualFlow(table);
        TableHeaderRow header = getTableHeader(table);
        double width = table.getWidth();
        double combined = flow.prefHeight(width) + header.prefHeight(width);
        assertEquals(table.prefHeight(width), combined, .1);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        table = createTablePrefSize();
        table.getSelectionModel().setCellSelectionEnabled(true);
        
        List<Locale> filtered =         
                Arrays.stream(Locale.getAvailableLocales())
                    .filter(l -> l.getDisplayName() != null && !l.getDisplayName().isBlank())
                    // find and remove duplicates
//                    .collect(collectingAndThen(
//                            toCollection(() -> new TreeSet<>(comparing(Locale::getDisplayName))),
//                            ArrayList::new));
                     .collect(toList());
        ObservableList<Locale> data = FXCollections.observableArrayList(filtered);
        table.setItems(data);
        
        table.getColumns().addAll(createTableColumn("displayName"));
        Pane root = new BorderPane(table);
        stage.setScene(new Scene(root));
        stage.show();
    }
    


}
