/*
 * Created on 24.09.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58059383/203657
 * nested properties and progressbar cell
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ProgressCellForTaks extends Application {
    public class MyBean {

        private SimpleStringProperty name;
        public MyBean() {
            name = new SimpleStringProperty("--");
        }
        public SimpleStringProperty nameProperty() {
            return name;
        }
        public void setName(String name) {
            this.name.setValue(name);
        }
    }

   public class MyTask extends Task<Void>{

        @Override
        protected Void call() throws Exception {
            // Set the total number of steps in our process
            double steps = 1000;

            // Simulate a long running task
            for (int i = 0; i < steps; i++) {

                Thread.sleep(10); // Pause briefly

                // Update our progress and message properties
                updateProgress(i, steps);
                updateMessage(String.valueOf(i));
            }       return null;
        }

    }

   public class MyWrapper {

       private SimpleObjectProperty<MyBean> myBean;
       private SimpleObjectProperty<MyTask> myTask;
       public MyWrapper(String name) {
           myBean = new SimpleObjectProperty<MyBean>();
           myBean.setValue(new MyBean());
           myBean.getValue().setName(name);
           myTask = new SimpleObjectProperty<MyTask>();
           myTask.setValue(new MyTask());

       }
       public MyBean getMyBean() {
           return myBean.getValue();
       }
       public MyTask getMyTask() {
           return myTask.getValue();
       }

   }


    private Parent createContent() {
        // table setup
        MyWrapper w1 = new MyWrapper("qqqqqqq");
        MyWrapper w2 = new MyWrapper("wwwwww");
        MyWrapper w3 = new MyWrapper("eeeeeee");
        ObservableList<MyWrapper> obsList = FXCollections.observableArrayList(w1, w2, w3);
        TableView<MyWrapper> table = new TableView<>(obsList);
        TableColumn<MyWrapper, String> nameColumn = new TableColumn<>("Name");
        TableColumn<MyWrapper, Double> progressColumn = new TableColumn<>("Progress");
        table.getColumns().addAll(nameColumn, progressColumn);
        
        // column setup
        nameColumn.setCellValueFactory(cc -> cc.getValue().getMyBean().nameProperty());
        progressColumn.setCellValueFactory(cc -> cc.getValue().getMyTask().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        new Thread(w1.getMyTask()).start();
        
        BorderPane content = new BorderPane(table);
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
            .getLogger(ProgressCellForTaks.class.getName());

}
