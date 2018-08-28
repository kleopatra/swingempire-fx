/*
 * Created on 28.08.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.scene.control.edit.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52055082/203657
 * at which time can we access the tableHeader?
 * 
 * answer: when the table is added to the scenegraph,
 * that is when the skin property is set
 * 
 * Note the nice trick to read-access inaccessible children
 * by sunflame :)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableApplyCustomHeader extends Application {

    private Parent createContent() {
        TableView<Person> table = new TableView(Person.persons());
        
        table.skinProperty().addListener(new ChangeListener<Skin>() {

            @Override
            public void changed(ObservableValue<? extends Skin> observable,
                    Skin oldValue, Skin newValue) {
                if (newValue != null) {
                    TableViewSkin<?> skin = (TableViewSkin<?>) table.getSkin();
                    LOG.info("header: " + getTableHeader(table));
                }
                
            }
            
        });
        BorderPane content = new BorderPane(table);
        return content;
    }

    private TableHeaderRow getTableHeader(TableView<?> tableView) {
        return (TableHeaderRow) tableView.queryAccessibleAttribute(AccessibleAttribute.HEADER);
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
            .getLogger(TableApplyCustomHeader.class.getName());

}
