/*
 * Created on 09.04.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
/**
 * https://stackoverflow.com/q/49726845/203657
 * control as items: style set at runtime only applied to visible controls
 * 
 * It's wrong to have controls as data, but shouldn't the style be applied
 * on making it visible? 
 * 
 * What's happening here is that the OP styles the children before they were
 * created by the skin, so effectively does nothing on cells that had not yet
 * been visible.
 */
public class JavaFXCssBroken extends Application {

    @Override
    public void start(Stage primaryStage) {
        ListView<CheckBox> listView = new ListView<>();
        for (int i = 0; i < 100; i++) {
            listView.getItems().add(new CheckBox("Element " + i));
        }

        CheckBox check = new CheckBox("standalone");
        Label label = new Label("?", check);
        Button markAll = new Button("Select all");
        markAll.setOnAction(aevt -> {
            Color newColor = Color.BLUE;
            listView.getItems().stream()
                    .forEach(
//                            this::select
                            updateStyle(newColor)
                    );
            updateStyle(newColor, check);
//            select(check);
//            markAll.getParent().getStylesheets().
//                add(this.getClass().getResource("checkstyle.css").toExternalForm());
            
            label.setGraphic(check);

        });

        VBox vbox = new VBox(listView, markAll, label);

        StackPane root = new StackPane();
        root.getChildren().add(vbox);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("JavaFXCssBroken");
        primaryStage.setScene(scene);
        primaryStage.show();
        // listview cell factory applied on-the-fly by skin!
//        LOG.info(""+ listView.getCellFactory());
    }

    
    /**
     * @param newColor
     * @return
     */
    protected Consumer<? super CheckBox> updateStyle(Color newColor) {
        return checkbox -> {
            updateStyle(newColor, checkbox);
        };
    }
    
    /**
     * @param newColor
     * @return
     */
    protected Consumer<? super CheckBox> updateStyle() {
        return checkbox -> {
                  select(checkbox);
               };
    }

    /**
     * @param newColor
     * @param checkbox
     */
    protected void updateStyle(Color newColor, CheckBox checkbox) {
        select(checkbox);
          checkbox.getChildrenUnmodifiable().stream()
         .forEach(child -> child.setStyle(new StringJoiner(", ", "-fx-background-color: rgba(", ")")
         .add(Double.toString(255 * newColor.getRed()))
         .add(Double.toString(255 * newColor.getGreen()))
         .add(Double.toString(255 * newColor.getBlue()))
         .add(Double.toString(newColor.getOpacity()))
         .toString()));
       System.out.println("children: " + checkbox.getChildrenUnmodifiable());    
    }


    /**
     * @param checkbox
     */
    protected void select(CheckBox checkbox) {
        checkbox.setSelected(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(JavaFXCssBroken.class.getName());
}

