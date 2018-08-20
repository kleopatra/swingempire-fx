/*
 * Created on 20.08.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import de.swingempire.fx.util.DebugUtils;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Align text and graphics vertically in a TextFlow
 * https://bugs.openjdk.java.net/browse/JDK-8098128
 * 
 * closed as wontfix because support is available (just no simple api)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFlowVerticalAlign extends Application {

    @Override public void start(final Stage stage) {
        String family = "Helvetica";
        double fontSize = 20;


        WritableImage image = new WritableImage(20, 40);
        byte[] black = new byte[20*40*3];
        image.getPixelWriter().setPixels(0, 0, 20, 40, PixelFormat.getByteRgbInstance(), ByteBuffer.wrap(black), 20*3);
        ImageView iv = new ImageView(image);

        image = new WritableImage(20, 40);
        image.getPixelWriter().setPixels(0, 0, 20, 40, PixelFormat.getByteRgbInstance(), ByteBuffer.wrap(black), 20*3);
        ImageView iv2 = new ImageView(image) {
//            public double getBaselineOffset() {
//                return 20;
//            }
        };

        image = new WritableImage(20, 40);
        image.getPixelWriter().setPixels(0, 0, 20, 40, PixelFormat.getByteRgbInstance(), ByteBuffer.wrap(black), 20*3);
        Text text1 = new Text("Hello ") {
            // getBaselinOffset is final
        };
        ImageView iv3 = new ImageView(image) {
            @Override
            public double getBaselineOffset() {
                LOG.info(" image/text baseline offset " + super.getBaselineOffset());
                return 0;
            }
        };

        TextFlow textFlow = new TextFlow() {
            // getBaselinOffset is final
        };
        textFlow.setLayoutX(40);
        textFlow.setLayoutY(40);
        text1.setFont(Font.font(family, fontSize));
        Text text2 = new Text("Bold");
        text2.setFont(Font.font(family, FontWeight.BOLD, fontSize));
        Text text3 = new Text(" World");
        text3.setFont(Font.font(family, FontPosture.ITALIC, fontSize));
        textFlow.getChildren().addAll(text1, iv, text2, iv2, text3, iv3);

        
        Group group = new Group(textFlow);
        // fishy, need to revise the debug util: textFlow has its layoutX/Y modified
        DebugUtils.addAllBounds(textFlow, text1);
        Scene scene = new Scene(group, 500, 500, Color.WHITE);
        stage.setTitle("Hello Rich Text");
        stage.setScene(scene);
        stage.show();
        
        LOG.info("text/flow " + text1.getBaselineOffset() + " " + textFlow.getBaselineOffset());
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFlowVerticalAlign.class.getName());

}
