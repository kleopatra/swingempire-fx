/*
 * Created on 07.05.2015
 *
 */
package de.swingempire.fx.scene.focus;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;

/**
 * Requirement: configure focus traversal
 * old question with old hack (using internal api):
 * http://stackoverflow.com/q/15238928/203657
 * 
 * New question (closed as duplicate by ... me ..)
 * http://stackoverflow.com/q/30094080/203657
 * Old hack doesn't work, change of internal api
 * rewritten to new internal (sic!) api
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class FocusTraversal extends Application {

    private Parent getContent() {
        final VBox vb = new VBox();

        final Button button1 = new Button("Button 1");
        final Button button2 = new Button("Button 2");
        final Button button3 = new Button("Button 3");

        Algorithm algo = new Algorithm() {

            @Override
            public Node select(Node node, Direction dir,
                    TraversalContext context) {
                Node next = trav(node, dir);
                return next;
            }
            
            /**
             * Just for fun: implemented to invers reaction
             */
            private Node trav(Node node, Direction drctn) {
                int index = vb.getChildren().indexOf(node);

                switch (drctn) {
                    case DOWN:
                    case RIGHT:
                    case NEXT:
                    case NEXT_IN_LINE:    
                        index--;
                        break;
                    case LEFT:
                    case PREVIOUS:
                    case UP:
                        index++;
                }

                if (index < 0) {
                    index = vb.getChildren().size() - 1;
                }
                index %= vb.getChildren().size();

                System.out.println("Select <" + index + ">");

                return vb.getChildren().get(index);
            }

            @Override
            public Node selectFirst(TraversalContext context) {
                return vb.getChildren().get(0);
            }

            @Override
            public Node selectLast(TraversalContext context) {
                return vb.getChildren().get(vb.getChildren().size() - 1);
            }
            
        };
        ParentTraversalEngine engine = new ParentTraversalEngine(vb, algo);
        vb.setImpl_traversalEngine(engine);

        vb.getChildren().addAll(button1, button2, button3);
        return vb;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
