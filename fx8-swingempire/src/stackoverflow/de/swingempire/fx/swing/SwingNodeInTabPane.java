/*
 * Created on 18.02.2021
 *
 */
package de.swingempire.fx.swing;

/*
 * Created 17.02.2021
 */

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/66238682/203657
 * Swing in tab - blank content until button moved over it
 * 
 * answer from Wolfgang:
 * - create tab content in runlater (doesn't help)
 * - setImplicitExit to false (doesn't help)
 * 
 * also: flickers on resize ..
 */
public class SwingNodeInTabPane extends Application {

    private final SwingNode node1 = new SwingNode();
    private final SwingNode node2 = new SwingNode();
    private final SwingNode node3 = new SwingNode();

    @Override
    public void start(Stage primaryStage) {
      try {

//        Platform.setImplicitExit(false);  
        TabPane root = new TabPane();
        root.getTabs().addAll(new Tab("first"), new Tab("second"), new Tab("last"));
        
        
        root.getTabs().get(0).setContent(node1);
        root.getTabs().get(1).setContent(node2);
        root.getTabs().get(2).setContent(node3);
        Scene scene = new Scene(root, 400, 400);
        
//        createScene(root);
//        Platform.runLater(() -> createScene(root));
        primaryStage.setScene(scene);
        primaryStage.show();
        createAndSetSwingContent();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }


    private void createAndSetSwingContent() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JPanel panel1 = new JPanel(new BorderLayout());
                JPanel panel2 = new JPanel(new BorderLayout());
                JPanel panel3 = new JPanel(new BorderLayout());

                JButton btn1 = new JButton("Button 1");
                JButton btn2 = new JButton("Button 2");
                JButton btn3 = new JButton("Button 3");

                panel1.add(btn1);
                panel2.add(btn2);
                panel3.add(btn3);

//          swingNode.setContent(new ButtonHtml());    
                node1.setContent(panel1);
                node2.setContent(panel2);
                node3.setContent(panel3);
            }

        });

    }

    public static void main(String[] args) {
      launch(args);
    }
  }

