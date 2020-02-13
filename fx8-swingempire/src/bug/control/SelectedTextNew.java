/*
 * Created on 09.02.2020
 *
 */
package control;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8176270
 * OOBE when accessing selectedText in listener
 * 
 * To reproduce manually:
 * - select last word until end
 * - type any char to replase selection
 * 
 * To reproduce with button
 * - click on button
 * 
 * Throwing only if changeListener is registered on selectedTextProperty, not
 * if no listener or invalidationListener is registered. Somehow related
 * to eager evaluation?
 * 
 */
public class SelectedTextNew extends Application {

    private TextField textField;

    @Override
    public void start(Stage primaryStage) {
        textField = new TextField("1234 5678");

        Button doSelect = new Button("doSelect");
        doSelect.setOnAction(e -> doSelectAndReplace());
        Pane root = new VBox();
        root.getChildren().addAll(textField, doSelect);

        // register changeListener blows on replace selection,
        // even if doing nothing
//        textField.selectedTextProperty().addListener((final ObservableValue<? extends String> ov,
//                final String oldSelection, final String newSelection) -> {
////                    System.out.println ("text selected: " + newSelection); 
//                });  
        
        // register invalidationListener is fine as long as the selectedText is not
        // forced to be evaluated (if, it blows just as the changeListener)
        textField.selectedTextProperty().addListener(ov
                 -> {
                    System.out.println ("text selected: " + textField.getSelectedText()); 
                });  
        
        Scene scene = new Scene(root, 100, 100);

        primaryStage.setScene(scene);
        primaryStage.show();

        Platform.runLater(new Runnable() {
            @Override public void run() {
            }
          });
                                
                                
                                
        }
    private void doSelectAndReplace() {
        textField.positionCaret(5);
//        WaitForAsyncUtils.waitForFxEvents();

        // select 2nd word
        textField.selectNextWord();
//        WaitForAsyncUtils.waitForFxEvents();

        // replace selection
        //type(KeyCode.DIGIT0);
        textField.replaceSelection("d");
//        WaitForAsyncUtils.waitForFxEvents();

    }

    public static void main(String[] args) {
        launch(args);
    }  
}