/*
 * Created on 16.11.2017
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableSkinTest extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        TableView<Person> table = new TableView<>(Person.persons()) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new WTableViewSkin(this); //super.createDefaultSkin();
            }
            
        };
        
        TableColumn<Person, String> first = new TableColumn<>("First Name");
        first.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<Person, String> last = new TableColumn<>("Last Name");
        last.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<Person, String> mail = new TableColumn<>("EMail");
        mail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        table.getColumns().addAll(first, last, mail);
        
        BorderPane pane = new BorderPane(table);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(getContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableSkinTest.class.getName());
}
