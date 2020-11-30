/*
 * Created on 29.11.2020
 *
 */
package de.swingempire.fx.fxml;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/65059177/203657
 * move the line on mouseMoved
 */
public class VerticalTimeMarkIsNotRelocated extends Application {

    @FXML private StackPane timeMarkContainer;
    @FXML private Label     label;
    @FXML private VBox      timeMark;

    @FXML
    private void initialize() {
       StackPane.clearConstraints( timeMark );
       StackPane.setAlignment( label, Pos.BOTTOM_CENTER );
//       timeMark.setManaged(false);
    }

    @FXML
    private void moveTimeMark( MouseEvent e ) {
       final double x = e.getX();
       System.err.printf( "%7.2f\n", x );
       timeMark.relocate( x*20, 0 );
//       timeMark.setTranslateX(x);
//       StackPane.setMargin( timeMark,
//               new Insets( 0.0, 0.0, 0.0, -timeMarkContainer.getWidth()+2.0*e.getX()));
    }

    @Override
    public void start( Stage primaryStage ) throws Exception {
       final Class<?> clazz = getClass();
       primaryStage.setScene( new Scene( FXMLLoader.load( clazz.getResource( "verticaltimemark.fxml" ))));
       primaryStage.show();
    }

    public static void main( String[] args ) {
       launch( args );
    }
 }