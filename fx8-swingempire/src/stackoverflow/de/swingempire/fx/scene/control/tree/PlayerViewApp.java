/*
 * Created on 26.10.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.io.IOException;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52992489/203657
 * update tree when attributes change
 * 
 * Here: bidi-bind treeItem's value prop to attribute prop.
 * tbd: cleanup when treeItem is removed
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class PlayerViewApp extends Application {
    private Stage mainStage;

    @FXML
    private AnchorPane mainView;

    @Override
    public void start(Stage mainStage) {
        this.mainStage = mainStage;
        PlayerList.playerListListener();
        this.showMainLayout();
    }

    /**
     * shows the main screen
     */
    public void showMainLayout() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("playerView.fxml"));
        try {
            mainView = (AnchorPane) loader.load();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Scene scene = new Scene(mainView);
        mainStage.sizeToScene();
        mainStage.setScene(scene);
        mainStage.show();
        PlayerViewController controller = loader.getController();
        controller.setMainApp(this);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Player {
        private StringProperty str1 = new SimpleStringProperty();

        private StringProperty str2 = new SimpleStringProperty();

        private StringProperty str3 = new SimpleStringProperty();

        public Player() {

        }

        public Player(String str1, String str2, String str3) {
            super();
            this.str1 = new SimpleStringProperty(str1);
            this.str2 = new SimpleStringProperty(str2);
            this.str3 = new SimpleStringProperty(str3);
        }

        public StringProperty str1() {
            return str1;
        }

        public String getStr1() {
            return str1.get();
        }

        public void setStr1(String str1) {
            this.str1.set(str1);
        }

        public StringProperty str2() {
            return str2;
        }

        public String getStr2() {
            return str2.get();
        }

        public void setStr2(String str2) {
            this.str2.set(str2);
        }

        public StringProperty str3() {
            return str3;
        }

        public String getStr3() {
            return str3.get();
        }

        public void setStr3(String str3) {
            this.str3.set(str3);
        }

    }

    public static class PlayerList {

        private final ObservableList<Player> playerList = FXCollections
                .observableArrayList();

        private static PlayerList instance = new PlayerList();

        public static TreeItem<String> rootNode = new TreeItem<String>("Root");

        private PlayerList() {
        }

        public ObservableList<Player> getPlayerList() {
            return playerList;
        }

        public static PlayerList getInstance() {
            return instance;
        }

        public static void playerListListener() {
            // adds a list listener which reloads tree if objects are added or
            // removed
            PlayerList.getInstance().getPlayerList().addListener(
                    (ListChangeListener<? super Player>) new ListChangeListener<Player>() {

                        @SuppressWarnings("unchecked")
                        public void onChanged(Change<? extends Player> change) {
                            // re-sort list
                            // System.out.println("List changed");
                            rootNode.getChildren().clear();// clear tree before
                                                           // reloading on list
                                                           // change

                            for (Player p : PlayerList.getInstance()
                                    .getPlayerList()) {
                                
                                // Parent Node
                                TreeItem<String> playerName = new TreeItem<String>();
                                        playerName.valueProperty().bindBidirectional(p.str1());
                                // Items
                                TreeItem<String> attr1 = new TreeItem<String>();
                                        attr1.valueProperty().bindBidirectional(p.str2());
                                TreeItem<String> attr2 = new TreeItem<String>();
                                        attr2.valueProperty().bindBidirectional(p.str3());

                                playerName.getChildren().addAll(attr1, attr2);
                                playerName.setExpanded(true);
                                rootNode.getChildren().add(playerName);
                                rootNode.setExpanded(true);
                            }

                        }
                    });
        }

    }

}

/*
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import java.lang.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.layout.AnchorPane?>

<AnchorPane fx:id="mainView" prefHeight="400.0" prefWidth="200.0" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1" fx:controller="viewcontroller.PlayerViewController">
   <children>
      <VBox fx:id="vbox" layoutX="14.0" layoutY="14.0" prefHeight="200.0" prefWidth="100.0" AnchorPane.bottomAnchor="0.0" AnchorPane.leftAnchor="0.0" AnchorPane.rightAnchor="0.0" AnchorPane.topAnchor="0.0">
         <children>
            <Button mnemonicParsing="false" onAction="#handleButtonClick" text="Button" />
         </children>
      </VBox>
   </children>
</AnchorPane>

*/