/*
 * Created on 24.01.2018
 *
 */
package control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Problem: replace empty placeholder and show just empty rows.
 * long standing issue (2013): https://bugs.openjdk.java.net/browse/JDK-8090949
 * 
 * on SO: https://stackoverflow.com/q/16992631/203657
 * 
 * Trying to hack around:
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
 * - seems to work, except for the very beginning: the placeholder is added lazily the
 *   very first time that updatePlaceHolderRegionvisible is called  
 *   
 * @author Jeanette Winzenburg, Berlin
 */
public class EmptyPlaceholders extends Application {

    TableView<Person> table;
    Parent placeholderRegion;
    Node placeholderNode;
    ChangeListener<Skin> skinListener = (src, ov, nv) -> skinChanged(nv);
    ChangeListener<Boolean> visibleListener = (src, ov, nv) -> visibleChanged(nv);
    ChangeListener<Parent> parentListener = (src, ov, nv) -> parentChanged(nv);
    
    private void skinChanged(Skin<?> nv) {
        table.skinProperty().removeListener(skinListener);
        StackPane placeholderNode = new StackPane();
//        placeholderNode.setVisible(false);
        // that's the style for parent which is the node that's visible is toggles
//        placeholderRegion.getStyleClass().setAll("placeholder");
        placeholderNode.parentProperty().addListener(parentListener);
        table.setPlaceholder(placeholderNode);
    }
    
    /**
     * @param nv
     * @return
     */
    private void visibleChanged(Boolean nv) {
        LOG.info("visible: " + nv);
        if (nv) {
            Node flow = table.lookup("VirtualFlow");
            flow.setVisible(true);
            placeholderRegion.setVisible(false);
        }
    }

    private void parentChanged(Parent nv) {
        LOG.info("getting parent: " + nv);
        placeholderRegion =  nv;
        placeholderRegion.visibleProperty().addListener(visibleListener);
        placeholderRegion.setVisible(false);
    }

    private Parent createContent() {
        // okay if initially populated
        table = new TableView<>(Person.persons());
        TableColumn<Person, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        table.getColumns().addAll(first);
        table.skinProperty().addListener(skinListener);
        
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
            .getLogger(EmptyPlaceholders.class.getName());

}
