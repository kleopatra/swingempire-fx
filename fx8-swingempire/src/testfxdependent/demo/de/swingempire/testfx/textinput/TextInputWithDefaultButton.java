/*
 * Created on 18.07.2018
 *
 */
package de.swingempire.testfx.textinput;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * https://stackoverflow.com/q/51388408/203657
 * Default button always triggered, even when consuming the enter.
 * reported by OP https://bugs.openjdk.java.net/browse/JDK-8207759
 * 
 * What to expect? 
 * - node.setEventHandler(xxEvent) is doc'ed to be called as last handler in the chain
 * - all setOnXX delegate to that
 * - where in the dispatch chain do accelerators are triggered
 * 
 * <hr>
 * 
 * yet another quirk: 
 * add handler before showing -> consumed as expected, default not triggered
 *    this is caused by ...?
 *    custom handler added before InputMap -> consume in custom prevents
 *       calling fire (and forwardToParent)
 * add handler after showing -> default triggered before handler, consume has no effect
 *    custom handler is added after InputMap -> calls fire and forwardToParent
 * 
 * Maybe problem with InputMap? registration sequence shouldn't make a difference?
 * backing out handle if consumed - are siblings still notified?
 * 
 * InputMap: 
 * - adds/removes wrapping eventHandler to node on adding/removing mappings
 * - the wrapper is a delegate to handle which looks up the actual handler
 *     from the available mappings
 * - handle backs out if given event is null (how can it?) or consumed   
 * - need to check if this consuming might violate contract (all sibling
 *   handlers must see the event, whether it is consumed or not)
 * 
 * @author Jeanette Winzenburg, Berlin
 * @see de.swingempire.fx.scene.control.text.TextFieldActionHandler
 */
public class TextInputWithDefaultButton extends Application {

    // quick check: eventHandler added before/after showing 
    // behaves as expected/not as expected
    private boolean registerBeforeShowing = true;
    private boolean registerHandler = true;
    
    private boolean registerSingletonHandler;// = true;
    
    private EventStackRecorder recorder;
    
    public static void main(String[] args) {
        launch(args);
    }

    
    @Override
    public void start(Stage primaryStage) {
        
        TextFieldSkin skin;
        // Simple UI
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        // test infrastructure
        recorder = new EventStackRecorder(20);
        
        // TextField
        TextField textField = new TextField();

//        textField.setOnAction(e -> {});
//        textField.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
        // Capture the [ENTER] key
        if (registerSingletonHandler && registerBeforeShowing) {
            System.out.println("setOnPressed before showing");
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                    System.out.println( event.getCode() + " -> consumed in singleton handler");
                    recorder.record(event);
                    event.consume();
                }
            });
        }
//        
        // adding a consuming handler before showing: consumed as expected
        if (registerHandler && registerBeforeShowing) {
            System.out.println("add before showing");
            textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                    System.out.println( event.getCode() + " -> consumed in added handler");
                    event.consume();
                    recorder.record(event);
                }
            });
        }

//        textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.ENTER) {
//                System.out.println("-> Enter in add");
//                event.consume();
//            }
//            
//        });
        
        // Buttons
        Button btnCancel = new Button("Cancel");
        btnCancel.setCancelButton(true);
        btnCancel.setOnAction(e -> {
            System.out.println("-> Cancel");
        });

        Button btnSave = new Button("Save");
        btnSave.setDefaultButton(true);
        btnSave.setOnAction(e -> {
            System.out.println("-> Save");
            recorder.record(e);
        });

        Button log = new Button("event recorder");
        log.setOnAction(e -> {
            recorder.logAll();
        });
        
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(log, btnCancel, btnSave);

        root.getChildren().addAll(textField, buttonBar);

        Scene scene = new Scene(root);
        scene.getAccelerators().put(KeyCombination.keyCombination("ENTER"), () -> {
            System.out.println("-> accelerator");
        });
        primaryStage.setScene(scene);
        primaryStage.setTitle("Consume Event");
        primaryStage.show();
        
        if (registerHandler && !registerBeforeShowing) {
            System.out.println("add after showing");
            // adding a consuming handler after showing: not consumed 
            textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                    System.out.println( event.getCode() + " -> consumed in added handler, after showing");
                    recorder.record(event);
                    event.consume();
                }
            });
        }
        
        if (registerSingletonHandler && !registerBeforeShowing) {
            System.out.println("setOnPressed after showing");
            textField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                    System.out.println( event.getCode() + " -> consumed in singleton handler, after showing");
                    event.consume();
                    recorder.record(event);
                }
            });
        }


    }
}

