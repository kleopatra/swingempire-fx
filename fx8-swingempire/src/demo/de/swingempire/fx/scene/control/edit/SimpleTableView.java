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
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author tarrsalah.org
 */
public class SimpleTableView extends Application {

        private TableView<Person> simpleList;

        @Override
        public void start(Stage primaryStage) {
                simpleList = new TableView<>(Person.persons());
                simpleList.setEditable(true);
 
                TableColumn<Person, String> first = new TableColumn<>("first name");
                first.setCellFactory(TextFieldTableCell.forTableColumn());    
                first.setCellValueFactory(new PropertyValueFactory<>("firstName"));

                first.setOnEditCommit(t ->
                            System.out.println("setOnEditCommit " + t.getTablePosition() + " /" + t));
//                                simpleList.getItems().set(t.getIndex(), t.getNewValue());
//                                if (t.getIndex() == simpleList.getItems().size() - 1) {
//                                    int index = t.getIndex() + 1;
//                                    simpleList.getSelectionModel().select(index);
//                                    simpleList.getItems().add("newItem");
//                                    System.out.println("setOnEditCommit - last " + t.getIndex() + " /" + t);
////                                    simpleList.edit(index);
//                                }

                first.setOnEditCancel(t -> 
                    System.out.println("setOnEditCancel " + t.getTablePosition() + " /" + t.getRowValue()));
                        
                simpleList.getColumns().addAll(first);

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
