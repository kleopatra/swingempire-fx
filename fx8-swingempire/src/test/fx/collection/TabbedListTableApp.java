/*
 * Created on 02.06.2013
 *
 */
package fx.collection;

import de.swingempire.fx.demobean.Person;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.ListView;
import javafx.scene.control.ListViewBuilder;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPaneBuilder;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TableViewBuilder;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TabbedListTableApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane tablePane = BorderPaneBuilder.create() 
           .center(createTableView())     
           .build();
        BorderPane list = BorderPaneBuilder.create() 
           .center(createListView())     
           .build();
        TabPane tabs = TabPaneBuilder.create()
           .tabs(
              TabBuilder.create()
                  .text("Table")
                  .content(tablePane)
                  .build(),
              TabBuilder.create()
                  .text("list")
                  .content(list)
                  .build()
            )     
           .build();
        Scene scene = SceneBuilder.create()
           .root(tabs)     
           .build();  
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @return
     */
    private Node createListView() {
        ListView list = ListViewBuilder.create()
            .items(getTeamMembers()) 
            .build();
        return list;
    }

    /**
     * @return
     */
    private Node createTableView() {
        TableView table = TableViewBuilder.create()
                .columns(
                  TableColumnBuilder.create()
                    .text("First Name")
                    .cellValueFactory(new PropertyValueFactory("firstName"))
                    .prefWidth(180)
                    .build(),
                  TableColumnBuilder.create()
                    .text("Last Name")
                    .cellValueFactory(new PropertyValueFactory("lastName"))
                    .prefWidth(180)
                    .build(),
                  TableColumnBuilder.create()
                    .text("Phone Number")
                    .cellValueFactory(new PropertyValueFactory("phone"))
                    .prefWidth(150)
                    .build()
                )
                .items(getTeamMembers())
                .build();
              
              table.getSelectionModel().selectedItemProperty()
                                          .addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue,
                                    Object newValue) {
                  Person selectedPerson = (Person)newValue;
                  System.out.println(selectedPerson + " chosen in TableView");
                }
              });
              return table;
    }

    public ObservableList getTeamMembers() {
        ObservableList teamMembers = FXCollections.observableArrayList();
        for (int i = 1; i <= 10; i++) {
          teamMembers.add(new Person("FirstName" + i,
                                     "LastName" + i,
                                     "Phone" + i));
        }
        return teamMembers;
      }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
