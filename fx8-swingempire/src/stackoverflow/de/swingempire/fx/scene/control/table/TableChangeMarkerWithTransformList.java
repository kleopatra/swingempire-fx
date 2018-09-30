/*
 * Created on 27.09.2018
 *
 */
package de.swingempire.fx.scene.control.table;
import java.util.logging.Logger;

import de.swingempire.fx.collection.ChangeDecorator;
import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.util.Duration;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TableChangeMarkerWithTransformList extends Application {

    public static class DecoTableRow<T> extends TableRow<T> {

        final PseudoClass marked = PseudoClass.getPseudoClass("marked");

        /**
         * Overridden to update the dirty marker visuals.
         * Note: need to do it here because updateItem is not called
         * for update changes.
         */
        @Override
        public void updateIndex(int i) {
            super.updateIndex(i);
            boolean highlight = false;
            
            if (!isEmpty() 
                    && getTableView() != null 
                    && getTableView().getItems() instanceof ChangeDecorator 
                    && getItem() != null) {
                highlight = ((ChangeDecorator<T>) getTableView().getItems()).isChanged(getItem());
            }
            pseudoClassStateChanged(marked, highlight);
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
        
        // must not!! just see what happens
        Button changeOffFX = new Button("Change First Name Off FX");
        changeOffFX.setOnAction(e -> {
            Runnable startTimeline = () -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    Person three = table.getItems().get(3);
                    three.setFirstName(three.getFirstName() + "X");
                        LOG.info("end: on fx " + Platform.isFxApplicationThread());
                    
                
                  // using a timeline we force a delayed change on the fx thread  
//                Timeline tl = new Timeline(new KeyFrame(
//                        Duration.millis(2000) , ae -> {
//                            Person three = table.getItems().get(3);
//                            three.setFirstName(three.getFirstName() + "X");
//                        LOG.info("end: on fx " + Platform.isFxApplicationThread());
//                           
//                        }));
//                LOG.info("start: on fx " + Platform.isFxApplicationThread());
//                tl.play();
            };
            Thread th = new Thread(startTimeline);
            th.start();
        });
        
        // quick check: Keyframe action is called on fx thread
        // doc'ed?
        Button changeOnThread = new Button("Play timeline off FX");
        changeOnThread.setOnAction(e -> {
            Runnable startTimeline = () -> {
                // this does the update on FX thread
                Timeline tl = new Timeline(new KeyFrame(
                        Duration.millis(2000) , ae -> LOG.info("end: on fx " + Platform.isFxApplicationThread())));
                LOG.info("start: on fx " + Platform.isFxApplicationThread());
                tl.play();
            };
            Thread th = new Thread(startTimeline);
            th.start();
        });
        
        table.getStylesheets().add(this.getClass().getResource("tablecolor.css").toExternalForm());

        HBox buttons = new HBox(10, changeFirst, changeOnThread, changeOffFX);
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
