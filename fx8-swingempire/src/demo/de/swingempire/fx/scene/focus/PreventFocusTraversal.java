/*
 * Created on 08.09.2015
 *
 */
package de.swingempire.fx.scene.focus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
/**
 * Keep focus inside a parent, don't allow traversal to any outside control via keyboard.
 * 
 * https://stackoverflow.com/q/51668644/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class PreventFocusTraversal extends Application {

    /**
     * @return
     */
    private Parent getContent() {
        VBox prev = new VBox(10, new Button("prev1"), new Button("prev2"));
        
        VBox confine = new VBox(10, new Button("first"), new Button("second"), new Button("third"));
        ParentTraversalEngine engine = new ParentTraversalEngine(confine, new Algorithm( ) {

            @Override
            public Node select(Node owner, Direction dir,
                    TraversalContext context) {
                Node next = trav(owner, dir, context.getRoot());
                return next;
            }
            
            /**
             * Implement a traversal that's trapped inside the parent.
             * TBD: handle traversal into subtree
             */
            private Node trav(Node node, Direction drctn, Parent vb) {
                int index = vb.getChildrenUnmodifiable().indexOf(node);

                switch (drctn) {
                    case DOWN:
                    case RIGHT:
                    case NEXT:
                    case NEXT_IN_LINE:    
                        index++;
                        break;
                    case LEFT:
                    case PREVIOUS:
                    case UP:
                        index--;
                }

                if (index < 0) {
                    index = vb.getChildrenUnmodifiable().size() - 1;
                }
                index %= vb.getChildrenUnmodifiable().size();

                System.out.println("Select <" + index + ">");

                return vb.getChildrenUnmodifiable().get(index);
            }



            @Override
            public Node selectFirst(TraversalContext context) {
                return context.getRoot().getChildrenUnmodifiable().get(0);
            }

            @Override
            public Node selectLast(TraversalContext context) {
                Parent confine = context.getRoot();
                int size = confine.getChildrenUnmodifiable().size();
                return confine.getChildrenUnmodifiable().get(size - 1);
            }
            
        });
        
        ParentHelper.setTraversalEngine(confine, engine);
        VBox next = new VBox(10, new Button("dummy"), new Button("select first"));
        
        HBox outer = new HBox(10, prev, confine, next);
        return outer;
    }

    /**
     * Utility method to transfer focus from the given node into the
     * direction. Implemented to reflectively (!) invoke Scene's
     * package-private method traverse.
     * 
     * @param node
     * @param next
     */
    public static void traverse(Node node, Direction dir) {
        Scene scene = node.getScene();
        if (scene == null) return;
        try {
            Method method = Scene.class.getDeclaredMethod("traverse", 
                    Node.class, Direction.class);
            method.setAccessible(true);
            method.invoke(scene, node, dir);
        } catch (NoSuchMethodException | SecurityException 
                | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(PreventFocusTraversal.class.getName());
}
