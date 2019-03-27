/*
 * Created on 27.03.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

/**
 * Behavior of ScrollBarPolicy.AS_NEEDED 
 * if isFitting -> shown only if both actual nodeSize and its min size > available
 * https://stackoverflow.com/q/55375051/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ScrollBarAsNeeded extends Application {

    private BorderPane borderPane;
    private GridPane inner;
    private TabPane tabPane;

    private ScrollPane scp;
    
    @Override
    public void start(Stage primaryStage) {

        tabPane = new TabPane();
        Tab tab = new Tab("test");
        tabPane.getTabs().add(tab);

        scp = new ScrollPane();
        scp.setFitToHeight(true);
        scp.setFitToWidth(true);
        scp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        borderPane = new BorderPane();
        borderPane.setCenter(innerGrid());
        tab.setContent(borderPane);
        tabPane.setMinWidth(Region.USE_PREF_SIZE);
        
//        scp.setContent(borderPane);  // this works
        scp.setContent(tabPane);   // this doesnt

        Scene s = new Scene(scp);
        primaryStage.setScene(s);
        primaryStage.show();
    }

    private boolean isHbarVisible() {
        ScrollPane sp = scp;
        Region scrollNode = (Region) sp.getContent();
        double nodeWidth = scrollNode.getWidth();
        double contentWidth = sp.getWidth(); // not quite right: it's the width passed into layoutChildren
        
        double minWidth = scrollNode.minWidth(-1);
        boolean canFit = sp.isFitToWidth() && scrollNode != null ? scrollNode.isResizable() : false;
        LOG.info("can? " + canFit);
        return canFit ?
                (nodeWidth > contentWidth && minWidth > contentWidth) : (nodeWidth > contentWidth);
    }
    private GridPane innerGrid() {
        inner = new GridPane();

        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setHgrow(Priority.SOMETIMES);
        inner.getColumnConstraints().add(columnConstraints);

        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setVgrow(Priority.SOMETIMES);
        inner.getRowConstraints().add(rowConstraints);

        HBox top = addHBox();
//        Button top = 
        top.setStyle("-fx-background-color: green;");
        inner.add(top, 0, 0);

        HBox top2 = addHBox();
        top2.setStyle("-fx-background-color: yellow;");
        inner.add(top2, 1, 0);

        HBox mid = addHBox();
        mid.setStyle("-fx-background-color: #f9d600;");
        inner.add(mid, 0, 1);

        HBox mid2 = addHBox();
        mid2.setStyle("-fx-background-color: #f98400;");
        inner.add(mid2, 1, 1);

        HBox bot = addHBox();
        bot.setStyle("-fx-background-color: #71f91d;");
        inner.add(bot, 0, 2);

        HBox bot2 = addHBox();
        bot2.setStyle("-fx-background-color: #0919f9;");
        inner.add(bot2, 1, 2);

        return inner;
    }

    public static void main(String[] args) {
        ScrollBarAsNeeded.launch(args);
    }

    int count; 
    public HBox addHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);

        Button buttonCurrent = new Button("Test " + count++);
        buttonCurrent.setOnAction(e -> {
            String text = "Content: min/pref/max/actual \n " + getPrefWidthText(scp.getContent()) + " / " + ((Region) scp.getContent()).getWidth();
            LOG.info(isHbarVisible() + text);
        });

//        List<Integer> integers = Arrays.asList(0, 1, 2);
//        Collections.reverse(integers);

        Button buttonProjected = new Button("Test 2");
        hbox.getChildren().addAll(buttonCurrent, buttonProjected);

        return hbox;
    }

    /**
     * @param borderPane
     */
    protected String getPrefWidthText(Node borderPane) {
        String borderSize = borderPane.minWidth(-1) + " / " + borderPane.prefWidth(-1) + " / " + borderPane.maxWidth(-1);
        return borderSize;
    }
    

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollBarAsNeeded.class.getName());
}

