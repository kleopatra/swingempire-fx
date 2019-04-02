/*
 * Created on 27.03.2019
 *
 */
package test.combobox;

import java.util.logging.Logger;

import static org.junit.Assert.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * combo prob with uncontained value: 
 * - set if had been null: shown as expected
 * - set if had been previously set: nothing shown
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboUncontainedValue extends Application {
//    private  ComboBox<String> testCombo;

    
    public static class MyComboBoxSkin<T> extends ComboBoxListViewSkin<T> {

        public MyComboBoxSkin(ComboBox<T> control) {
            super(control);
            FXUtils.invokeGetMethodValue(ComboBoxBaseSkin.class, this, "updateDisplayArea");
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            super.layoutChildren(x, y, w, h);
            // must be wrapped inside a runlater, either before or after calling super
//            Platform.runLater(this::getDisplayNode);
        }
        
    }
    @Override public void start(Stage primaryStage) {
        ComboBox<String> testCombo = new ComboBox<>(FXCollections.observableArrayList("Option 1", "Option 2", "Option 3")) {
//                @Override
//                protected Skin<?> createDefaultSkin() {
//                    return new MyComboBoxSkin<>(this);
//                }
            
        };
        
        ListCell<String> custom = new ListCell<>() {
            @Override public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                LOG.info("in updateItem" + testCombo.getValue() + " item: " + item + " empty: " + empty);
                new RuntimeException("").printStackTrace();
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    /**
                     * This label is used if the item associated with this cell is to be
                     * represented as a String. While we will lazily instantiate it
                     * we never clear it, being more afraid of object churn than a minor
                     * "leak" (which will not become a "major" leak).
                     */
                    setText(item == null ? "null" : item.toString());
                    setGraphic(null);
                }
            }

            /**
             * overridden to test whether the fix to JDK-8145588 might have 
             * introduced this. At least it's no longer virulent. 
             */
            @Override
            public void updateSelected(boolean selected) {
                
                super.updateSelected(selected);
                FXUtils.invokeSetFieldValue(Cell.class, this, "itemDirty", false);
            }
          
            
        };
        
        /**
         * from OP: ignore empty - works as expected
         */
        ListCell<String> ignoreEmpty = new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {return;} // this is the solution: DO NOT ERASE ON empty=true!
                // further logic copied from the solution in the default skin: 
                // see ComboBoxListViewSkin.updateDisplayText
                // (default testing for "item instanceof Node" omitted for brevity)
                final StringConverter<String> c = testCombo.getConverter();
                final String promptText = testCombo.getPromptText();
                String s = item == null && promptText != null ? promptText
                        : c == null ? (item == null ? null : item.toString()) : c.toString(item);
                setText(s);
                setGraphic(null);
            }

        };
//        testCombo.setButtonCell(custom);
        
        Button nullIt = new Button("Set null");
        nullIt.setOnAction(e -> {
            testCombo.setValue(null);
        });
        
        Button btn = new Button("Set test value outside list");
        btn.setOnAction(e -> {
            testCombo.setValue("test value outside list");
        });
        

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
        Button lookup = new Button("lookup displayText");
        lookup.setOnAction(e -> {
            displayText.setText(lookupDisplayText(testCombo));
//            ((ComboBoxBaseSkin) testCombo.getSkin()).getDisplayNode();
        });
        
        Button display = new Button("getdisplayText");
        display.setOnAction(e -> {
            displayText.setText(getDisplayText(testCombo));
//            ((ComboBoxBaseSkin) testCombo.getSkin()).getDisplayNode();
        });
        
        VBox root = new VBox(5);
        root.setPadding(new Insets(5));
        root.getChildren().addAll(nullIt, btn, testCombo, valueTextField, selectedItem, displayText, display, lookup);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Test Combo");
        primaryStage.setScene(scene);
        primaryStage.show();
        lookupDisplayText(testCombo);
    }

    ListCell<String> cell;
    
    protected String lookupDisplayText(ComboBox testCombo) {
        if (cell == null) {
            cell = getButtonCell(testCombo);
            assertNotNull(cell);
            
        } else {
            ListCell<String> current = getButtonCell(testCombo);
            
            LOG.info("same? " +  current + (current == cell) );
            if (current != null && cell != current) {
                cell = current;
            }
        }
        LOG.info("" + cell + cell.getParent());
        return cell.getText();
    }

    /**
     * @param testCombo
     * @return
     */
    protected ListCell getButtonCell(ComboBox testCombo) {
        ComboBoxListViewSkin skin = (ComboBoxListViewSkin) testCombo.getSkin();
        return (ListCell) FXUtils.invokeGetFieldValue(ComboBoxListViewSkin.class, skin, "buttonCell");
//        return (ListCell) testCombo.lookup(".list-cell");
    }

    protected String getDisplayText(ComboBox combo) {
        Node node = ((ComboBoxBaseSkin) combo.getSkin()).getDisplayNode();
        if (node instanceof ListCell) {
            LOG.info("access cell: " + node);
            return ((ListCell) node).getText();
        } 
        return "no cell";
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboUncontainedValue.class.getName());
}

