/*
 * Created on 20.10.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58468478/203657
 * max text length
 * 
 * OP: tried to do in onTyped
 * Answerer: did onTyped and reported to work? Doesn't, commented ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFieldValidateOnTyped extends Application {
    
    private Parent createContent() {
        TextField field = new TextField("some text that shouldn't grow");
        int maxLength = field.getLength();
        field.setPrefColumnCount(maxLength);
        field.setOnKeyTyped(e -> {
                if (field.getText().length() > 10) {
                    
                    e.consume();
                
                }
                field.setOnKeyTyped(e1 -> {
            });
        });
        BorderPane content = new BorderPane(field);
        return content;
    }

/*
 actually, your snippet is ... really _really_ __really__ wrong .. it
 a) doesn't compile 
 b) seems to register a new onKeyTyped handler each time the onType method is called 
 c) doesn't even work - don't know why it is accepted, as it does exactly nothing 
    to solve the problem 
    (and would wonder if it did: the handler registered with setOnXX 
    is guaranteed to be the _last_ of all registered with addEventHandler(XXType), 
    that is _after_ the skin already handled the key by replacing the text - 
    and even if that were not the case: sibling handler are _always_ notified, 
    irrespective of the consumed)     
 */
    // original snippet from answer
//    TextField txtDescription;    
//    private void onType(){
//        txtDescription.setOnKeyTyped(event ->{
//        int maxCharacters = 30;
//        if(txtDescription.getText().length() > maxCharacters)event.consume();
//    }); 
        
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
            .getLogger(TextFieldValidateOnTyped.class.getName());

    
}
