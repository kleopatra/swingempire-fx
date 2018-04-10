/*
 * Created on 09.04.2018
 *
 */
package control.cell;

import java.time.LocalDate;
import java.time.Month;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8201285 
 * STEPS TO FOLLOW TO REPRODUCE
 * THE PROBLEM : 
 * 1) Start the test application 
 * 2) Display the calendar popup 
 * 3) Last month click 
 * 4) Next month click 
 * 5) Next month click
 * 
 * EXPECTED VERSUS ACTUAL BEHAVIOR : 
 * EXPECTED - The text color of DateCell is
 * displayed correctly 
 * ACTUAL - Some DateCell text colors are grayed out
 * 
 * virulent in fx10 > +14
 * 
 * given up digging in fx10 - all collaborators seem to be unchanged
 */
public class DateCellDisableTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        DatePicker datePicker = new DatePicker();

        datePicker.setDayCellFactory((cell) -> {
            return new DateCell() {
                @Override
                public void updateItem(final LocalDate item,
                        final boolean empty) {
                    super.updateItem(item, empty);

                    if (item.isBefore(LocalDate.now())) {
                        this.setDisable(true);
                    }
                    if (item.getMonth() == Month.APRIL)
                        System.out.println("item: " + item + " /mine: " + isDisable() + " /all " + isDisabled());
                }
            };
        });

        BorderPane root = new BorderPane(datePicker);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}
