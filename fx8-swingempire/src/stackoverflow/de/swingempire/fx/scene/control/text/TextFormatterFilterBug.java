/*
 * Created on 30.08.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.function.UnaryOperator; 

import javafx.application.Application; 
import javafx.concurrent.Service; 
import javafx.concurrent.Task; 
import javafx.scene.Scene; 
import javafx.scene.control.TextField; 
import javafx.scene.control.TextFormatter; 
import javafx.scene.layout.StackPane; 
import javafx.scene.text.Font; 
import javafx.stage.Stage; 

/**
 * https://bugs.openjdk.java.net/browse/JDK-8210145
 * TextFormatter filter getting bypassed
 * 
 * requirement: caret must not be inside the suffix
 * all okay if start length > 0
 * if start length = 0, 
 *      caret can be positioned at 1 (that is after first char of suffix) by mouse 
 *      not by keyboard
 *      
 * values are correct but painted at wrong pos?     
 */
public class TextFormatterFilterBug extends Application { 

    public TextField txtFldObj; 
    public Service serviceObj; 

    public TextFormatterFilterBug() { 

        /*serviceObj = new Service() { 
            @Override 
            protected Task createTask() { 
                return new Task() { 
                    @Override 
                    protected Void call() throws Exception { 
                        System.out.println("Thread started, Waiting"); 
                        Thread.sleep(2000); 
                        System.out.println("Thread started, Executing. txtFldObj.getCaretPosition():"+txtFldObj.getCaretPosition()+", txtFldObj.getAnchor():"+txtFldObj.getAnchor()); 
                        //txtFldObj.end(); 
                        return null; 
                    } 
                }; 
            } 
        };*/ 
    } 

    private TextField createFixedPrefixTextField(String suffix) { 
        txtFldObj = new TextField(suffix); 
        txtFldObj.setFont(new Font("Arial", 30)); 

        UnaryOperator<TextFormatter.Change> filter = c -> { 
            if (c.getCaretPosition() > (c.getControlNewText().length() - suffix.length()) 
                    || c.getAnchor() > (c.getControlNewText().length() - suffix.length())) { 
                System.err.println("Returning Null. c.getCaretPosition():" + c.getCaretPosition()+
                        ",c.getAnchor():" + c.getAnchor()+"....."+"\n   controlNewTextLength-SuffixLength:" + (c.getControlNewText().length() - suffix.length())); 
                
                System.err.println("Returning Null. c.getControlCaretPosition():" + c.getControlCaretPosition()+
                        ",c.getControlAnchor():" + c.getControlAnchor()+"....."+"\n   controlNewTextLength-SuffixLength:" + (c.getControlNewText().length() - suffix.length())); 

                /*if (!serviceObj.isRunning()) { 
                    serviceObj.reset(); 
                    serviceObj.start(); 
                }*/ 
                return null; 
            } else { 
                System.err.println("valid. c.getCaretPosition():" + c.getCaretPosition()+",c.getAnchor():" + c.getAnchor()+"....."+"controlNewTextLength-SuffixLength:" + (c.getControlNewText().length() - suffix.length())); 
//                System.out.println("Valid"); 
                return c; 
            } 
        }; 

        txtFldObj.setTextFormatter(new TextFormatter<>(filter)); 
        txtFldObj.positionCaret(0); 

        return txtFldObj; 
    } 

    @Override 
    public void start(Stage primaryStage) { 
        txtFldObj = createFixedPrefixTextField("PCS"); 
        StackPane root = new StackPane(txtFldObj); 
        Scene scene = new Scene(root, 300, 40); 
        primaryStage.setScene(scene); 
        primaryStage.show(); 
    } 

    public static void main(String[] args) { 
        launch(args); 
    } 
} 