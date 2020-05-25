/*
 * Created on 15.05.2020
 *
 */
package de.swingempire.fx.collections;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * List not updated on updating item property (with extractor)
 * bug in ObservableSequentialListWrapper, doesn't handle extractor in modifications
 * https://stackoverflow.com/q/61809390/203657
 */
public class ListViewTest extends Application {
    private ObservableList<Book> bookList;
    private int tmpIdSeq = 100; // temp autogen ID for testing

    private ObservableList<Book> createBookList() {
        // create the callback for updates
        Callback<Book, Observable[]> extr =
                (Book b) -> new Observable[]{
                    b.idProperty(),
                    b.titleProperty()
            };
        // create the list of books
        List<Book> tmpList = new LinkedList<>(); 
        // add a few books
        for (int i=2; i<6; i++) {
            Book b = new Book();
            b.setId(i);
            b.setTitle("Citadel " + i);
            tmpList.add(b);
        }
        ObservableList<Book> startList = 
                FXCollections.observableList(tmpList, extr);
        // return the observable list
        return startList;
    }

    @Override
    public void start(Stage primaryStage) {
        // setup the UΙ
        VBox root = new VBox();
        root.setPadding(new Insets(5));
        HBox btnBar = new HBox();
        Label statusLbl = new Label("");

        // create the ListView using the observable list
        bookList = createBookList();
        ListView<Book> listView = new ListView<>(bookList);

        // add a selection listener
        listView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // prints current & previous  selection
                    statusLbl.setText("Curr: " 
                            + (newValue != null ? newValue.getTitle() : "<None>")
                            + ",  Prev: " 
                            + (oldValue != null ? oldValue.getTitle() : "<None>"));
                }
        );         

        // add a change listener to the observable list of the ListView
        bookList.addListener(
                (ListChangeListener.Change<? extends Book> change) -> {
                    String txt = "list changed";
                    System.out.println(txt);
                    while (change.next()) {
                        if (change.wasPermutated()) {
                            // not implemented
                        } else if (change.wasReplaced()) {
                            // not implemented
                        } else if (change.wasRemoved()) {
                            // not implemented
                        } else if (change.wasAdded()) {
                            txt = txt + ", wasAdded";
                            System.out.println("added new");
                        } else if (change.wasUpdated()) {
                            txt = txt + ", wasUpdated";
                            System.out.println("updated");
                        }
                        statusLbl.setText(txt);
                    }
                });

        // create sample buttons: add & update
        Button btnAdd = new Button("Add");
        btnAdd.setOnAction((ActionEvent ev) -> {
            // adds a new entry in the list
            Book b = new Book();
            b.setId(tmpIdSeq);
            b.setTitle("The " + tmpIdSeq++);
            bookList.add(b);
        });
        Button btnUpdate = new Button("Update");
        btnUpdate.setOnAction((ActionEvent ev) -> {
            // modify the title of the selected book
            Book b = listView.getSelectionModel().getSelectedItem();
            b.setTitle(b.getTitle() + " I");
        });

        // showtime
        btnBar.getChildren().addAll(btnAdd, btnUpdate);
        root.getChildren().addAll(listView, btnBar, statusLbl);
        Scene scene = new Scene(root); 
        primaryStage.setTitle("ListView Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    } 
    
    public static class Book {
        private IntegerProperty id;
        private StringProperty title;

        public Book() {
            this.id = new SimpleIntegerProperty();
            this.title = new SimpleStringProperty();
        }

        //ID 
        public int getId() {
            return id.get();
        }

        public void setId(int id) { 
            this.id.set(id);
        }

        public IntegerProperty idProperty() {
            return id;
        }

        // TITLE
        public String getTitle() {
            return title.get();
        }

        public void setTitle(String name) {
            this.title.set(name);
        }

        public StringProperty titleProperty() {
            return title;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Book other = (Book) obj;
            return Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return title.get();
        }
    }


}

