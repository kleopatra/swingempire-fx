/*
 * Created on 07.12.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/a/42664066/203657
 * suboptimal custom cell .. commented
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnCellData extends Application {

    
    private Parent createContent() {
        TableView<Person> table = new TableView<>(Person.persons());
        for (int i = 0; i < 5; i++) {
//            table.getItems().addAll(Person.persons());
        }
        TableColumn<Person, String> name = new TableColumn<>("First Name");
        name.setCellValueFactory(cc -> cc.getValue().firstNameProperty());

        TableColumn<Person, String> other = new TableColumn<>("Derived");
        other.setCellValueFactory(cc -> cc.getValue().firstNameProperty());

        Callback<TableColumn<Person, String>, TableCell<Person, String>> test = tc -> new TableCell<>();
        LOG.info("" + test.call(null));
        
        Callback<TableColumn<Person, String>, TableCell<Person, String>> circle_cell_factory = (
                final TableColumn<Person, String> param) -> {
            final TableCell<Person, String> circle_cell = new TableCell<Person, String>() {
                final Circle circle = new Circle(6);

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    String data = param.getCellData(getIndex()) + item;
                    System.out.println(data + getIndex() + param);
                    if (!empty) {
                        if (data.startsWith("E")) {
                            circle.setFill(Color.RED);
                        } else {
                            circle.setFill(Color.GREEN);
                        }
                        setGraphic(circle);
                    }
                }
            };
            return circle_cell;
        };
        other.setCellFactory(circle_cell_factory);
        table.getColumns().addAll(name, other);

        BorderPane content = new BorderPane(table);
        return content;
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
            .getLogger(TableColumnCellData.class.getName());

}
