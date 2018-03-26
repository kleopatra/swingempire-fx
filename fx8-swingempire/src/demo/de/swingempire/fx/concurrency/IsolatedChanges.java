/*
 * Created on 08.10.2017
 *
 */
package de.swingempire.fx.concurrency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
//import javafx.concurrent.Task;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/46629294/203657
 * 
 * Isolate background model from view.
 * @author Jeanette Winzenburg, Berlin
 */
public class IsolatedChanges extends Application {

    public static class Model {
        ObservableList<String> messages;
        
        public ObservableList<String> getMessages() {
            if (messages == null) {
                messages = FXCollections.observableArrayList();
            }
            return messages;
        }
        /**
         * Blocks - must be called on a background thread
         * @param file
         */
        public void upload(String file) {
            getMessages().add("starting upload" + file);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            getMessages().add("done upload: " + file);
            
        }
        
        public void addMessage(String message) {
            getMessages().add(message);
        }
        
        public void clearMessages() {
            getMessages().clear();
        }
    }
    
    public static class ModelManager {
        
        ObservableList<String> files = FXCollections.observableArrayList();
        Model model;
        
        public ModelManager() {
            this.model = new Model();
        }
        
        public void addFile(String file) {
            files.add(file);
        }
        
        public boolean startBackup() {

            Task task = new Task() {
                @Override
                protected Object call() throws Exception {

                    System.out.println("I started");
                    model.clearMessages();

                    for(String location : files){
//                        File localDirPath = new File(location);         
                        model.upload(location);
                    }            
                    files.clear();
                    return null;
                }           
            };      
            new Thread(task).start();

            return true;
        }

        public ObservableList<String> getMessages() {
            return model.getMessages();
        }
    }
    /**
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Parent getContent() {
        ModelManager manager = new ModelManager();
        ObservableList<String> uploading = FXCollections.observableArrayList("one", "two", "three");
        
        ObservableList<String> items = FXCollections.observableArrayList();
        manager.getMessages().addListener((ListChangeListener) c -> {
            
            while (c.next()) {
                if (c.wasAdded()) {
                    Platform.runLater(() ->  
                        items.addAll(c.getFrom(), c.getAddedSubList()));
                } 
                if (c.wasRemoved()) {
                    Platform.runLater(() ->
                         items.removeAll(c.getRemoved()));
                }
            }
        });
        
        
        ListView<String> list = new ListView<>(items);
        Button button = new Button("start");
        button.setOnAction(ev -> {
            uploading.stream().forEach(e -> manager.addFile(e));
            manager.startBackup();
        });
        BorderPane pane = new BorderPane(list);
        pane.setBottom(button);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(getContent());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
