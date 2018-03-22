/*
 * Created on 20.03.2018
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.util.logging.Logger;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
/**
 * horizontal scrollbar hides content
 * https://stackoverflow.com/q/49386416/203657
 * <p>
 * Okay for fx9 (except a slight flicker when changing the policy, probably due to forced hiding
 * and re-showing). Not properly working in fx8: the sizing is not updated when changing
 * the policy.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ScrollPaneSizing extends Application{

    public static class DebugScrollPaneSkin extends ScrollPaneSkin {

        public DebugScrollPaneSkin(ScrollPane scroll) {
            super(scroll);
            unregisterChangeListeners(scroll.hbarPolicyProperty());
            registerChangeListener(scroll.hbarPolicyProperty(), p -> {
                if (scroll.getHbarPolicy() == AS_NEEDED)
                    getHorizontalScrollBar().setVisible(false);
                scroll.requestLayout();
            });
        }

        @Override
        protected double computePrefHeight(double x, double topInset,
                double rightInset, double bottomInset, double leftInset) {
            double computed = super.computePrefHeight(x, topInset, rightInset, bottomInset, leftInset);
            if (getSkinnable().getHbarPolicy() == ScrollBarPolicy.AS_NEEDED && getHorizontalScrollBar().isVisible()) {
                // this is fine when horizontal bar is shown/hidden due to resizing
                // not quite okay while toggling the policy
                // the actual visibilty is updated in layoutChildren?
                computed += getHorizontalScrollBar().prefHeight(-1);
                LOG.info("computed: " + computed);
            }
            return computed;
        }

        @Override
        protected void layoutChildren(double x, double y, double width,
                double height) {
            super.layoutChildren(x, y, width, height);
            LOG.info("" + height);
        }
        
        
        
    }
    private Parent createContent() {
        HBox inner = new HBox(new Text("somehing horizontal and again again ........")); 
        TitledPane titled = new TitledPane("my title", inner);
        VBox wrap = new VBox(titled);
        HBox outer = new HBox(wrap);
        ScrollPane scroll = new ScrollPane(outer) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new DebugScrollPaneSkin(this);
            }
            
        };
        scroll.setVbarPolicy(NEVER);
        scroll.setHbarPolicy(ALWAYS);
        // scroll.setFitToHeight(true);
        
        Button policy = new Button("toggle HBarPolicy");
        policy.setOnAction(e -> {
            ScrollBarPolicy p = scroll.getHbarPolicy();
            scroll.setHbarPolicy(p == ALWAYS ? AS_NEEDED : ALWAYS);
        });
        Button measure = new Button("measure");
        measure.setOnAction(e -> {
            Parent parent = titled;
            ScrollBar bar = ((ScrollPaneSkin) scroll.getSkin()).getHorizontalScrollBar();
            LOG.info("hbar visible? parent/scroll: " + bar.isVisible() + " / " +  parent.prefHeight(-1) + " / " + scroll.prefHeight(-1));
        });
        HBox buttons = new HBox(10, policy, measure);
        BorderPane content = new BorderPane();
        content.setTop(scroll);
        content.setBottom(buttons);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollPaneSizing.class.getName());

}
