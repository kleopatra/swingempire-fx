/*
 * Created on 27.03.2019
 *
 */
package test.combobox;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * combo prob with uncontained value: 
 * - set if had been null: shown as expected
 * - set if had been previously set: nothing shown
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboUncontainedValue extends Application {
    private static ComboBox<String> testCombo;

    
    public static class MyComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        public MyComboBoxSkin(ComboBox<T> control) {
            super(control);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            // must be wrapped inside a runlater, either before or after calling super
            Platform.runLater(this::getDisplayNode);
        }
        
    }
    @Override public void start(Stage primaryStage) {
        Button btn = new Button("Set test value outside list");
        btn.setOnAction(e -> {
            testCombo.setValue("test value outside list");
        });
        
        testCombo = new ComboBox<>(FXCollections.observableArrayList("Option 1", "Option 2", "Option 3")) {
                @Override
                protected Skin<?> createDefaultSkin() {
                    return new MyComboBoxSkin<>(this);
                }
                
        };

        TextField valueTextField = new TextField();
        testCombo.valueProperty().addListener((ob, ov, nv) -> {
            valueTextField.setText("combo value: " + nv);
//           ((ComboBoxBaseSkin) testCombo.getSkin()).getDisplayNode();
        });

        TextField selectedItem =  new TextField();
        testCombo.getSelectionModel().selectedItemProperty().addListener((c, ov, nv) -> {
            selectedItem.setText("selected item: " + nv);
        });
        
        TextField displayText = new TextField();
        Button display = new Button("getDisplayNode");
        display.setOnAction(e -> {
            ((ComboBoxBaseSkin) testCombo.getSkin()).getDisplayNode();
        });
        
        VBox root = new VBox(5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(btn, testCombo, valueTextField, selectedItem, displayText, display);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Test Combo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    protected String getDisplayText(ComboBox combo) {
        Node node = ((ComboBoxBaseSkin) combo.getSkin()).getDisplayNode();
//        if (node instanceof ListCell) {
//            return ((ListCell) node).getText();
//        } 
        return "";
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboUncontainedValue.class.getName());
}

