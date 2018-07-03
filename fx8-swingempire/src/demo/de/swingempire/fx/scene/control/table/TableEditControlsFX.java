/*
 * Created on 21.06.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.scene.control.cell.DebugTextFieldTableCell;
import de.swingempire.fx.scene.control.cell.TextField2TableCell;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Testdriver for TextFieldTableCell in ControlsFX.
 * @author Jeanette Winzenburg, Berlin
 */
public class TableEditControlsFX extends Application {

    private Parent createTabContent(Callback cellFactory) {
        TableView<Dummy> table = new TableView<>(createData(50));
        table.setEditable(true);
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(cellFactory);
        
        table.getColumns().addAll(column);
        
        table.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F4) {
                table.scrollTo(table.getItems().size());
                e.consume();
            }
        });
        BorderPane pane = new BorderPane(table);
        return pane;
    }


    private Parent createContent() {
        TabPane tabPane = new TabPane();
        
        addTab(tabPane, "core", TextFieldTableCell.forTableColumn());
        addTab(tabPane, "controlsfx", TextField2TableCell.forTableColumn());
        addTab(tabPane, "debugtablecell", DebugTextFieldTableCell.forTableColumn());
        
        return tabPane;
    }

    protected Tab addTab(TabPane pane, String title, Callback cellFactory
            ) {
        Tab tab = new Tab(title);
        pane.getTabs().add(tab); 
        tab.setContent(createTabContent(cellFactory));
        return tab;
    }

    private ObservableList<Dummy> createData(int size) {
        return FXCollections.observableArrayList(
                Stream.generate(Dummy::new)
                .limit(size)
                .collect(Collectors.toList()));
    }
    
    private static class Dummy {
        private static int count;
        StringProperty value = new SimpleStringProperty(this, "value", "initial " + count++);
        public StringProperty valueProperty() {return value;}
        public String getValue() {return valueProperty().get(); }
        public void setValue(String text) {valueProperty().set(text); }
        public String toString() {return "[dummy: " + getValue() + "]";}
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableEditControlsFX.class.getName());

}
