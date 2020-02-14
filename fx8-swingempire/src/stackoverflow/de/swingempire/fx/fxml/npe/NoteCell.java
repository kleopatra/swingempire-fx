/*
 * Created on 13.02.2020
 *
 */
package de.swingempire.fx.fxml.npe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

/**
*
* @author blj0011
*/
public class NoteCell extends ListCell<Note>
{
   TextArea textArea = new TextArea();
   TitledPane titledPane = new TitledPane("", textArea);
   ObservableList<Note> observableList = FXCollections.observableArrayList();
   Button button;
   
   public NoteCell() {
//       setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//       setGraphic(titledPane);
   }
   @Override
   public void updateItem(Note item, boolean empty)
   {
       titledPane.setExpanded(false);
       super.updateItem(item, empty);
       if (item == null || empty) {
           setText(null);
           setGraphic(null);
       }
       else {
//           titledPane.setExpanded(false);
           titledPane.setText(item.getTitle());
           textArea.setText(item.getText());
           titledPane.setAnimated(false);
           setGraphic(titledPane);
       }
   }
}

