/*
 * Created on 21.10.2014
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.ArrayList;
import java.util.List;

import de.swingempire.fx.control.TableViewSample.PlainTableCell;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Here we have a list that's equal to the new list because its items are equals.
 * Should be covered by 15793? Why not?
 * 
 * 
 * Since 8u40b12, this can be handled by a custom ListCell that overrides
 * isItemChanged to return false if new/old item aren't identical.
 * Since 8u60, the remaining part of the bug (tableRowCell needs custom
 * skin as well) is fixed also.
 * https://javafx-jira.kenai.com/browse/RT-39094

 * 
 * @author dosiennik
 */
public class TestCaseTable22463 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Test case");
        primaryStage.setX(0);
        primaryStage.setY(0);
        Button refreshButton = new Button("Refresh");
        final TableView<Person22463> table = new TableView<>();
        // tweak prior to fix
//        table.setRowFactory(p -> new IdentityCheckingTableRow());
        table.setTableMenuButtonVisible(true);
        TableColumn c1 = new TableColumn("Id");
        TableColumn c2 = new TableColumn("Name");
        c1.setCellValueFactory(new PropertyValueFactory<Person22463, Long>("id"));
        c2.setCellValueFactory(new PropertyValueFactory<Person22463, String>("name"));
        c2.setPrefWidth(200);
        table.getColumns().addAll(c1, c2);

        // with new api, since 8u40b12
        Callback rowFactory = p -> {
            return new TableRow() {

                @Override
                protected boolean isItemChanged(Object oldItem,
                        Object newItem) {
                    return oldItem != newItem;
                }
                
                // still needs help of skin to trigger update of child cells
                //@Override
                //protected Skin<?> createDefaultSkin() {
                //    return new TableRowSkinX(this);
                //}

                
            };
        };
        table.setRowFactory(rowFactory);
        Callback<TableColumn<Person22463, String>, TableCell<Person22463, String>> cellFactory = p -> {
            return new PlainTableCell<Person22463, String>() {

                @Override
                protected boolean isItemChanged(String oldItem, String newItem) {
                    return oldItem != newItem;
                }

            };
        };
        // not needed, why not?
//        c2.setCellFactory(cellFactory);

        
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                // Without clearing, the table isn't refreshed.
//                table.getItems().clear();
                List<Person22463> person = getPerson2();
                table.setItems(FXCollections.observableList(person));
            }
        });

        Button setItem = new Button("refresh first item");
        setItem.setOnAction(e -> {
            // this is updating
            Person22463 person = table.getItems().get(0);
            table.getItems().set(0, createEqualPerson(person));
        });

        BorderPane pane = new BorderPane();
        pane.setCenter(table);
        
        Parent buttons = new HBox(refreshButton, setItem);
        pane.setBottom(buttons);

        primaryStage.setScene(new Scene(pane, 400, 200));
        primaryStage.show();

        List<Person22463> person = getPerson1();
        table.setItems(FXCollections.observableList(person));

    }

    /**
     * @param person
     * @return
     */
    private Person22463 createEqualPerson(Person22463 person) {
        Person22463 p = new Person22463();
        p.setId(person.getId());
        p.setName(person.getName() + "X");
        return p;
    }

    private List<Person22463> getPerson1() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("name2");
        p.add(p1);
        p.add(p2);
        return p;
    }

    private List<Person22463> getPerson2() {
        List<Person22463> p = new ArrayList<>();
        Person22463 p1 = new Person22463();
        p1.setId(1l);
        p1.setName("updated name1");
        Person22463 p2 = new Person22463();
        p2.setId(2l);
        p2.setName("updated name2");
        p.add(p1);
        p.add(p2);
        return p;
    }
}