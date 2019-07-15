/*
 * Created on 15.07.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Random;

import de.swingempire.fx.scene.control.selection.SelectionAndModification;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57034428/203657
 * selection cleared on replace item: while this example is too complex - seems
 * to update many/all persons - still virulent in SelectionAndModification
 * 
 * @see SelectionAndModification
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableSelectionOnUpdate extends Application
{

  private TableView<Person> table = new TableView<Person>();

  @Override
  public void start(Stage primaryStage) throws Exception
  {
    Scene scene = new Scene(new Group());
    primaryStage.setTitle("Table View Sample");
    primaryStage.setWidth(450);
    primaryStage.setHeight(500);

    final Label label = new Label("Address Book");
    label.setFont(new Font("Arial", 20));

    table.setEditable(true);

    TableColumn<Person, Integer> IDCol = new TableColumn<>("ID");
    IDCol.setMinWidth(100);
    IDCol.setCellValueFactory(new PropertyValueFactory<Person, Integer>("ID"));

    TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
    lastNameCol.setMinWidth(100);
    lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));

    table.getColumns().add(IDCol);
    table.getColumns().add(lastNameCol);

    final VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    vbox.getChildren().addAll(label, table);

    ((Group) scene.getRoot()).getChildren().addAll(vbox);

    primaryStage.setScene(scene);
    primaryStage.show();

    addDataToTable();
  }

  public static void main(String[] args)
  {
    launch(args);
  }

  private void addDataToTable()
  {    
    //If the table has data in it, then we must check if the ID already exist, so that we can replace it.
    //else, we just add the items to the table in the first run.
    Runnable runnable = new Runnable()
    {

      @Override
      public void run()
      {
        while(true)
        {
          ObservableList<Person> data = generateData();
          try
          {
            Thread.sleep(2000);
          } catch (InterruptedException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          if(table.getItems().size() > 0)
          {
            //first we cycle through the data in the generated list
            //then we cycle through the data in the table itself.
            //if the data is found, we replace it, and break from the loop.
            for(int i = 0; i < data.size(); i++)
            {
              for(int j = 0; j < table.getItems().size(); j++)
              {
                Person newPerson = data.get(i);
                Person currentPerson = table.getItems().get(j);

                if(newPerson.getID() == currentPerson.getID())
                {
                  final int J = j;
                  //now we replace it if it is the same
                  Platform.runLater(new Runnable()
                  {

                    @Override
                    public void run()
                    {
                      table.getItems().set(J, newPerson);
                      System.out.println("new: " + J + "/" + newPerson);
                    }
                  });

                  break;
                }
              }
            }
          }
          else
          {
            //When modifying the data on the table,w e do it on the platform thread, 
            //to avoid any concurrent modification exceptions.
            Platform.runLater(new Runnable()
            {

              @Override
              public void run()
              {
                table.getItems().addAll(data);
              }
            });
          }
        }
      }
    };

    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
  }

  private ObservableList<Person> generateData()
  {
    ObservableList<Person> values = FXCollections.observableArrayList();

    for(int i = 0; i < 100; i++)
    {
      Person oPerson = new Person(i, randomStringGeneerator());
      values.add(oPerson);
    }

    return values;
  }

  private String randomStringGeneerator()
  {
      String[] sample = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".split("");
      String result = "";
      Random oRandom = new Random();

      int length = oRandom.nextInt(10) + 5;
      for(int i = 0 ; i < length; i++)
      {
        result += sample[oRandom.nextInt(sample.length)];
      }

      return result;
  }  
  
  public class Person
  {
    private final SimpleIntegerProperty ID;
    private final SimpleStringProperty lastName;

    public Person(int ID, String lName)
    {
      this.ID = new SimpleIntegerProperty(ID);
      this.lastName = new SimpleStringProperty(lName);
    }

    public int getID()
    {
      return this.ID.get();
    }

    public void setID(int ID)
    {
      this.ID.set(ID);
    }

    public String getLastName()
    {
      return this.lastName.get();
    }

    public void setLastName(String lName)
    {
      this.lastName.set(lName);
    }

    @Override
    public String toString()
    {
      return "ID: " + ID + " value: " + lastName;
    }
  }


}