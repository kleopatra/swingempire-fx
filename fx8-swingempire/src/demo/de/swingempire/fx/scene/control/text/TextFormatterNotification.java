/*
 * Created on 04.09.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.function.UnaryOperator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; 

/**
 * https://bugs.openjdk.java.net/browse/JDK-8210297
 * on focus-out, all filters of the formatters are notified
 * expected: only the one which might have been changed if any
 * 
 * to reproduce, click in any and tab out
 * expected:  no notification at all (nothing changed)
 * actual: all are notified
 * 
 * type a char: the target filter gets notified
 * type enter: no notification( nothing changed compared to last notificaion)
 * tab out: all filters notified
 * 
 * On tabbing:
 * - all are selection changes
 * - might be okay for source (caret removed) and target (caret added) field 
 *   but not for the third
 * 
 * Looks like done in a focusListener installed in TextFieldBehavior's constructor
 * - selects all text if new focusOwner is its field (okay)
 * - unselects all text unconditionally (not okay, should do only if old focusOwner is its field)
 * - unselect (and the notification) even happens if focus moved between unrelated controls, f.i.buttons)
 * 
 * fix might be to conditionally unselect
 * 
 * commented bug
 * 
 * still open: why do we get two notifications for a single move?
 * happens in textInputControl.backwards:
 * 
 * - first the caret is moved, the exact pos depends on whether or not we had a selection
 *   via selectRange(pos, pos) -> sets new anchor/caret to pos each and triggers a notification
 * - then deselect is called (always) which calles selectRange(getCaretPos, getCaretPos)
 *   actually doing the same as the    
 * - not in home/end  
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFormatterNotification extends Application { 

    public TextField txtFldObjA; 
    public TextField txtFldObjB; 
    public TextField txtFldObjC; 


    private TextField createFixedPrefixTextField(String prefix, String id) { 
        TextField txtFldObj = new TextField(prefix) {

            /**
             * This removes to double notification on back/forward
             */
            @Override
            public void deselect() {
//                if (getAnchor() == getCaretPosition()) return;
                super.deselect();
            }

            /**
             * Overridden to return if anchor and caret are already at the given 
             * positions.
             * 
             * Removes both double notification on backward/forward and
             * notification on unrelated focusOwner changes.
             */
            @Override
            public void selectRange(int anchor, int caretPosition) {
                if (getAnchor() == anchor && getCaretPosition() == caretPosition) return;
                super.selectRange(anchor, caretPosition);
            }
            
            
            
        }; 
        txtFldObj.setId(id); 

        UnaryOperator<TextFormatter.Change> filter = c -> { 
            System.out.println(id+" Has recieved change notification: \n   " + c); 
//            new RuntimeException("who is calling? \n ").printStackTrace();
            return c; 
        }; 

        txtFldObj.setTextFormatter(new TextFormatter<>(filter)); 
        return txtFldObj; 
    } 

    @Override 
    public void start(Stage primaryStage) { 
        txtFldObjA = createFixedPrefixTextField("$ ","Field 1"); 
        txtFldObjB = createFixedPrefixTextField("R ","Field 2"); 
        txtFldObjC = createFixedPrefixTextField("P ","Field 3"); 
         
        VBox box = new VBox(10); 
        box.setPadding(new Insets(10,10,10,10)); 
         
        box.getChildren().addAll(txtFldObjA, txtFldObjB, txtFldObjC, new Button("dummy"), new Button("other")); 
         
        StackPane root = new StackPane(box); 
        Scene scene = new Scene(root, 400, 400); 
        primaryStage.setScene(scene); 
        primaryStage.show(); 
    } 

    public static void main(String[] args) { 
        launch(args); 
    } 
} 