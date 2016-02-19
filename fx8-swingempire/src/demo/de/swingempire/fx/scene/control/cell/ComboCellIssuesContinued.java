/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Combo/Choice must be focused after starting edit, reported
 * https://bugs.openjdk.java.net/browse/JDK-8138682
 * 
 * Combo must not commit on navigating drop-down, reported:
 * https://bugs.openjdk.java.net/browse/JDK-8138683
 * fixed: listening to showing instead of selection
 * 
 * Note: showing is a whacky indicator, commented the issue
 * doesn't appear to be as bad as it looked: 
 * - suspected: esc commits, actual doesn't commit
 * - suspected: hide popup commits, actual doesn't commit when editable
 * - suspected/actual: commit (aka: enter) without popup been visible doesn't commit
 * 
 * New:
 * doesn't commit edited value
 * https://bugs.openjdk.java.net/browse/JDK-8150041
 * 
 * New:
 * Combo must not throw NPE when clicking the dropdown arrow twice
 * https://bugs.openjdk.java.net/browse/JDK-8150042
 * 
 * New:
 * unexpected behavior on Esc/Enter after naviging the popup
 * - popup open/navigate/esc must cancel (commits)
 * - popup closed/navigate/enter must commit (does nothing)
 * https://bugs.openjdk.java.net/browse/JDK-8150046
 * 
 * Combo must commit on selecting old value
 * https://bugs.openjdk.java.net/browse/JDK-8138688
 * fixed: listening to showing instead of selection
 * 
 * Combo must cope with modification of items in edit handler
 * https://bugs.openjdk.java.net/browse/JDK-8138747
 * 
 * Also: test driver for XComboBoxTableCell, both not/editable combo.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboCellIssuesContinued extends Application {
  
    private Parent getContent() {
        
        ObservableList<Shape> items = FXCollections.observableArrayList(
               new Line(), new Rectangle(), new Arc());
   
        items.forEach(p -> p.setId(p.getClass().getSimpleName()));
        List<String> names = items.stream().map(Node::getId).collect(Collectors.toList());
        TableView<Shape> table = new TableView<>(items);
        table.setEditable(true);
        TableColumn<Shape, String> plain = new TableColumn<>("Plain");
        plain.setCellValueFactory(new PropertyValueFactory("id"));
        plain.setCellFactory(TextFieldTableCell.forTableColumn());
        table.getColumns().addAll(plain);
        
        TableColumn<Shape, String> combo = new TableColumn<>("Combo");
        combo.setCellValueFactory(new PropertyValueFactory("id"));
        combo.setCellFactory(p -> {
            ComboBoxTableCell tc = new ComboBoxTableCell(names.toArray()); //"someId", "other", "orNothing");
            tc.setComboBoxEditable(true);
            return tc;
        });
        table.getColumns().addAll(combo); 
        
        TableColumn<Shape, String> fixedCombo = new TableColumn<>("fixedCombo (editable)");
        fixedCombo.setCellValueFactory(new PropertyValueFactory("id"));
        fixedCombo.setCellFactory(p -> {
            ComboBoxTableCell tc = new XComboBoxTableCell(names.toArray());
            tc.setComboBoxEditable(true);
            return tc;
        });
        table.getColumns().addAll(fixedCombo); 
        
        TableColumn<Shape, String> fixedNotEditable = new TableColumn<>("fixedCombo (not editable)");
        fixedNotEditable.setCellValueFactory(new PropertyValueFactory("id"));
        fixedNotEditable.setCellFactory(p -> {
            ComboBoxTableCell tc = new XComboBoxTableCell(names.toArray());
            tc.setComboBoxEditable(false);
            return tc;
        });
        table.getColumns().addAll(fixedNotEditable); 
      
        BorderPane content = new BorderPane(table);
        
        // quick check to verify combo's value == action == selected notiication 
        // all below is just to analyse combo's behavior
        ComboBox comboBox = new ComboBox(FXCollections.observableArrayList(names));
//        boolean editable = true;
//        if (editable) {
//            comboBox.setEditable(true);
//            // update selection on change of text
//            ChangeListener<String> textListener = (src, ov, nv) -> {
//                LOG.info("getting text: " + ov + "/" + nv);
//                StringConverter c = comboBox.getConverter();
//                Object o = c != null ? c.fromString(nv) : nv;
//                comboBox.getSelectionModel().select(o);
//            };
//            comboBox.getEditor().textProperty().addListener(textListener);
//        }
//        comboBox.addEventHandler(KeyEvent.KEY_RELEASED, createKeyHandler("Handler "));
//        comboBox.addEventHandler(KeyEvent.KEY_PRESSED, createKeyHandler("Handler "));
//        comboBox.addEventFilter(KeyEvent.KEY_RELEASED, createKeyHandler("Filter "));
//        comboBox.addEventFilter(KeyEvent.KEY_PRESSED, createKeyHandler("Filter "));
//        
//        comboBox.addEventHandler(MouseEvent.MOUSE_RELEASED, createMouseHandler("Handler "));
//        comboBox.addEventHandler(MouseEvent.MOUSE_PRESSED, createMouseHandler("Handler "));
//        comboBox.addEventFilter(MouseEvent.MOUSE_RELEASED, createMouseHandler("Filter "));
//        comboBox.addEventFilter(MouseEvent.MOUSE_PRESSED, createMouseHandler("Filter "));
        
        comboBox.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("new value: " + nv);
        });
        comboBox.setOnAction(e -> {
            LOG.info("action " + comboBox.getValue());
        });
        
        HBox bottom = new HBox(10, comboBox);
        content.setBottom(bottom);
        return content;
    }

    /**
     * @param string
     * @return
     */
    private EventHandler createMouseHandler(String text) {
        return e -> {
            LOG.info(text + e.getEventType() + " / " +e);
            
        };
    }

    /**
     * @return
     */
    private EventHandler<? super KeyEvent> createKeyHandler(String text) {
        return e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // keyPressed- enter never reaches the combo
                LOG.info(text + e.getEventType() + " / " +e);
            } else if (e.getCode() == KeyCode.ESCAPE) {
                // keyPressed esc if popup showing, esc is swallowed by hiding
                LOG.info(text + e.getEventType() + " / " +e);
            }
        };
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboCellIssuesContinued.class.getName());
}
