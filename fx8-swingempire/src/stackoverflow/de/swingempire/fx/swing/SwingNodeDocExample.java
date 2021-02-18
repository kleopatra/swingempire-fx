/*
 * Created 18.02.2021
 */
package de.swingempire.fx.swing;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Example from api doc
 * 
 * erratic behavior when creating before adding scene to stage:
 * - swing content only shown after moving mouse over
 * - swing content incorrect layout
 * 
 * after adding scene - not always
 * - swing content shown immediately 
 * - swing content correct layout
 * 
 * after showing stage
 * - mostly correct
 */
public class SwingNodeDocExample extends Application {

    @Override
    public void start(Stage stage) {
        final SwingNode swingNode = new SwingNode();
        // original: create and set before adding scene to stage
        // content only shown after moving mouse over
//        createAndSetSwingContent(swingNode);

        StackPane pane = new StackPane();
        pane.getChildren().add(swingNode);
        // create and set after adding to parent
        // content shown immediately, wrong layout - spurious, though, often same as above
//         createAndSetSwingContent(swingNode);

        stage.setScene(new Scene(pane, 100, 50));
        // create and set after adding scene to stage
        // this simple content is shown correctly (not always? sometimes black rectangle or same as above)
//        createAndSetSwingContent(swingNode);
        stage.show();
        createAndSetSwingContent(swingNode);
    }

    private void createAndSetSwingContent(final SwingNode swingNode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                swingNode.setContent(new JButton("Click me!"));
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}