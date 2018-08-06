/*
 * Created on 06.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** 
 * change sequence of state
 * https://stackoverflow.com/q/51691721/203657
 * 
 * use-case is physically-check-something:
 * 
 * from physically-unchecked (checkBox.unselected) to
 *    either physically-checked-and-okay (checkBox.selected) - majority of cases
 *    or     physically-checked-and-bad  (checkBox.indeterminate) - minority
 * 
 * Could do by subclassing and overriding fire (and getting the logic right, not
 * done here ;)
 * But: logic of rl-model doesn't quite fit the logic of the tristate
 * more natural: physically-unchecked == checkbox.indeterminate (initial)
 *    either physically-checked-and-okay (checkBox.selected) - majority of cases
 *    or     physically-checked-and-bad  (checkBox.unselected) - minority
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class CheckBoxStateCycling extends Application {

    private Parent createContent() {
        // normal: checked -> unchecked -> indeterminate
        CheckBox box = new CheckBox("Normal Cycling ...");
        box.setAllowIndeterminate(true);
        
        CheckBox normalSelected = new CheckBox("selected of normal");
        normalSelected.selectedProperty().bindBidirectional(box.selectedProperty());
        CheckBox normalIndeterminate = new CheckBox("indeterminate of normal");
        normalIndeterminate.selectedProperty().bindBidirectional(box.indeterminateProperty());
        
        HBox normal = new HBox(10, box, normalSelected, normalIndeterminate);
        
        // required: checked -> indeterminated -> unchecked
        CheckBox tweaked = new CheckBox("Tweaked Cycling ...") {

            @Override
            public void fire() {
                if (isDisabled()) return;
                if (isAllowIndeterminate()) {
                    if (!isSelected()) {
                        setSelected(true);
                        setIndeterminate(false);
                    } else if (isIndeterminate()) {
                        setSelected(false);
                        setIndeterminate(false);
                    } else {
                        setIndeterminate(true);
                    }
                } else {
                    setSelected(!isSelected());
                    setIndeterminate(false);
                }
                fireEvent(new ActionEvent());
            }
            
        };
        tweaked.setAllowIndeterminate(true);
        
        VBox pane = new VBox(10, normal, tweaked);
        return pane;
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
            .getLogger(CheckBoxStateCycling.class.getName());

}
