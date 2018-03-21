/*
 * Created on 20.03.2018
 *
 */
package control.skin;

import java.util.logging.Logger;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * horizontal scrollbar hides content
 * https://stackoverflow.com/q/49386416/203657
 * 
 * reported: 
 * https://bugs.openjdk.java.net/browse/JDK-8199934
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ScrollPaneSizingBug extends Application{

    private Parent createContent() {
        Text bigFatText = new Text("something");
        // increase size to get out of minHeight
        bigFatText.setFont(bigFatText.getFont().font(100));
        ScrollPane scroll = new ScrollPane(bigFatText);
        // no vbar to not interfere
        scroll.setVbarPolicy(NEVER);
        // start with correct height
        scroll.setHbarPolicy(ALWAYS);
        Button policy = new Button("toggle HBarPolicy");
        policy.setOnAction(e -> {
            ScrollBarPolicy p = scroll.getHbarPolicy();
            scroll.setHbarPolicy(p == ALWAYS ? AS_NEEDED : ALWAYS);
        });
        Button measure = new Button("measure");
        measure.setOnAction(e -> {
            Node content = bigFatText;
            ScrollBar bar = ((ScrollPaneSkin) scroll.getSkin()).getHorizontalScrollBar();
            LOG.info("hbar visible? parent/scroll: " + bar.isVisible() + " / "
                    +  content.prefHeight(-1) + " / " + scroll.prefHeight(-1));
        });
        Label policyText = new Label();
        policyText.textProperty().bind(scroll.hbarPolicyProperty().asString());
        HBox buttons = new HBox(10,  measure, policy, policyText);
        BorderPane content = new BorderPane();
        content.setTop(scroll);
        content.setBottom(buttons);
        return content;
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
            .getLogger(ScrollPaneSizingBug.class.getName());

}
