/*
 * Created on 01.10.2015
 *
 */
package control.skin;

import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * TableCellBehavior: NPE on mouse interaction.
 * 
 * Happens if the cell is added outside of a tableView. 
 * Error is in TableCellBehavior: several methods assume that
 * getCellContainer != null and access its properties. 
 */
public class TableCellAndRowBug extends Application {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void start(Stage stage) throws Exception {
        FlowPane flowPane = new FlowPane(10, 10);
         TableCellBehavior b;
        TableCell tableCell = new TableCell() {

            @Override
            protected void updateItem(Object arg0, boolean arg1) {
                super.updateItem(arg0, arg1);
                setText("detached TableCell");
            }

        };
        tableCell.setMinWidth(100);
        // just to see where to click
        tableCell.updateIndex(-1);

        flowPane.getChildren().addAll(tableCell);

        Scene scene = new Scene(flowPane, 500, 500);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param circleRadius
     * @param listCell
     * @return
     */
    protected EventHandler<? super MouseEvent> createHandler(
            double circleRadius, Node listCell) {
        return mouseEvent -> {
//            circle.relocate(mouseEvent.getSceneX(), mouseEvent.getSceneY());
            listCell.setTranslateX(mouseEvent.getSceneX() - circleRadius);
            listCell.setTranslateY(mouseEvent.getSceneY() - circleRadius);
        };
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellAndRowBug.class.getName());
}