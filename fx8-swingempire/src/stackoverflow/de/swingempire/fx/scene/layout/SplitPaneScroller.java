/*
 * Created on 19.04.2020
 *
 */
package de.swingempire.fx.scene.layout;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class SplitPaneScroller implements Initializable {

    @FXML
    SplitPane splitPane;
    @FXML TableView tableView;
    @FXML
    Region last;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        splitPane.setDividerPositions(0d,.33d, .66d,1d);
        last.setMaxHeight(Region.USE_PREF_SIZE);
    }
    
    @FXML
    private void print() {
        Pane parent = (Pane) tableView.getParent();
        System.out.println(
                "table height/pref: " + tableView.getHeight() + " / "
            + tableView.prefHeight(-1)
            + "\n"
            +  "parent height/pref: " + parent.getHeight() + " / "
            + parent.prefHeight(-1)
            + "\n"
          +  "splitpane height/pref: " + splitPane.getHeight() + " / "
        + splitPane.prefHeight(-1)
        + "\n"
                );

    }

}