/*
 * Created on 21.07.2019
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/57130588/203657
 * value not committed after editing? worksforme - but not entirely clear
 * on the OPs expectation, talks about keyReleased.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCommitHandler extends Application {

    private Parent createContent() {
        ObservableList<Tours> data = FXCollections.observableArrayList(
                new Tours("dummy1", "some", "10")
                ,new Tours("dummy1", "some", "10")
                ,new Tours("dummy1", "some", "10")
                ,new Tours("dummy1", "some", "10")
                );
        
        
        TableView<Tours> table = new TableView<Tours>(data);
        table.setEditable(true);
        TableColumn<Tours, String> hotelCol = new TableColumn<Tours, String>("Hotel");
        TableColumn<Tours, String> priceCol = new TableColumn<Tours, String>("Price per Day ");
        TableColumn<Tours, String> tournumberCol = new TableColumn<Tours, String>("Tour No.");

        hotelCol.setCellValueFactory(new PropertyValueFactory<>("hotelname"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        tournumberCol.setCellValueFactory(new PropertyValueFactory<>("tournumber"));


        tournumberCol.setEditable(true);
        tournumberCol.setCellFactory(TextFieldTableCell.<Tours>forTableColumn());

        tournumberCol.setOnEditCommit(
         (TableColumn.CellEditEvent<Tours, String> t) ->
         (t.getTableView().getItems().get(t.getTablePosition().getRow())).setTournumber(t.getNewValue()));
        table.getColumns().addAll(hotelCol,priceCol,tournumberCol);

        VBox root = new VBox(table);
        root.setPadding(new Insets(5));
        root.setAlignment(Pos.CENTER);
         root.setSpacing(20);

        return root;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCommitHandler.class.getName());

    
    public class Tours {
        private String hotelname;
        private String price;
        private String tournumber;
        public Tours(String hotelname, String price, String tournumber) {
            super();
            this.hotelname = hotelname;
            this.price = price;
            this.tournumber = tournumber;
        }
        public String getHotelname() {
            return hotelname;
        }
        public void setHotelname(String hotelname) {
            this.hotelname = hotelname;
        }
        public String getPrice() {
            return price;
        }
        public void setPrice(String price) {
            this.price = price;
        }
        public String getTournumber() {
            return tournumber;
        }
        public void setTournumber(String tournumber) {
            this.tournumber = tournumber;
        }
    }    
}
