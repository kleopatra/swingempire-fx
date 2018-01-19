/*
 * Created on 19.01.2018
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DisableSelectionOnInvalidEntry extends Application {

//    /**
//    *
//    * @author Sedrick
//    */
//   public class FXMLDocumentController implements Initializable {
//
////       @FXML 
       TextField tfFirstName, tfLastName;
//       @FXML 
       TableView<Person> tvPerson;
//       @FXML 
       TableColumn<Person, String> tcFirstName, tcLastName;
       
       BorderPane content;

       final String firstNames = "Darryl  \n" +
                                   "Enriqueta  \n" +
                                   "Katherine  \n" +
                                   "Harley  \n" +
                                   "Arlean  \n" +
                                   "Jacquelynn  \n" +
                                   "Yuko  \n" +
                                   "Dion  \n" +
                                   "Vivan  \n" +
                                   "Carly  \n" +
                                   "Eldon  \n" +
                                   "Joe  \n" +
                                   "Klara  \n" +
                                   "Shona  \n" +
                                   "Delores  \n" +
                                   "Sabra  \n" +
                                   "Vi  \n" +
                                   "Gearldine  \n" +
                                   "Laine  \n" +
                                   "Lila  ";

      final String lastNames = "Ollie  \n" +
                                   "Donnette  \n" +
                                   "Audra  \n" +
                                   "Angelica  \n" +
                                   "Janna  \n" +
                                   "Lekisha  \n" +
                                   "Michael  \n" +
                                   "Tomi  \n" +
                                   "Cheryl  \n" +
                                   "Roni  \n" +
                                   "Aurelio  \n" +
                                   "Mayola  \n" +
                                   "Kelsie  \n" +
                                   "Britteny  \n" +
                                   "Dannielle  \n" +
                                   "Kym  \n" +
                                   "Scotty  \n" +
                                   "Deloris  \n" +
                                   "Lavenia  \n" +
                                   "Sun  \n";

//       @Override
       public void initialize(URL url, ResourceBundle rb) {
           tfFirstName = new TextField();
           tfLastName =  new TextField();
           tcFirstName = new TableColumn<>("firstName");
           tcLastName = new TableColumn<>("Last Name");
           tvPerson =  new TableView<>();
           tvPerson.getColumns().addAll(tcFirstName, tcLastName);
           tcFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
           tcLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

           
           tvPerson.setItems(FXCollections.observableArrayList(getPersons()));
           new VetoableSelection<>(tvPerson);
           
           tvPerson.getSelectionModel().selectedItemProperty().addListener((obs, oldPerson, newPerson)->{

               LOG.info("selectedItem: " + oldPerson + "/ " + newPerson);
//             new RuntimeException("who ist calling?? \n").printStackTrace();

//               if(!validateTextFields())
//               {
//                   tvPerson.getSelectionModel().select(oldPerson);
//                   return;
//               }

               if(newPerson != null)
               {
                   tfFirstName.setText(newPerson.getFirstName());
                   tfLastName.setText(newPerson.getLastName());
               }
           });

           tfFirstName.setOnKeyReleased(keyEvent ->{
               Person tempPerson = tvPerson.getSelectionModel().getSelectedItem();
               if(!tfFirstName.getText().trim().equals(tempPerson.getFirstName().trim()))
               {
                   tfFirstName.setStyle("-fx-control-inner-background: red;");
                   ((VetoableSelection<Person>) tvPerson.getSelectionModel()).setDisabled(true);
               }
           });

           tfFirstName.setOnAction(actionEvent ->{
               Person tempPerson = tvPerson.getSelectionModel().getSelectedItem();
               tempPerson.setFirstName(tfFirstName.getText().trim());

               tfFirstName.setStyle(null);
               ((VetoableSelection<Person>) tvPerson.getSelectionModel()).setDisabled(false);

           });       

           tfLastName.setOnKeyReleased(keyEvent ->{
               Person tempPerson = tvPerson.getSelectionModel().getSelectedItem();
               if(tfLastName.getText().trim().equals(tempPerson.getLastName().trim()))
               {
                   tfLastName.setStyle("-fx-control-inner-background: red;"); 
                   ((VetoableSelection<Person>) tvPerson.getSelectionModel()).setDisabled(true);

               }
           });

           tfLastName.setOnAction(actionEvent ->{
               Person tempPerson = tvPerson.getSelectionModel().getSelectedItem();
               tempPerson.setLastName(tfLastName.getText().trim());

               tfLastName.setStyle(null);
               ((VetoableSelection<Person>) tvPerson.getSelectionModel()).setDisabled(false);

           });

           
           HBox buttons = new HBox(10, tfFirstName, tfLastName);
           content = new BorderPane(tvPerson);
           content.setBottom(buttons);
       }    

       private boolean validateTextFields()
       {
           if(!tfFirstName.getStyle().isEmpty()){return false;}
           if(!tfLastName.getStyle().isEmpty()){return false;}

           return true;
       }

       List<Person> getPersons()
       {
           List<Person> tempPerson = new ArrayList<>();

           List<String> tempFirstName = Arrays.asList(firstNames.split("\n"));
           List<String> tempLastName = Arrays.asList(lastNames.split("\n"));

           for(int i = 0; i < tempFirstName.size(); i++)
           {
               tempPerson.add(new Person(tempFirstName.get(i).trim(), tempLastName.get(i).trim()));
           }

           return tempPerson;
       }

   

    public static class VetoableSelection<T> extends TableViewSelectionModel<T> {
        
        private boolean disabled;
        private TableViewSelectionModel<T> delegate;
        
        public VetoableSelection(TableView<T> table) {
            super(table);
            delegate = table.getSelectionModel();
            table.setSelectionModel(this);
            new VetoableFocusModel<>(table);
            delegate.selectedIndexProperty().addListener(c -> indexInvalidated());
        }

        /**
         */
        private void indexInvalidated() {
            setSelectedIndex(delegate.getSelectedIndex());
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
        
        public boolean isDisabled() {
            return disabled;
        }
        
        
        @Override
        public void clearAndSelect(int row) {
            if (isDisabled()) return;
            LOG.info("clearselecting: " + row );
           delegate.clearAndSelect(row);
        }

        @Override
        public void select(int row) {
            if (isDisabled()) return;
            LOG.info("selecting: " + row );
            delegate.select(row);
        }

        @Override
        public void clearAndSelect(int row, TableColumn<T, ?> column) {
            if (isDisabled()) return;
            LOG.info("clearselecting: " + row + " / " + column);
//            new RuntimeException("who ist calling?? \n").printStackTrace();
            delegate.clearAndSelect(row, column);
        }
        

        @Override
        public void clearSelection(int row, TableColumn<T, ?> column) {
            if (isDisabled()) return;
            delegate.clearSelection(row, column);
        }

        @Override
        public ObservableList<TablePosition> getSelectedCells() {
            return delegate.getSelectedCells();
        }

        @Override
        public boolean isSelected(int row, TableColumn<T, ?> column) {
            return delegate.isSelected(row, column);
        }

        @Override
        public void select(int row, TableColumn<T, ?> column) {
            if (isDisabled()) return;
            LOG.info("selecting: " + row + " / " + column);
            delegate.select(row, column);
        }

        @Override
        public void selectAboveCell() {
            if (isDisabled()) return;
            delegate.selectAboveCell();
            
        }

        @Override
        public void selectBelowCell() {
            if (isDisabled()) return;
            delegate.selectBelowCell();
        }

        @Override
        public void selectLeftCell() {
            if (isDisabled()) return;
            delegate.selectLeftCell();
        }

        @Override
        public void selectRightCell() {
            if (isDisabled()) return;
            delegate.selectRightCell();
        }

        
    }

    public static class VetoableFocusModel<T> extends TableViewFocusModel<T> {

        boolean disabled;
        TableViewFocusModel<T> delegate;
        /**
         * @param table
         */
        public VetoableFocusModel(TableView<T> table) {
            super(table);
            this.delegate = table.getFocusModel();
            table.setFocusModel(this);
        }
        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
        
        public boolean isDisabled() {
            return disabled;
        }
        
        
    }
    @Override
    public void start(Stage stage) throws Exception {
        initialize(null, null);
        Scene scene = new Scene(content);
        stage.setScene(scene);
        stage.show();

    }
    
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DisableSelectionOnInvalidEntry.class.getName());
}
