/*
 * Created on 18.11.2019
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableView.TreeTableViewFocusModel;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58904792/203657
 * disable selection for some items
 * 
 * Digging into failing tests, namely around expanding/collapsing/children
 * modifications.
 * 
 * Might be a version problem though, OP
 * works against fx-8.
 */
public class TreeTableViewFilterSelectionApp extends Application {

    private Parent createContent() {
        setup();
        // extended
        sm.setCellSelectionEnabled(false);
        sm.setSelectionMode(SelectionMode.SINGLE);
        
        treeTableView.setRoot(myCompanyRootNode);
//        myCompanyRootNode.setExpanded(true);
//        salesDepartment.setExpanded(true);
//        itSupport.setExpanded(true);
//        sm.select(8);                   // itSupport
        
        
        // plain
        smP.setCellSelectionEnabled(false);
        smP.setSelectionMode(SelectionMode.SINGLE);

        plain.setRoot(myCompanyRootNodeP);
//        myCompanyRootNodeP.setExpanded(true);
//        salesDepartmentP.setExpanded(true);
//        itSupportP.setExpanded(true);
//        smP.select(8);                   // itSupport

        
        Button button = new Button("selectedItem");
        button.setOnAction(e -> {
            System.out.println("plain/ext: " + plain.getSelectionModel().getSelectedItem() + " / " 
                    + treeTableView.getSelectionModel().getSelectedItem());
        });
        
        HBox buttons = new HBox(10, button);
        HBox trees = new HBox(10, treeTableView, plain);
        BorderPane content = new BorderPane(trees);
        content.setBottom(buttons);
        return content;
    }

//----------- Data setup
    private TreeTableView<String> treeTableView;
    
    private TreeTableView.TreeTableViewSelectionModel sm;
    private TreeTableViewFocusModel<String> fm;

    private TreeTableView<String> plain;
    private TreeTableView.TreeTableViewSelectionModel smP;
    
//    
//    // sample data #1
//    private TreeItem<String> root;
//    private TreeItem<String> child1;
//    private TreeItem<String> child2;
//    private TreeItem<String> child3;

    // sample data #1
    private TreeItem<String> myCompanyRootNode;
        private TreeItem<String> salesDepartment;
            private TreeItem<String> ethanWilliams;
            private TreeItem<String> emmaJones;
            private TreeItem<String> michaelBrown;
            private TreeItem<String> annaBlack;
            private TreeItem<String> rodgerYork;
            private TreeItem<String> susanCollins;
        
        private TreeItem<String> itSupport;
            private TreeItem<String> mikeGraham;
            private TreeItem<String> judyMayer;
            private TreeItem<String> gregorySmith;
    
    // sample data #1
    private TreeItem<String> myCompanyRootNodeP;
        private TreeItem<String> salesDepartmentP;
            private TreeItem<String> ethanWilliamsP;
            private TreeItem<String> emmaJonesP;
            private TreeItem<String> michaelBrownP;
            private TreeItem<String> annaBlackP;
            private TreeItem<String> rodgerYorkP;
            private TreeItem<String> susanCollinsP;

        private TreeItem<String> itSupportP;
            private TreeItem<String> mikeGrahamP;
            private TreeItem<String> judyMayerP;
            private TreeItem<String> gregorySmithP;


    private void setup() {
        // plain
        plain = new TreeTableView<String>();
        TreeTableColumn<String, String> firstCol = new TreeTableColumn<>("Item");
        firstCol.setCellValueFactory(cc -> new SimpleStringProperty(cc.getValue().getValue() ));
        plain.getColumns().addAll(firstCol);
        smP = plain.getSelectionModel();
        
        // extended
        treeTableView = new TreeTableView<String>();
        TreeTableColumn<String, String> col = new TreeTableColumn<>("Item");
        col.setCellValueFactory(cc -> new SimpleStringProperty(cc.getValue().getValue() ));
        treeTableView.getColumns().addAll(col);
        sm = treeTableView.getSelectionModel();
        treeTableView.setSelectionModel(new TreeTableViewFilteredSelectionModel(
                treeTableView, sm, item -> true));
        sm = treeTableView.getSelectionModel();
        fm = treeTableView.getFocusModel();
        
        

        // build sample data #2, even though it may not be used...
        myCompanyRootNode = new TreeItem<String>("MyCompany Human Resources");
        salesDepartment = new TreeItem<String>("Sales Department");
        ethanWilliams = new TreeItem<String>("Ethan Williams");
        emmaJones = new TreeItem<String>("Emma Jones");
        michaelBrown = new TreeItem<String>("Michael Brown");
        annaBlack = new TreeItem<String>("Anna Black");
        rodgerYork = new TreeItem<String>("Rodger York");
        susanCollins = new TreeItem<String>("Susan Collins");
        
        itSupport = new TreeItem<String>("IT Support");
        mikeGraham = new TreeItem<String>("Mike Graham");
        judyMayer = new TreeItem<String>("Judy Mayer");
        gregorySmith = new TreeItem<String>("Gregory Smith");
        
        myCompanyRootNode.getChildren().setAll(
                salesDepartment,
                itSupport
                );
        salesDepartment.getChildren().setAll(
                ethanWilliams,
                emmaJones,
                michaelBrown,
                annaBlack,
                rodgerYork,
                susanCollins
                );
        itSupport.getChildren().setAll(
                mikeGraham,
                judyMayer,
                gregorySmith
                );
        
        // build sample data #2, even though it may not be used...
        myCompanyRootNodeP = new TreeItem<String>("MyCompany Human Resources");
        salesDepartmentP = new TreeItem<String>("Sales Department");
            ethanWilliamsP = new TreeItem<String>("Ethan Williams");
            emmaJonesP = new TreeItem<String>("Emma Jones");
            michaelBrownP = new TreeItem<String>("Michael Brown");
            annaBlackP = new TreeItem<String>("Anna Black");
            rodgerYorkP = new TreeItem<String>("Rodger York");
            susanCollinsP = new TreeItem<String>("Susan Collins");

        itSupportP = new TreeItem<String>("IT Support");
            mikeGrahamP = new TreeItem<String>("Mike Graham");
            judyMayerP = new TreeItem<String>("Judy Mayer");
            gregorySmithP = new TreeItem<String>("Gregory Smith");

        myCompanyRootNodeP.getChildren().setAll(
            salesDepartmentP,
            itSupportP
        );
        salesDepartmentP.getChildren().setAll(
            ethanWilliamsP,
            emmaJonesP,
            michaelBrownP,
            annaBlackP,
            rodgerYorkP,
            susanCollinsP
        );
        itSupportP.getChildren().setAll(
            mikeGrahamP,
            judyMayerP,
            gregorySmithP
        );
        
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
            .getLogger(TreeTableViewFilterSelectionApp.class.getName());

}
