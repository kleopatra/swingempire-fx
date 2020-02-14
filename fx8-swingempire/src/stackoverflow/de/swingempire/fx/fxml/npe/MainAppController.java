/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class MainAppController implements Initializable
{
    private DataModel model;

    @FXML
    ListView<MainListViewCellData> lvMainApp;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // TODO
    }

    public void initModel(DataModel model)
    {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;

        lvMainApp.setCellFactory(t -> new MainListViewCell());
        lvMainApp.setItems(this.model.getMainListViewData());
        //this.model.getMainListViewData().forEach(System.out::println);
    }
}

