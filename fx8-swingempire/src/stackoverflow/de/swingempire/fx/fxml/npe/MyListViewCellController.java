/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author blj0011
 */
public class MyListViewCellController extends HBox
{
    @FXML
    private Label cellLblTitle;
    @FXML
    private TextField cellTfBibId, cellTfPoNumber, cellTfUrl, cellTfUsername, cellTfPassword;
    @FXML
    private CheckBox cellCbCounter, cellCbStatsOnly, cellCbOtherStats;
    @FXML
    private ListView<Note> cellListView;

    public MyListViewCellController()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MyListViewCell.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void setData(MainListViewCellData mainListViewCellData)
    {
        cellLblTitle.setText(mainListViewCellData.getTitle());
        cellTfBibId.setText(mainListViewCellData.getBibId());
        cellTfPoNumber.setText(mainListViewCellData.getPoNumber());
        cellTfUrl.setText(mainListViewCellData.getUrl());
        cellTfUsername.setText(mainListViewCellData.getUsername());
        cellTfPassword.setText(mainListViewCellData.getPassword());
        cellCbCounter.setSelected(mainListViewCellData.isHasCounter());
        cellCbStatsOnly.setSelected(mainListViewCellData.isStatsOnly());
        cellCbOtherStats.setSelected(mainListViewCellData.isHasOtherStats());
        cellListView.setCellFactory(t -> new NoteCell());

        if (!mainListViewCellData.getNotes().isEmpty()) {
            cellListView.getItems().setAll(mainListViewCellData.getNotes());
        }
    }
}

