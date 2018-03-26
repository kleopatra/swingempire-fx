/*
 * Created on 22.08.2017
 *
 */
package de.swingempire.fx.scene.control.skin;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Trying to dig into sequence of scene/skin notification:
 * - scene seems to be before skin, at that time skin is not yet set
 * - notification of scene, children not yet have scene (top-down?)
 * - at notification of skin change, children have not yet skin (top-down)
 * - at notification of skin change, direct children (always?) have scene
 * - at notification of skin change, grandchildren might not have scene
 * @author Jeanette Winzenburg, Berlin
 */
public class SceneSkinExp extends Application {

    Label parentA;
    Label childA;
    Label grandChildA;
    
    private Parent getContent() {
        parentA = createLabel("parentA");
        childA = createLabel("childA");
        grandChildA = createLabel("grandChildA");
        parentA.setGraphic(childA);
        childA.setGraphic(grandChildA);
        VBox box = new VBox(10, parentA);
        return box;
    }

    private Label createLabel(String string) {
        Label label = new Label(string);
        label.sceneProperty().addListener((src, ov, nv) -> sceneChanged(src, ov, nv));
        label.skinProperty().addListener((src, ov, nv) -> skinChanged(src, ov, nv));
        return label;
    }

    private void skinChanged(ObservableValue<? extends Skin<?>> src,
            Skin<?> ov, Skin<?> nv) {
        Label label = (Label) nv.getSkinnable();
        Label graphic = (Label) label.getGraphic();
        String graphicText = graphic != null ? graphic.getText() + " graphic-Skin " + graphic.getSkin() : " no graphic";
        System.out.println("skinChange for " + label.getText() + " with graphic " + graphicText);
        if ("parentA".equals(label.getText())) {
//            Label childA = (Label) label.getGraphic();
//            Label grandChildA = (Label) childA.getGraphic();
//            LOG.info("scene on grandChild: " + grandChildA.getScene());
//            LOG.info("window? " + label.getScene().getWindow());
//            label.getScene().getWindow().showingProperty().addListener((wsrc, wov, wnv) -> onShowing(wnv));
        }
    }

    /**
     * @param wnv
     * @return
     */
    private void onShowing(Boolean wnv) {
        LOG.info("showing");
    }

    private void sceneChanged(ObservableValue<? extends Scene> src, Scene ov,
            Scene nv) {
        ReadOnlyProperty<Scene> sceneProperty= (ReadOnlyProperty<Scene>) src;
        Label label = (Label) sceneProperty.getBean();
        String text = label.getText();
        System.out.println("scene change for " + text + " with skin " + label.getSkin());
        if ("parentA".equals(label.getText())) {
//          Label childA = (Label) label.getGraphic();
//          Label grandChildA = (Label) childA.getGraphic();
//          LOG.info("scene on grandChild: " + grandChildA.getScene());
            // window still null, can't start listening directly
//          label.getScene().getWindow().showingProperty().addListener((wsrc, wov, wnv) -> onShowing(wnv));
          label.getScene().windowProperty().addListener((wsrc, wov, wnv) -> {
              if (wnv != null) {
                  LOG.info("window? " + label.getScene().getWindow());
                  wnv.showingProperty().addListener((ssrc, sov, snv) -> onShowing(snv));
              }
          });
      }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 500, 200));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SceneSkinExp.class.getName());
}
