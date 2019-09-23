/*
 * Created on 23.09.2019
 *
 */
package de.swingempire.fx.scene.control;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Refresh bug with combo/list in tab
 * https://stackoverflow.com/q/58044487/203657
 * 
 * - select in combo: new value showing
 * - press add: value showing in list
 * - select second tab
 * - select first tab again
 * - select another value in combo: not shown
 * - press add: not shown in list
 * - press cancel (just to refresh): values are updated
 * 
 * OP tracked into 3 combined triggers (removing any of this will make the update work
 * as expected) 
 * - tabPane closing policy must be unavailable
 * - next button must be un/managed on selecting tab
 * - containing layout must be nested
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboManagedBug extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    private int count;
    
    @Override
    public void start(Stage primaryStage) {
        // add combo box
        ComboBox<String> combo = new ComboBox<>();
        combo.setPromptText("Choose a value...");
        combo.getItems().setAll("1", "2", "3");
        
        
//        combo.valueProperty().addListener((src, ov, nv) -> System.out.println("value changed: " + nv));
        // add list view
//        ListView<Label> list = new ListView<>();
        ListView<String> list = new ListView<>();

        
        // add "add" button
        Button add = new Button("Add");
        add.setOnAction(e -> list.getItems()
//                .add("" + count++));
                .add(combo.getSelectionModel().getSelectedItem()));
//                .add(new Label(combo.getSelectionModel().getSelectedItem())));

        // add tab pane
//        Tab tab1 = new Tab("First", new VBox(add, list));
        Tab tab1 = new Tab("First", new VBox(combo, add, list));
        Tab tab2 = new Tab("Second"); //, new Button("dummy"));
        TabPane tabs = new TabPane(tab1, tab2);
        tabs.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE); // important!

        // add "next" and "cancel" buttons at bottom
        Button next = new Button("Next");
        Button cancel = new Button("Cancel (Triggers refresh)");
        // must be at least 2 buttons (one of them unmanaged ...)
        HBox buttons = new HBox(next, cancel);
//        FlowPane buttons = new FlowPane(next, cancel);

        // install tab listener (doesn't matter if before or after the skin)
        tabs.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
            // at this point, the skin has not yet reacted, content/parent not visible
            System.out.println("visible? " + tab1.getContent().getParent().isVisible());  
            // intention is to show next button only on first tab
            boolean firstTab = c == tab1;
            // listen to index/item - no change
//            boolean firstTab = c.intValue() == 0;
            next.setVisible(firstTab);
            next.setManaged(firstTab); // important!
            // actually remove/add doesn't help
//                    if (!firstTab) {
//                        buttons.getChildren().remove(next);
//                    } else {
//                        buttons.getChildren().add(0, next);
//                    }
            // no effect
            //tabs.layout();
            // no effect
//            Platform.runLater(() -> {
//                // at this point, the skin has reacted, content/parent is visible
//                System.out.println("visible later? " + tab1.getContent().getParent().isVisible());  
//                tabs.layout();
//            });
        });
        
        // show - nested layout needed
        VBox center = new VBox(tabs, buttons);
//        BorderPane root = new BorderPane(center);
        VBox root = new VBox(center); // important!
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("ComboBox/ListView Rendering Bug Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setX(100);
//        fixTabRendering(tabs);
        Platform.runLater(() -> fixTabRendering(tabs));
    }
    
    // fake unavailable - hack by OP, doesn't seem to work? Now the list
    // is never updated
    public static void fixTabRendering(TabPane tabs) {
        if (tabs.getTabClosingPolicy() != TabClosingPolicy.UNAVAILABLE) return;
        tabs.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
        for (Node node : tabs.lookupAll(".tab-close-button")) {
            // hide "close" button to imitate TabClosingPolicy.UNAVAILABLE
            node.setStyle("-fx-background-color:transparent;-fx-shape:null;-fx-pref-width:0.001");
        }
    }


}

