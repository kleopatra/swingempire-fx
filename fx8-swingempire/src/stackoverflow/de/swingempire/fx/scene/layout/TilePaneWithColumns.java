/*
 * Created on 22.01.2020
 *
 */
package de.swingempire.fx.scene.layout;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59826882/203657
 * TilePane showing more than prefColumns tiles in a row
 * 
 * behaves as specified: prefColumns is used to calc the prefWidth, layouts
 * are free to size to whatever. Solution is to use a layout that
 * respects its children's pref
 */
public class TilePaneWithColumns extends Application {
    TilePane tp = new TilePane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setResizable(false);  
            tp.setPrefColumns(10);
            setTP();    
            // original: used as root
//            Scene scene = new Scene(tp,800,600);
            // fix: add to layout that respects prefWidth, f.i. HBox
            Scene scene = new Scene(new HBox(tp),800,600);
            // Note: VBox doesn't work because it respects prefHeight
//            Scene scene = new Scene(new VBox(tp),800,600);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setTP() {           
        tp.setVisible(true);

        int[] numbers = {1,2,3,4,5,6,7,8,9,10};

        for(int row=0; row<11; row++) {

            for (int i: numbers ) {

                Text t = new Text(String.valueOf(i));

                HBox hbox = new HBox();
                hbox.getChildren().add(t);

                hbox.setStyle("-fx-border-color: red;");
                tp.getChildren().add(hbox);
            }
        }
    }
}

