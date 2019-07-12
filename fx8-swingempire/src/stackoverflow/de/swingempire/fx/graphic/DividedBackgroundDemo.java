/*
 * Created on 12.07.2019
 *
 */
package de.swingempire.fx.graphic;

import java.util.logging.Logger;

import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/56922706/203657 two-color background with
 * diagonal border between
 * 
 * use a path and bind its elements' locations to the control is fine for Text, 
 * not working for control ... something wrong with the bounds? Too lazy to explore ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DividedBackgroundDemo extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        
        BackgroundFill fill;
        Background bg;
        
        String arrow = "M 44.7016 41.0951 L 69.3251 65.7207 L 44.5005 90.2008 L 55.7281" + 
                "101.4289 L 92.0866 65.0698 L 56.5768 29.5638 L 44.7016 41.0951 Z";      
        
        String alt = "M 0 0 L 200 0 L 0 200 Z";
        SVGPath arrowShape = new SVGPath();
        arrowShape.setContent(alt);
        arrowShape.setFill(Color.WHITE);
                
//                SVGPathBuilder.create()
//                                   .content(arrow)                                        
//                                   .build();

        MoveTo start = new MoveTo(0, 0);
        HLineTo upperR = new HLineTo(100);
        LineTo lowerL = new LineTo(0, 100);
        VLineTo end = new VLineTo(0);
        Path path = new Path(start, upperR, lowerL, end);
        path.setFill(Color.BEIGE);
        path.setStrokeWidth(0);
        primaryStage.setTitle("Test: Diagonally divided colour background");

        Text txt = new Text("Text ................");
//        Label txt = new Label("Text ................");
        txt.setFont(
                Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 50));


        DoubleBinding startX = new DoubleBinding() {
            
            {
                txt.boundsInParentProperty();
            }
            @Override
            protected double computeValue() {
                return txt.boundsInParentProperty().get().getMinX();
            }
            
        };
        DoubleBinding startY = new DoubleBinding() {
            
            {
                txt.boundsInParentProperty();
            }
            @Override
            protected double computeValue() {
                return txt.boundsInParentProperty().get().getMinY();
            }
            
        };
        DoubleBinding width = new DoubleBinding() {
            
            {
                txt.boundsInParentProperty();
            }
            @Override
            protected double computeValue() {
                return txt.boundsInParentProperty().get().getMaxX();
            }
            
        };
        DoubleBinding height = new DoubleBinding() {

            {
                txt.boundsInParentProperty();
            }
            @Override
            protected double computeValue() {
                return txt.boundsInParentProperty().get().getMaxY();
            }
            
        };
        start.xProperty().bind(startX);
        start.yProperty().bind(startY);
        upperR.xProperty().bind(width);
        lowerL.yProperty().bind(height);
        lowerL.xProperty().bind(startX);
        end.yProperty().bind(startY);
        StackPane root = new StackPane();
//        start.xProperty().bind(root.layoutXProperty());
//        upperR.xProperty().bind(root.widthProperty());
        root.getChildren().addAll(path, txt);
//        txt.setStyle("-fx-background-color: yellow;");

        DebugUtils.addBoundsInLocal(root, txt);
        BorderPane content = new BorderPane(root);
//        FlowPane content = new FlowPane(100, 100);
//        content.getChildren().add(root);
        primaryStage.setScene(new Scene(content, 200, 200));
        primaryStage.show();
        LOG.info("" + path.getElements());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DividedBackgroundDemo.class.getName());
}

