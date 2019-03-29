/*
 * Created on 29.03.2019
 *
 */
package de.swingempire.fx.scene.layout;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * content reported to be clipped: the datePickers at the right, the buttons at the bottom.
 * https://stackoverflow.com/q/55403477/203657
 * 
 * can reproduce the bottom, not the datePicker
 * the question talks about using computed_size (-1) but actually configures the
 * sizing hints to pref_size: the former is default and results in correct layout 
 * horizontally (not vertically)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class InitialSizeClipped extends Application {

    private Parent createContent() {
//        Region.USE_COMPUTED_SIZE;
        HBox box = new HBox(new Button("Button"), new Button("Button"));
        box.setAlignment(Pos.BASELINE_RIGHT);
        TitledPane buttons = new TitledPane("Untitled", box);
        buttons.setAnimated(false);
        buttons.setCollapsible(false);
        VBox content = new VBox(createInnerContent(), createInnerContent(), buttons);
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);
        LOG.info("min: " + content.getMinWidth() + (content.getMinWidth() == Region.USE_COMPUTED_SIZE));
        LOG.info("max: " + content.getMaxWidth() + (content.getMaxWidth() == Region.USE_COMPUTED_SIZE));
        LOG.info("pref: " + content.getPrefWidth() + (content.getPrefWidth() == Region.USE_COMPUTED_SIZE));
        return content;
    }

    private Node createInnerContent() {
        VBox content = new VBox(new Label("Label"), new DatePicker());
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMinHeight(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);
        content.setMaxHeight(Region.USE_PREF_SIZE);
        HBox box = new HBox(content);
        TitledPane titled =  new TitledPane("Untitled", box);
        titled.setAnimated(false);
        titled.setCollapsible(false);
        return titled;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        //stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(InitialSizeClipped.class.getName());

}
