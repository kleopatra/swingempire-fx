/*
 * Created on 24.03.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Let Text flow around images.
 * https://stackoverflow.com/q/55311564/203657
 * 
 * Not supported, nothing obvious to implement ... give up
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextFlowWithImage extends Application {

    private Parent createContent() {
        
        
        Text one = new Text("all very longish or some other textual string representation");
        Text other = new Text("sdlkjf ekjrekj  lsjlje r dlejri ald jfle ld fle blind text written by apes");
        
        
        WritableImage image = new WritableImage(20, 40);
        byte[] black = new byte[20*40*3];
        image.getPixelWriter().setPixels(0, 0, 20, 40, PixelFormat.getByteRgbInstance(), ByteBuffer.wrap(black), 20*3);
        ImageView iv = new ImageView(image){
            @Override
            public double getBaselineOffset() {
                LOG.info(" image/text baseline offset " + super.getBaselineOffset());
                return 0;
            }

        };

        Label label = new Label(one.getText() + " " + other.getText());
        label.setGraphic(iv);
        label.setWrapText(true);
        
        TextArea area = new TextArea();
        area.setWrapText(true);
        area.setText(one.getText() + "\n " +  other.getText());
//        TextFlow flow = new TextFlow(iv, one, other);
        
        Button debug =  new Button("debug");
        debug.setOnAction(e -> {
            LOG.info("nodes: " + getParagraphNodes(area).getChildren());
        });
        
        BorderPane content = new BorderPane(area);
        content.setBottom(debug);
        return content;
    }

    private Group getParagraphNodes(TextArea area) {
        TextAreaSkin skin = (TextAreaSkin) area.getSkin();
        Group paras = (Group) FXUtils.invokeGetFieldValue(TextAreaSkin.class, skin, "paragraphNodes");
        return paras;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextFlowWithImage.class.getName());

}
