/*
 * Created on 02.08.2019
 *
 */
package de.swingempire.fx.scene.control.table;


import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotResult;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * both header and hbar appear in constrained mode, example from
 * https://bugs.openjdk.java.net/browse/JDK-8115476
 * 
 * hbar flickers on/of, header seems to be only above vbar (is-corner)
 * @author Jeanette Winzenburg, Berlin
 */
public class TableColumnResizeBug extends Application {


        public static void main(String[] args) {
                launch(args);
        }

        @Override
        public void start(final Stage stage) throws Exception {
                stage.setTitle("TableView Demo");
                

                VBox pane = new VBox();
                pane.setPadding(new Insets(10));

                ObservableList<Person> items = FXCollections.observableArrayList(people(100));
                final TableView<Person> tableView = new TableView<>(items);
                tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                final TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
                firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
                final TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
                lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));

                tableView.getColumns().addAll(firstNameColumn, lastNameColumn);

                Button button = new Button("take snapshot");
                button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(final ActionEvent event) {

                                WritableImage wim = new WritableImage(300, 500);
                                stage.getScene().snapshot(new Callback<SnapshotResult, Void>() {
                                        @Override
                                        public Void call(final SnapshotResult param) {
                                                try {
                                                        ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "png", new File("tableview_constrained_resize_policy.png"));
                                                } catch (IOException e) {
                                                        e.printStackTrace();
                                                }
                                                return null;
                                        }
                                }, wim);


                        }
                });

                pane.getChildren().addAll(tableView, button);

                Scene scene = new Scene(pane, 300, 500, Color.DODGERBLUE);
                stage.setScene(scene);
                stage.show();
        }

        public static ObservableList<Person> people(int howMany) {
                ObservableList<Person> result = FXCollections.observableArrayList();
                for (int i = 0; i < howMany; i++) {
                        result.add(new Person("firstname " + i, "lastname " + i));

                }
                return result;
        }

        public static class Person {
                private final String firstName;
                private final String lastName;

                public Person(final String firstName, final String lastName) {
                        this.firstName = firstName;
                        this.lastName = lastName;
                }

                public String getFirstName() {
                        return firstName;
                }

                public String getLastName() {
                        return lastName;
                }
        }

}

