/*
 * Created on 26.06.2019
 *
 */
package de.swingempire.fx.graphic;

import java.util.logging.Logger;

import com.sun.javafx.scene.NodeHelper;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Quick check on subclassing Node/Shape/Shape3D directly - doesn't work
 * (read the doc, dude ;)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class NodeSubclassExample extends Application {

    public static class NodeSub extends Node {
        
        public NodeSub() {
        }

        /**
         * @return
         */
        protected NodeHelper getNodeHelper() {
            return (NodeHelper) FXUtils.invokeGetFieldValue(Node.class, this, "nodeHelper");
        }
    }
    private Parent createContent() {
        
        NodeSub node = new NodeSub();
        NodeHelper helper = node.getNodeHelper();
        LOG.info("creating - helper?" + helper);
        BorderPane content =  new BorderPane(node);
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
            .getLogger(NodeSubclassExample.class.getName());

}
