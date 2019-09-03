/*
 * Created on 20.08.2019
 *
 */
package control.skin;

import java.lang.StackWalker.StackFrame;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Original report via webbug
 * https://bugs.openjdk.java.net/browse/JDK-8149622
 * EventDispatch sequence broken by ComboBox
 * <p>
 * fixed (fx9), introduces regression of 
 * https://bugs.openjdk.java.net/browse/JDK-8145515
 * eventFilter on textField in combo doesn't receive enter
 * <p>
 * regression reported:
 * https://bugs.openjdk.java.net/browse/JDK-8229914
 * noted for fx11
 * <p>
 * still broken for editable combo
 * reported: https://bugs.openjdk.java.net/browse/JDK-8229924
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboBoxEventDispatchBug_8149622 extends Application {

    public static void main(String... args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll("Test", "hello", "world");
        // event dispatch still broken if editable!
        cb.setEditable(true);
        
        cb.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            
            System.out.println("combobox filter" );
//            new RuntimeException().printStackTrace();
        });
        
        cb.addEventHandler(KeyEvent.KEY_RELEASED, event -> System.out.println("combobox handler"));
        
        VBox root = new VBox(cb);

        Scene scene = new Scene(root);

        scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> System.out.println("scene filter"));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, event -> System.out.println("scene handler"));

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}