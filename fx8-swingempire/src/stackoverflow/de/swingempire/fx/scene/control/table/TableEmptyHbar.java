/*
 * Created on 08.02.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/54589836/203657
 * 
 * Bug or feature:
 * Hbar not shown if table is empty
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableEmptyHbar extends Application {

    private Parent createContent() {
        TableView<Person> table = new TableView<>();
        table.getColumns().addAll(createColumn("firstName"),createColumn("lastName"));
        table.setTableMenuButtonVisible(true);
//        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        Button toggle = new Button("toggle data");
        toggle.setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                table.getItems().addAll(Person.persons());
            } else {
                table.getItems().clear();
            }
        });
        
        Button debug = new Button("debug width");
        debug.setOnAction(e -> {
            TableColumn column = table.getColumns().get(1);
            LOG.info("width/min/pref/max: " + column.getWidth() + " / " + column.getMinWidth() 
                    + " / " + column.getPrefWidth() + " / " + column.getMaxWidth());
        });
        
        BorderPane content = new BorderPane(table);
        content.setBottom(new FlowPane(toggle, debug));
        return content;
    }

    /**
     * @param text
     * @return
     */
    protected TableColumn<Person, String> createColumn(String text) {
        TableColumn<Person, String> first = new TableColumn<>(text);
        first.setMinWidth(300);
        first.setMaxWidth(400);
        first.setPrefWidth(-1);
        return first;
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
            .getLogger(TableEmptyHbar.class.getName());

}
