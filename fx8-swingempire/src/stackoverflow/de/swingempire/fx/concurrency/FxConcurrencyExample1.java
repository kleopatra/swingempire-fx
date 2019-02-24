/*
 * Created on 21.02.2019
 *
 */
package de.swingempire.fx.concurrency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Different behaviour of label.setText vs. textArea.appendText: the latter
 * doesn't throw right away if called off the fx app thread. Accidental
 * and must be on fx anyway - just want to see who's throwing.
 * 
 * https://stackoverflow.com/q/54770062/203657
 * 
 * Technically, the difference is that label.setText actually changes the scenegraph
 * (Labeled is a composed control which adds/removed children on update) and the
 * scenegraph actively protects itself against threading violations 
 * parent.onProposedChanged is messaged on notifications of the child list which 
 * then calls checkFxUserThread
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FxConcurrencyExample1  extends Application
{
    // Create the TextArea
    TextArea textArea = new TextArea();

    // Create the Label
    Label statusLabel = new Label("Not Started...");

    // Create the Buttons
    Button startButton = new Button("Start");
    Button exitButton = new Button("Exit");

    public static void main(String[] args) 
    {
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) 
    {
        // Create the Event-Handlers for the Buttons
        startButton.setOnAction(new EventHandler <ActionEvent>() 
        {
            public void handle(ActionEvent event) 
            {
                startTask();
                // doesn't matter - caret doesn't seem to trigger a thread violation
//                textArea.requestFocus();
            }
        });

        exitButton.setOnAction(new EventHandler <ActionEvent>() 
        {
            public void handle(ActionEvent event) 
            {
                stage.close();
            }
        });

        // Create the ButtonBox     
        HBox buttonBox = new HBox(5, startButton, exitButton);

        // Create the VBox
        VBox root = new VBox(10, statusLabel, buttonBox, textArea);

        // Set the Style-properties of the VBox
        root.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: blue;");

        // Create the Scene
        Scene scene = new Scene(root,400,300);
        // Add the scene to the Stage
        stage.setScene(scene);
        // Set the title of the Stage
        stage.setTitle("A simple Concurrency Example");
        // Display the Stage
        stage.show();       
    }

    public void startTask() 
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask();
            }
        };

        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
    }

    public void runTask() 
    {
        for(int i = 1; i <= 10; i++) 
        {
            try
            {
                String status = "Processing " + i + " of " + 10;
                System.out.println(status);
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        statusLabel.setText(status);
                    }
                });
                textArea.appendText(status+"\n");
                Thread.sleep(1000);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   
}

