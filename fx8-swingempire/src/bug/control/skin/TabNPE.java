/*
 * Created on 16.03.2018
 *
 */
package control.skin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/49316378/203657
 * npe when expanding/collapsing children - cant reproduce
 * had been scenicView running in background of OP, somehow
 * interfered
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TabNPE extends Application {
    Tab treeTab;
    @Override
    public void start(Stage primaryStage) throws Exception {
        TabPane pane = new TabPane();
        Scene scene = new Scene(pane,800,600);
        treeTab = new Tab("Tree");
        pane.getTabs().addAll(treeTab);
        primaryStage.setTitle("F(X)yz - Collision Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        makeTree();
    }
    private void makeTree(){
        TreeItem<String> treeItemRoot = new TreeItem<> ("Root");         
        TreeItem<String> nodeItemA = new TreeItem<>("Item A");
        TreeItem<String> nodeItemB = new TreeItem<>("Item B");
        TreeItem<String> nodeItemC = new TreeItem<>("Item C");
        treeItemRoot.getChildren().addAll(nodeItemA, nodeItemB, nodeItemC);        
        TreeItem<String> nodeItemCA = new TreeItem<>("Item CA");
        TreeItem<String> nodeItemCB = new TreeItem<>("Item CB");
        nodeItemC.getChildren().addAll(nodeItemCA, nodeItemCB);         
        TreeItem<String> nodeItemA1 = new TreeItem<>("Item A1");
        TreeItem<String> nodeItemA2 = new TreeItem<>("Item A2");
        TreeItem<String> nodeItemA3 = new TreeItem<>("Item A3");
        nodeItemA.getChildren().addAll(nodeItemA1, nodeItemA2, nodeItemA3);  
        treeItemRoot.setExpanded(true);
        TreeView<String> treeView = new TreeView<>(treeItemRoot);
        treeView.getSelectionModel().clearAndSelect(1);
        treeTab.setContent(treeView);        
    }        
     public static void main(String[] args) {
        launch(args);
    }
}

