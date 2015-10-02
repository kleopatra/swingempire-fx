/*
 * Created on 01.10.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import de.swingempire.fx.util.FXUtils;

/**
 * Combo/Choice must be focused after starting edit, reported
 * https://bugs.openjdk.java.net/browse/JDK-8138682
 * 
 * Combo must not commit on navigating drop-down, reported:
 * https://bugs.openjdk.java.net/browse/JDK-8138683
 * 
 * Combo must commit on selecting old value
 * https://bugs.openjdk.java.net/browse/JDK-8138683
 * 
 * Combo must cope with modification of items in edit handler
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboChoiceCellIssues extends Application {

    
    int count;
    private Parent getContent() {
        ObservableList<Shape> items = FXCollections.observableArrayList(
                new Line(), new Rectangle(), new Arc());
   
        TableView<Shape> table = new TableView<>(items);
        table.setEditable(true);
        TableColumn<Shape, StrokeType> plain = new TableColumn<>("Plain");
        plain.setCellValueFactory(new PropertyValueFactory("strokeType"));
        table.getColumns().addAll(plain);
        
//        TableColumn<Shape, StrokeType> combo = new TableColumn<>("Combo");
//        combo.setCellValueFactory(new PropertyValueFactory("strokeType"));
//        combo.setCellFactory(ComboBoxTableCell.forTableColumn(StrokeType.values()));
//        
//        TableColumn<Shape, StrokeType> choice = new TableColumn<>("Choice");
//        choice.setCellValueFactory(new PropertyValueFactory("strokeType"));
//        choice.setCellFactory(ChoiceBoxTableCell.forTableColumn(StrokeType.values()));
        
//        TableColumn<Shape, StrokeType> comboWithHandler = new TableColumn<>("ComboWithHandler");
//        comboWithHandler.setCellValueFactory(new PropertyValueFactory("strokeType"));
//        ObservableList<StrokeType> modifiableCombos = FXCollections.observableArrayList(StrokeType.values());
//        comboWithHandler.setCellFactory(ComboBoxTableCell.forTableColumn(modifiableCombos));
//        comboWithHandler.setOnEditCommit(ev -> {
//            LOG.info("counter: " + count++ + ev.getNewValue());
//            ev.getRowValue().setStrokeType(ev.getNewValue());
//            modifiableCombos.remove(ev.getNewValue());
//            if (ev.getNewValue() != null) {
//            }
//        });
//        table.getColumns().addAll(comboWithHandler);

//        TableColumn<Shape, StrokeType> choiceWithHandler = new TableColumn<>("ChoiceWithHandler");
//        choiceWithHandler.setCellValueFactory(new PropertyValueFactory("strokeType"));
//        ObservableList<StrokeType> modifiableChoices =FXCollections.observableArrayList(StrokeType.values());
//        choiceWithHandler.setCellFactory(ChoiceBoxTableCell.forTableColumn(modifiableChoices));
//        choiceWithHandler.setOnEditCommit(ev -> {
//            LOG.info("counter: " + count++ + ev.getNewValue());
//            if (ev.getNewValue() != null) {
//                ev.getRowValue().setStrokeType(ev.getNewValue());
//                modifiableChoices.remove(ev.getNewValue());
//            }
//        });
//        table.getColumns().addAll(choiceWithHandler);

//        TableColumn<Shape, StrokeType> comboFix = new TableColumn<>("fixed Combo");
//        comboFix.setCellValueFactory(new PropertyValueFactory("strokeType"));
//        comboFix.setCellFactory(c -> new FixedComboBoxTableCell<>(StrokeType.values()));
//
        TableColumn<Shape, StrokeType> comboFixWithHandler = new TableColumn<>("fixed ComboHandler");
        comboFixWithHandler.setCellValueFactory(new PropertyValueFactory("strokeType"));
        ObservableList<StrokeType> modifiableFixed = FXCollections.observableArrayList(StrokeType.values());
        comboFixWithHandler.setCellFactory(c -> new FComboBoxTableCell<>(modifiableFixed));
        comboFixWithHandler.setOnEditCommit(ev -> {
            LOG.info("counter: " + count++ + ev.getNewValue());
            if (ev.getNewValue() == null) return;
            ev.getRowValue().setStrokeType(ev.getNewValue());
            modifiableFixed.remove(ev.getNewValue());
        });
        table.getColumns().addAll(comboFixWithHandler);
        
        Parent content = new BorderPane(table);
        return content;
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
            .getLogger(ComboChoiceCellIssues.class.getName());
}
