/*
 * Created on 20.01.2020
 *
 */
package de.swingempire.fx.scene.control.selection;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/59820422/203657
 * another variant of not showing an uncontained value correctly, here it misses the
 * graphic
 * 
 * culprit is skin.updateDisplayNodeText plus some lazy skin initialization (which
 * initializes the displayArea in computeWidth or so)
 * 
 * answered, but OP states the line isn't showing ... no idea why not, waiting
 * for further feedback
 * 
 * its a variant of a known bug (reported by me ;)
 * https://bugs.openjdk.java.net/browse/JDK-8221722
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboUncontainedValue extends Application {

    enum Shapes{
        Circle, Square, Empty;
    }
    
    @Override
    public void start(Stage stage) {
        ComboBox<Shapes> shapeBox = new ComboBox<>();
        shapeBox.getItems().add(Shapes.Circle);
        shapeBox.getItems().add(Shapes.Square);
        //shapeBox.getItems().add(Shapes.Empty);
        shapeBox.setValue(Shapes.Empty);
        // the original cell
        // initially, showing only the text and no graphic
        // after selecting and clear, showing nothing
        shapeBox.setButtonCell(new ShapeListCell());
        shapeBox.setCellFactory(param-> new ShapeListCell());
        // tweaked cell
//        shapeBox.setButtonCell(new ShapeListCell2(shapeBox));
//        shapeBox.setCellFactory(param-> new ShapeListCell2());
//        shapeBox.valueProperty().addListener((src, ov, nv) -> {
//            // at this point the cell is not yet updated
//            System.out.println("value chanded: " + nv + " buttoncell: " + buttonCell.getText() + buttonCell.getGraphic());
//        });


        Button clearBtn = new Button("Clear selection");
        clearBtn.setOnAction(e->shapeBox.setValue(Shapes.Empty));
        
        Button circle = new Button("Circle");
        circle.setOnAction(e -> shapeBox.setValue(Shapes.Circle));
        HBox root = new HBox(shapeBox,clearBtn, circle);
        root.setSpacing(10);
        Scene scene = new Scene(root, 400,200);
        stage.setScene(scene);
        stage.show();
        
    }

    public class ShapeListCell2 extends ListCell<Shapes> {
        double r = 10;
        Circle circle = new Circle(r, r, r);
        Line line = new Line(0, r, r*2, r);
        Rectangle rect = new Rectangle(r*2, r*2);
        ComboBox<Shapes> combo;
        InvalidationListener gl = obs -> graphicUpdated();
        
        /**
         * Use this constructor in the combo's cellFactory.
         */
        public ShapeListCell2() {
            this(null);
        }
        
        /**
         * Use this constructor when being the button cell.
         * @param combo
         */
        public ShapeListCell2(ComboBox combo) {
            this.combo = combo;
            if (isButtonCell()) {
                // initialize with empty text/graphic
                resetButtonCell();
                // register listener to reset on first nulling by skin
                graphicProperty().addListener(gl);
            }
        }

        private void graphicUpdated() {
            // remove listener
            graphicProperty().removeListener(gl);
            resetButtonCell();
        }
        
        protected void resetButtonCell() {
            setText(Shapes.Empty.toString());
            setGraphic(line);
        }

        protected boolean isButtonCell() {
            return combo != null;
        }
        
        @Override
        public void updateItem(Shapes item, boolean empty) {
            super.updateItem(item, empty);

            // special case: buttonCell with uncontained value
            if (isButtonCell() && getIndex() < 0 && combo.getValue() != null) {
                resetButtonCell();
                return;
            }
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                switch (item) {
                case Circle:
                    setGraphic(circle);
                    break;
                case Empty:
                    setGraphic(line);
                    break;
                case Square:
                    setGraphic(rect);
                    break;
                }
            }
        }
    }
    
    // original cell
    private class ShapeListCell extends ListCell<Shapes> {
        double r = 10;
        @Override
        public void updateItem(Shapes item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
                
            } else {
                setText(item.toString());

                switch (item) {
                case Circle:
                    setGraphic(new Circle(r, r, r));
                    break;
                case Empty:
                    setGraphic(new Line(0, r, r*2, r));
                    break;
                case Square:
                    setGraphic(new Rectangle(r*2, r*2));
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
