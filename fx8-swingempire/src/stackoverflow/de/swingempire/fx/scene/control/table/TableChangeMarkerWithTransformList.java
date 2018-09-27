/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;
import java.util.logging.Logger;

import de.swingempire.fx.collection.ChangeDecorator;
import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableChangeMarkerWithTransformList extends Application {

    public static class DecoTableRow<T> extends TableRow<T> {

        final PseudoClass marked = PseudoClass.getPseudoClass("marked");

        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            boolean highlight = false;
            
            if (!isEmpty() 
                    && getTableView() != null 
                    && getTableView().getItems() instanceof ChangeDecorator 
                    && getItem() != null) {
                highlight = ((ChangeDecorator<T>) getTableView().getItems()).isDirty(getItem());
            }
            pseudoClassStateChanged(marked, highlight);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            // we dont get here if the list fired an update
            super.updateItem(item, empty);
        }
        
    }
    
    private Parent createContent() {
        
        ObservableList<Person> persons = FXCollections.observableList(Person.persons()
                , e -> new Observable[] {e.firstNameProperty()});
        ChangeDecorator<Person> decorator = new ChangeDecorator<>(persons);
        decorator.addListener((ListChangeListener<Person>)c -> {
            while (c.next()) {
                if (c.wasUpdated()) {
//                    LOG.info("dirty? " + decorator.isDirty(c.getList().get(c.getFrom()))  + c);
                }
            };
        });
        TableView<Person> table = new TableView<>(decorator);
        
        table.setRowFactory(c -> new DecoTableRow<>());
        
        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(p -> p.getValue().firstNameProperty());
        firstNameCol.getStyleClass().add("mark-column");
        
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(p -> p.getValue().lastNameProperty());
        
        table.getColumns().addAll(firstNameCol, lastNameCol);
        
        Button changeFirst = new Button("Change First Name");
        changeFirst.setOnAction(e -> {
            Person three = table.getItems().get(3);
            three.setFirstName(three.getFirstName() + "X");
        });
        
        table.getStylesheets().add(this.getClass().getResource("tablecolor.css").toExternalForm());

        HBox buttons = new HBox(10, changeFirst);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
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
            .getLogger(TableChangeMarkerWithTransformList.class.getName());

}
