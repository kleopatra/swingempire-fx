/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import javafx.scene.control.ListCell;

/**
 *
 * @author blj0011
 */
public class MainListViewCell extends ListCell<MainListViewCellData> {
    static int creationCount;
    int cellMarker;
    int reuseCount;
    MyListViewCellController myListViewCellController = new MyListViewCellController();

    public MainListViewCell() {
        cellMarker = creationCount;
        System.out.println("main cell created: " + creationCount++);
    }
    @Override
    public void updateItem(MainListViewCellData item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            myListViewCellController.setData(item);
            setGraphic(myListViewCellController);
        }
        System.out.println("main cell " + cellMarker + " reused: " + reuseCount++);
    }
}