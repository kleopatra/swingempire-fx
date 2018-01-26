/*
 * Created on 24.01.2018
 *
 */
package control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Problem: replace empty placeholder and show just empty rows.
 * long standing issue (2013): https://bugs.openjdk.java.net/browse/JDK-8090949
 * 
 * on SO: https://stackoverflow.com/q/16992631/203657
 * 
 * Trying to hack around: (experimented with fx9)
 * 
 * - handled in TableViewSkinBase updatePlaceHolderRegionVisibility which sets the
 *   placeholder visible to true and the flow visible to false
 * - called whenever item/column count changes.
 * - skin manages a stackPane placeHolderRegion, which contains the placeholder as
 *   single child (either default label or property from table)
 *   
 * Idea of the hack:
 * - listen to the visible property of placeholder parent and reset flow visible to true
 *   always
 * - seems to work, 
 * - except for the very beginning: the placeholder is added lazily the
 *   very first time that updatePlaceHolderRegionvisible is called  
 * - and except when resizing window while empty  
 * - fx9 only (checked against fx8 is fine): 
 *   can be remedied by faking the itemCount to min of 1 -> problem with
 *   keys to move the selection (cells disappear) 
 *   
 *   
 * @author Jeanette Winzenburg, Berlin
 */
public class EmptyPlaceholdersInSkin extends Application {

    public static class FakeItemCount<T> extends TableViewSkin<T> {

        /**
         * @param arg0
         */
        public FakeItemCount(TableView<T> arg0) {
            super(arg0);
        }
        
        @Override
        public int getItemCount() {
            int r = super.getItemCount();
            return r == 0 ? 1 : r;
        }
        
    }
    private Parent createContent() {
        // initially populated
        //TableView<Person> table = new TableView<>(Person.persons()) {
        // initially empty
        TableView<Person> table = new TableView<>() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new FakeItemCount(this);
//                return new NoPlaceHolderTableViewSkin<>(this);
            }
            
        };
        TableColumn<Person, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        table.getColumns().addAll(first);
        
        Button clear = new Button("clear");
        clear.setOnAction(e -> table.getItems().clear());
        clear.disableProperty().bind(Bindings.isEmpty(table.getItems()));
        Button fill = new Button("populate");
        fill.setOnAction(e -> table.getItems().setAll(Person.persons()));
        fill.disableProperty().bind(Bindings.isNotEmpty(table.getItems()));
        BorderPane pane = new BorderPane(table);
        pane.setBottom(new HBox(10, clear, fill));
        return pane;
    }


    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(EmptyPlaceholdersInSkin.class.getName());

}
