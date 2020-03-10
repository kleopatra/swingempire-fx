/*
 * Created on 10.03.2020
 *
 */
package de.swingempire.fx.graphic;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60616188/203657
 * NPE and property naming (getcStatus can't be resolved by PropertyValueFactory)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PanesClass extends Application {
    ObservableList<Connections> cList = FXCollections.observableArrayList();

    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("all")
    @Override
    public void start(Stage primaryStage) throws Exception {
        NewConnection newConnection     = new NewConnection();
        SplitPane     root              = new SplitPane();
        AnchorPane    first             = new AnchorPane();
        AnchorPane    second            = new AnchorPane();
        TableView     activeConnections = new TableView();
        HBox          buttonBox         = new HBox();
        BorderPane    topBar            = new BorderPane();
        Button        nConnection       = new Button("+");
        Button        deleteConnection  = new Button("X");
        Button        connect           = new Button("Connect");

        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(nConnection, deleteConnection, connect);
        topBar.setTop(buttonBox);

        TableColumn<String, Connections> cNameColoumn = new TableColumn<>("Name");

        cNameColoumn.setCellValueFactory(new PropertyValueFactory<>("cName"));

        TableColumn<String, Connections> cStatusColoumn = new TableColumn<>("Status");

        cStatusColoumn.setCellValueFactory(new PropertyValueFactory<>("cStatus"));
        activeConnections.getColumns().addAll(cNameColoumn, cStatusColoumn);
        activeConnections.setLayoutX(20);
        activeConnections.setLayoutY(40);
        activeConnections.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        activeConnections.setItems(cList);
        first.getChildren().addAll(topBar, activeConnections);
        root.getItems().addAll(first, second);

        Scene sc = new Scene(root, 600, 480);

        primaryStage.setScene(sc);
        primaryStage.show();
        nConnection.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
//                                        cList.addAll(newConnection.getConnection());
                                        newConnection.getConnection(cList);
                                    }
                                });
    }
    
    public static class NewConnection {
        Connections                 connection = null;
        ObservableList<Connections> cList      = FXCollections.observableArrayList();

        public ObservableList<Connections> getConnection(ObservableList<Connections> cList) {
            this.cList = cList;
            Stage    secondaryStage = new Stage();
            VBox     root           = new VBox();
            GridPane cDetails       = new GridPane();
            HBox     actionButtons  = new HBox();
            Button   connect        = new Button("Connect");
            Button   save           = new Button("Save");
            Button   cancel         = new Button("Cancel");

            actionButtons.getChildren().addAll(connect, save, cancel);
            actionButtons.setSpacing(10);

            Label name = new Label("Username : ");

            cDetails.add(name, 0, 0);

            TextField uName = new TextField();

            cDetails.setHgrow(uName, Priority.ALWAYS);
            cDetails.add(uName, 1, 0);

            Label password = new Label("Password : ");

            cDetails.add(password, 0, 1);

            TextField pwd = new TextField();

            cDetails.add(pwd, 1, 1);

            Label urllink = new Label("URL : ");

            cDetails.add(urllink, 0, 2);

            TextField url = new TextField();

            cDetails.add(url, 1, 2);
            cDetails.setVgap(10);
            cDetails.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 1;"
                              + "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: black;");
            root.getChildren().addAll(cDetails, actionButtons);

            Scene sc = new Scene(root, 500, 200);

            secondaryStage.setScene(sc);
            secondaryStage.show();
            save.setOnAction(new EventHandler<ActionEvent>() {
                                 @Override
                                 public void handle(ActionEvent event) {
                                     connection = new Connections();
                                     connection.setCName(uName.getText());
                                     connection.setPwd(pwd.getText());
                                     connection.setUrl(url.getText());
                                     cList.add(connection);
                                     secondaryStage.close();
                                     System.out.println(cList);
                                 }
                             });
            System.out.println(cList);

            return cList;
        }
    }

    public static class Connections {
        private String cName;
        private String cStatus;
        private String url;
        private String pwd;

        public Connections() {}


        public Connections(String cName, String cStatus, String url, String pwd) {
        this.cName = cName;
        this.cStatus = cStatus;
        this.url = url;
        this.pwd = pwd;
        }


        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCName() {
            return cName;
        }

        public String getCStatus() {
            return cStatus;
        }

        public void setCName(String cName) {
            this.cName = cName;
        }

        public void setCStatus(String cStatus) {
            this.cStatus = cStatus;
        }


        public String getPwd() {
            return pwd;
        }


        public void setPwd(String pwd) {
            this.pwd = pwd;
        }


        @Override
        public String toString() {
        return "Connections [cName=" + cName + ", cStatus=" + cStatus + ", url=" + url + ", pwd=" + pwd + "]";
        }



    }

}
