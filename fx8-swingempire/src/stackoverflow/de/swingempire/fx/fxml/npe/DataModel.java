/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataModel
{
    public DataModel()
    {
    }

    public ObservableList<MainListViewCellData> getMainListViewData()
    {
        ObservableList<MainListViewCellData> observableList = FXCollections.observableArrayList();

        String title = "title";
        String username = "username";
        String password = "password";
        String url = "www.helloworld.com/";
        String bibId = "bibId";
        String poNumber = "poNumber";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            MainListViewCellData mainListViewCellData = new MainListViewCellData(i, title + i, username + i, password + i, url + i, bibId + i, poNumber + i,
                    random.nextInt(2) == 1,
                    random.nextInt(2) == 1,
                    random.nextInt(2) == 1);
            List<Note> notes = new ArrayList<>();
            notes.add(new Note(random.nextInt(), i, "note title " + i, "note text " + 1));
            mainListViewCellData.setNotes(notes);
            observableList.add(mainListViewCellData);
        }

        return observableList;
    }
}

