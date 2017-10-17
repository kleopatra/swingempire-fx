/*
 * Created on 17.10.2017
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Set;

import javafx.application.Application;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;
import javafx.stage.Stage;

/**
 * Removed selected tab gets reselected
 * 
 * https://stackoverflow.com/q/46776086/203657
 * bug: 
 * https://bugs.openjdk.java.net/browse/JDK-8189424
 * 
 * answered/analysed on SO and added reference to bug report
 * 
 * @author Jeanette Winzenburg, Berlin
 */
    public class TabPaneRemoveSelected extends Application {
    
        public static void main(String[] arg) {
            launch(arg);
        }
    
        public static class MyTabSkin extends TabPaneSkin {
    
            public MyTabSkin(TabPane pane) {
                super(pane);
                pane.getTabs().addListener(this::tabsChanged);
            }
            
            protected void tabsChanged(Change<? extends Tab> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        // lookup all TabHeaderSkins
                        Set<Node> tabHeaders = getSkinnable().lookupAll(".tab");
                        tabHeaders.stream()
                            .filter(p -> p instanceof Parent)
                            .map(p -> (Parent) p)
                            .forEach(p -> {
                                // all children removed indicates being in the process
                                // of being removed
                                if (p.getChildrenUnmodifiable().size() == 0) {
                                    // complete removeListeners
                                    p.setOnMousePressed(null);
                                }
                            }
                        );
                        
                    }
                }
            }
            
        }
        
        @Override
        public void start(Stage primaryStage) throws Exception {
            TabPane tabPane = new TabPane() {
    
                @Override
                protected Skin<?> createDefaultSkin() {
                    return new MyTabSkin(this);
                }
                
            };
            tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
                System.out.println("Tab change: " + oldTab + "/" + newTab );
            });
            Tab tab = new Tab("Test tab");
            Tab second = new Tab("second");
            installHandler(tabPane, tab, second);
            installHandler(tabPane, second);
            System.out.println("Adding tab");
            tabPane.getTabs().addAll(tab, second);
            Group root = new Group(tabPane);
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        
        protected void installHandler(TabPane tabPane, Tab... tab) {
            for (Tab tab2 : tab) {
                tab2.setOnCloseRequest((event) -> {
                    System.out.println("Removing tab");
                    event.consume();
                    //I need to remove tab manually
                    tabPane.getTabs().remove(tab2);
                });
                
            }
        }
    
    }

