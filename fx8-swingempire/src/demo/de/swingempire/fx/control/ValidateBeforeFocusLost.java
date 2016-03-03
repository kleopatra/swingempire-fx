/*
 * Created on 22.09.2015
 *
 */
package de.swingempire.fx.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalContext;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Ideas on validate-on-focusLost.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ValidateBeforeFocusLost extends Application {


    /** Ideally, we would either extend a given algorithm (cant because the constructors 
     * are package private) or wrap the default algorithm (can't because
     * there's no getter in TraversalEngine. 
     * 
     * Actually, don't need this: if we validate on focuslost (and request back)
     * that's enough 
     */ 
    public static class ValidatingAlgorithm implements Algorithm {

        private HashMap<Node, Predicate<Node>> rules;

        public ValidatingAlgorithm(Map<Node, Predicate<Node>> rules) {
            this.rules = new HashMap<>(rules);
        }
        @Override
        public Node select(Node node, Direction dir, TraversalContext context) {
            if (!isValid(node)) {
                // returning null will disable navigation inside the parent
                // (but not outside, focus will move to parent's sibling if any)
                return null;
            }
            Node next = doTrav(node, dir, context.getRoot());
            return next;
        }
        
         private boolean isValid(Node node) {
            Predicate rule = rules.get(node);
            return rule != null ? rule.test(node) : true;
        }

         // NOTE: this is the wrong thingy-to-do! we need to walk the
         // tree until we find a suitable (aka: focusable!) node
         private Node doTrav(Node node, Direction drctn, Parent parent) {
            int index = parent.getChildrenUnmodifiable().indexOf(node);

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
                index = parent.getChildrenUnmodifiable().size() - 1;
            }
            index %= parent.getChildrenUnmodifiable().size();

            System.out.println("Select <" + index + ">");

            return parent.getChildrenUnmodifiable().get(index);
        }

        /**
         * PENDING JW: need to implement check!
         */
        @Override
        public Node selectFirst(TraversalContext context) {
            Node current = context.getRoot().getScene().getFocusOwner();
            if (!isValid(current)) {
                return null;
            }
            List<Node> vb = context.getRoot().getChildrenUnmodifiable();
            return vb.get(0);
        }

        @Override
        public Node selectLast(TraversalContext context) {
            Node current = context.getRoot().getScene().getFocusOwner();
            if (!isValid(current)) {
                return null;
            }
            List<Node> vb = context.getRoot().getChildrenUnmodifiable();
            return vb.get(vb.size() - 1);
        }
        
    }
    
    @SuppressWarnings("deprecation")
    private Parent getContent() {
        int minLength = 10;
        TextField field = new TextField("must have length > " + minLength);
        TextField other = new TextField("initial invalid! length < " + minLength );
        ObservableList<String> items = FXCollections.observableArrayList("One",
                "Two", "All");
        ComboBox<String> comboBox = new ComboBox(items);
        comboBox.setValue("tooooooo long");
        comboBox.setEditable(true);
        
        // a map of validation rules
        Map<Node, Predicate<Node>> rules = new HashMap<>();
        rules.put(field, f -> ((TextField) f).getText().length() > minLength);
        rules.put(other, f -> ((TextField) f).getText().length() < minLength);
//        rules.put(comboBox, f -> ((ComboBox<String>) f).getEditor().getText().length() < minLength);
        // validating on the value doesn't work: the value is not yet committed on focuslost
        // https://bugs.openjdk.java.net/browse/JDK-8151129
        rules.put(comboBox, f -> ((ComboBox<String>) f).getValue().length() < minLength);
        
        installValidationListeners(field, rules);
        installValidationListeners(other, rules);
        installValidationListeners(comboBox, rules);
        VBox grid = new VBox(10, field, other, comboBox);
        // use validating traversalEngine in parent
//        grid.setImpl_traversalEngine(new ParentTraversalEngine(grid,
//                new ValidatingAlgorithm(rules)));
        return grid;
    }

    private void installValidationListeners(Node field,
            Map<Node, Predicate<Node>> rules) {
        // use rule in focusListener and request focus back if not valid
        field.focusedProperty().addListener((src, ov, nv) -> {
            if (!nv) {
                Predicate rule = rules.get(field);
                if (rule != null && !rules.get(field).test(field)) {
                    field.requestFocus();
                }
            }
        });
        // if valid travers to next node
        EventHandler<ActionEvent> handler = e -> {
            Predicate rule = rules.get(field);
            if (rule == null || rule.test(field)) {
                field.impl_traverse(Direction.NEXT);
            }
        };
        if (field instanceof TextField) {
            ((TextField) field).setOnAction(handler);
        } else if (field instanceof ComboBoxBase) {
            ((ComboBoxBase) field).setOnAction(handler);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 600, 600));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ValidateBeforeFocusLost.class.getName());
}
