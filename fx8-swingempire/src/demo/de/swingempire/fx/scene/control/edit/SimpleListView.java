/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author tarrsalah.org
 */
public class SimpleListView extends Application {

        private ListView<String> simpleList;

        @Override
        public void start(Stage primaryStage) {
                simpleList = new ListView<>(FXCollections.observableArrayList("Item1", "Item2", "Item3", "Item4"));
                simpleList.setEditable(true);

                simpleList.setCellFactory(TextFieldListCell.forListView());             

                simpleList.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
                        @Override
                        public void handle(ListView.EditEvent<String> t) {
                            System.out.println("setOnEditCommit " + t.getIndex() + " /" + t);
                                simpleList.getItems().set(t.getIndex(), t.getNewValue());
                                if (t.getIndex() == simpleList.getItems().size() - 1) {
                                    int index = t.getIndex() + 1;
                                    simpleList.getSelectionModel().select(index);
                                    simpleList.getItems().add("newItem");
                                    System.out.println("setOnEditCommit - last " + t.getIndex() + " /" + t);
//                                    simpleList.edit(index);
                                }
                        }
                                                
                });

                simpleList.setOnEditCancel(new EventHandler<ListView.EditEvent<String>>() {
                        @Override
                        public void handle(ListView.EditEvent<String> t) {
                                System.out.println("setOnEditCancel " + t.getIndex() + " /" + t);
                        }
                });

                BorderPane root = new BorderPane(simpleList);
                Scene scene = new Scene(root, 300, 250);

                primaryStage.setTitle("Hello World!");
                primaryStage.setScene(scene);
                primaryStage.show();
        }

        /**
         * The main() method is ignored in correctly deployed JavaFX application.
         * main() serves only as fallback in case the application can not be
         * launched through deployment artifacts, e.g., in IDEs with limited FX
         * support. NetBeans ignores main().
         *
         * @param args the command line arguments
         */
        public static void main(String[] args) {
                launch(args);
        }
}
