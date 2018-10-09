/*
 * Created on 09.10.2018
 *
 */
package de.swingempire.fx.webview;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Open link in browser, oldish answer on SO
 * learn-item-of-the-day ;)
 * 
 * https://stackoverflow.com/a/24450989/203657
 * 
 * Bad though: loaded the hyperlink (==view) as data, vs. the urls 
 * with a custom list cell. Trying the latter here.
 * 
 * Note: the hyperlink is just a button with a visited property!
 * On re-use that visited must be handled somewhere else, where?
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class Hypercell extends Application {


    
    final ListView<URL> listView = new ListView<>();
    
    @Override
    public void start(Stage primaryStage) throws MalformedURLException {

        ObservableList<URL> urls = FXCollections.observableArrayList(
              new URL("http://blog.professional-webworkx.de"),
              new URL("http://www.stackoverflow.com")
                );

        listView.getItems().addAll(urls);
        listView.setCellFactory(c -> {
            ListCell<URL> cell = new ListCell<>() {
                private Hyperlink hyperlink;
                
                {
                    hyperlink = new Hyperlink();
                    hyperlink.setOnAction(e -> {
                        if (getItem() != null) {
                            getHostServices().showDocument(getItem().toString());
                        }
                    });
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }

                @Override
                protected void updateItem(URL item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty) {
                        hyperlink.setText(item.toString());
                        setGraphic(hyperlink);
                    } else {
                        setGraphic(null);
                    }
                }
                
            };
            return cell;
        });
        
        AnchorPane pane = new AnchorPane();
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        final TextField urlField = new TextField();
        Button b = new Button("Add Links");
        hBox.getChildren().addAll(b, urlField);

        b.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                try {
                    addLink(urlField.getText().trim());
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                urlField.clear();
            }
        });
        vBox.getChildren().add(hBox);
        vBox.getChildren().add(listView);
        pane.getChildren().add(vBox);
        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void addLink(final String url) throws MalformedURLException {
//        final Hyperlink link = new Hyperlink(url);
//        link.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent t) {
//                getHostServices().showDocument(link.getText());
//                //openBrowser(link.getText());
//            }
//
//        });
        listView.getItems().add(new URL(url));
    }

    private void openBrowser(final String url) {
        getHostServices().showDocument(url);
    }
}

