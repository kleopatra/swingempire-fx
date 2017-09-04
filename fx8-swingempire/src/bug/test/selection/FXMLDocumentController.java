/*
 * Created on 29.08.2017
 *
 */
package test.selection;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;


public class FXMLDocumentController implements Initializable {

    List<SomeData> dataList = new ArrayList();
    private ObservableList<SomeData> observedList = FXCollections.observableArrayList(dataList); 

    @FXML
    private Button updatButton;
    @FXML
    private TableView myTable;       

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for(int i = 0; i < 8; i++){
            SomeData newData = new SomeData();
            newData.setID(i);
            newData.setStatus(0);
            observedList.add(newData);
        }
        myTable.setItems(observedList);
        myTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }    

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("Update table!");
        Integer index = 5;
        SomeData newData = new SomeData();
        newData.setID(index);
        newData.setStatus(observedList.get(index).getStatus() + 1);
        observedList.set(index, newData);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
            }
        });        
    }
    
    public static class SomeData {
        private int ID;
        private int status;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

    }


}

