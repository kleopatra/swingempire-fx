/*
 * Created on 07.04.2015
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
/**
 * https://javafx-jira.kenai.com/browse/RT-40313
1) Expand 'File Explorer' using the arrow next to the text. (single click)
2) You should see your drives on your machine.
3) Using the arrow again, Open one you know has folders (C)
4) The folders are listed but none have arrows indicating there are sub folders.
5) Double click on the TEXT section of the drive you opened.
6) Suddenly all the arrows appear where they should be.

Restart the program.

1) Expand the 'File Explorer'
2) Double click on the drive's TEXT section like step 5 previously.
3) The folders are listing correctly.

usage error: don't use low-level events, instead use semantic events

 */
public class ShowTreeViewProblem_40313 extends Application {
    
    private TreeView<TBLeaf> treeView;
    private TreeItem<TBLeaf> rootNode = new TreeItem<TBLeaf>( new TBLeaf("Root","someImage"));
    private TreeItem<TBLeaf> computerNode = new TreeItem<TBLeaf>(new TBLeaf("File Explorer","someImage"));
    
    
    @Override
    public void start(Stage primaryStage) {
       
        // This code is a folder browser and only interested in folders and not files.
        // It creates A root node with Text 'Root', with a node below that 'File Explorer'
        // The idea is to be able to browes the file system, starting with all the lcoal drives.
        
        // To make sure that each node correclty displays an arrow to show it has child nodes...
        // I work a step ahead (using function reachFurtherFiles) and inspect the sub folders of the sub folders of the currently showing node in the tree.
        // So If we are on C\ and C\ is expanded I will find all folders in C\ and check if any of those folders have sub folders
        // If they do I'll add the sub folders to the child folders of C\.
        // For example if C\ has a temp folder I'll go into the temp folder and add all the sub directories of the temp folder
        // I'll do that for all the other folders under C\.
        
        // As I go deeper the same check will happen deeper and deeper.
        // The problem is that when the user clicks the arrow (only 1 click needed to trigger the arrow)
        // the underlining (native) code which the arrow triggers does not seem to set the state of the TreeItem's (isExpanded) property properly.
        
        // Instead by double clicking the TreeItem cell (on the item text) you get the correct behavior.
        
        // I use a class called tb_leaf so that I can hold the full URL of each folder away from the treeview.
                
        
        
        treeView = new TreeView<TBLeaf>(rootNode);
        treeView.setEditable(true);
        rootNode.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            log("handler of root: " + e);
        });
        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // directory in tree item has been clicked
                if (true) { // click.getClickCount() == 1
                    
                    log("click: " + click);
                    TreeItem<TBLeaf> thisItem = treeView.getSelectionModel().getSelectedItem();
                    if (thisItem == null) return;
                    String keyURL = thisItem.getValue().URL;

                    log("CurrentLocation:"+keyURL);
                    
                    if( !thisItem.isExpanded() )
                    {
                        log("Not expanded");
                        return; // item is not expanded / open
                    }

                    if( thisItem.getValue().getAlreadyOpened() == false) {
                    
                        treeView.getSelectionModel().getSelectedItem().valueProperty().getValue().OpenedAlready = true;
                        log("Opend for first time");
                        List<TreeItem<TBLeaf>> listItemChildrean = new ArrayList<TreeItem<TBLeaf>>();
                        listItemChildrean = thisItem.getChildren();
                       
                        TreeItem<TBLeaf> child;
                        for(int x = 0; x < listItemChildrean.size(); x++){
                            child = listItemChildrean.get(x);
                            //log(child.getValue().toString());
                            reachFurtherFiles(child.valueProperty().getValue().URL,child);
                        }
                        /*for(TreeItem<tb_leaf> child:listItemChildrean)
                        {
                            reachFurtherFiles(child.getValue().toString(),thisItem);
                        }*/
                    }else{
                        log("Opend already");
                    }

                }
            }
        });
        
        // find all drives on the computer
        findDiskItems();
        
        rootNode.getChildren().add(computerNode);
        rootNode.setExpanded(true);
        BorderPane border = new BorderPane();
        StackPane browserPane = new StackPane();
        browserPane.getChildren().add(treeView);
        
        border.setCenter(browserPane);
        
        
        Scene scene = new Scene(border); //, 880, 660);
        //String css = DynamicCSS.class.getResource("/jarcss.css").toExternalForm();
       
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void findDiskItems(){
        File[] paths;
        FileSystemView fsv = FileSystemView.getFileSystemView();

        // returns pathnames for files and directory
        paths = File.listRoots();

        // for each pathname in pathname array
        for(File path:paths)
        {
            // prints file and directory paths
            //log("Drive Name: "+path);
            //log("Description: "+fsv.getSystemTypeDescription(path));
            String driveIcon = "";
            switch(fsv.getSystemTypeDescription(path)){
                case "Local Disk":{
                     driveIcon = "src/img/browser/disk.png";
                };break;
                case "CD Drive":{
                    driveIcon = "src/img/browser/cd.png";
                };break;
                case "Removable Disk":{
                    driveIcon = "src/img/browser/usb.png";
                };break;
            }
            TreeItem<TBLeaf> folder =
            new TreeItem<TBLeaf>(new TBLeaf(path.toString(),"someImage",false));
            
            computerNode.getChildren().add(folder);
            
            reachFurtherFiles(path.toString(), folder);
        }
    }
    
    private void reachFurtherFiles(String path, TreeItem<TBLeaf> folder){
        
        //log("reachFurtherFiles:" + path);
        
        File dir = new File(path);
        File[] listOfFiles = dir.listFiles();

        String fileIcon = "";
        String fullPath = "";
        

        try {
            for (int i = 0; i < listOfFiles.length; i++) {
                if( path.charAt(path.length()-1) == '\\') {
                    fullPath = path + listOfFiles[i].getName();
                }else {
                    fullPath = path + "\\" + listOfFiles[i].getName();
                }
                
                if (listOfFiles[i].isFile()) {
                    /*if(TB.MP3Ext(listOfFiles[i].getName()))
                    {
                        TreeItem<tb_leaf> ti =
                            new TreeItem<tb_leaf>(path.toString(), new ImageView(TB.getFullPath(fileIcon)));
                        folder.getChildren().add(ti);
                    }*/
                } else if (listOfFiles[i].isDirectory()) {
                    
                    TreeItem<TBLeaf> ti =
                            new TreeItem<TBLeaf>(new TBLeaf(fullPath,"src/img/browser/folder.png"));
                    
                    log("Adding Directory " + fullPath);
                    folder.getChildren().add(ti);
                }

            }
        }catch(Exception e){
            log(e.toString());
        }
    }
    
    private class TBLeaf extends TreeItem {
        public String URL;
        public String Text;
        public String Icon;
        public Boolean OpenedAlready = false;

         // could add functionality to change icon /open/closed/
        public TBLeaf(String url,String iconLocation, Boolean opened){
            this.URL = url;
            this.OpenedAlready = opened;
            this.Text = createTextValue(url);
            this.setValue(this.Text);
            
        }
        
        public TBLeaf( String url, String iconLocation){
            this.URL = url;
            this.Text = createTextValue(url);
            this.setValue(this.Text);
            this.Icon = iconLocation;
            
        }
        private String createTextValue(String v) {
            String text;
            if( v.lastIndexOf("\\")>-1 && v.lastIndexOf("\\") != (v.length()-1) ) {
                String s = v.substring(v.lastIndexOf("\\") + 1);
                text = s;
            }else{
                text = v;
            }
            return text;
        }
        public void setAlreadyOpened(Boolean state){
            this.OpenedAlready = state;
        }
        public Boolean getAlreadyOpened() {
            return this.OpenedAlready;
        }
        @Override public String toString() {
            return this.getValue().toString();
            // Or return this.Text
            
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    private void log(String m){
        System.out.println(">>>:" + m);
    }
}
