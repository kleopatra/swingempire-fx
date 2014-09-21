/*
 * Created on 02.09.2014
 *
 */
package de.swingempire.fx.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * copy of openfx infrastructure
 * http://hg.openjdk.java.net/openjfx/8/master/rt/file/773008dbb998/modules/controls/src/test/java/com/sun/javafx/scene/control/infrastructure/StageLoader.java
 */
public class StageLoader {

    private Group group;
    private Scene scene;
    private Stage stage;

    public StageLoader(Node... content) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Null / empty content not allowed");
        }
        group = new Group();
        group.getChildren().setAll(content);
        scene = new Scene(group);
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
    
    public StageLoader(Scene scene) {
        stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
    
    public Stage getStage() {
        return stage;
    }
    
    public void dispose() {
        stage.hide();
        group.getChildren().clear();
        group = null;
        scene = null;
        stage = null;
    }
}

