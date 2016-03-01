/*
 * Created on 04.10.2014
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * ComboBoxBaseSkin installs mouse handlers on the arrow only if the combo is
 * editable at the time of creating the skin.
 * 
 * Compare behavior on mouse predded/released on the arrow
 * click on arrow while popup open 
 * - initial editable: popup is hidden
 * - dynamic editable: popup is hidden and shown again
 * 
 * reported
 * https://bugs.openjdk.java.net/browse/JDK-8150960
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ComboEditableArrowHandler extends Application {

    private Parent getContent() {
        ComboBox box = new ComboBox(FXCollections.observableArrayList("one", "two", "three"));
        box.setEditable(true);
        box.setValue("initial editable");
        ComboBox core = new ComboBox(FXCollections.observableArrayList("one", "two", "three"));
        core.setValue("dynamic editable");
        Button toggle = new Button("toggle editable");
        toggle.setOnAction(e -> {
            core.setEditable(!core.isEditable());
        });
        
        return new VBox(10, box, core, toggle);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
