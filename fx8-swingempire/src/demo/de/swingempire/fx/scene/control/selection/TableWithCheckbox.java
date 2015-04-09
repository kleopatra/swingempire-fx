/*
 * Created on 02.04.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Might be slightly more to it than the usage error (clearing selection in 
 * selection listener) no need to dig into it, though
 * http://stackoverflow.com/q/29386766/203657
 */
public class TableWithCheckbox extends Application {

    final TableView<Player> table = new TableView<Player>();

    ObservableList<Player> data;

    @Override
    public void start(Stage primaryStage) {
        final BorderPane root = new BorderPane();

        table.setItems(createData());
        final TableColumn<Player, String> firstNameColumn = new TableColumn<>(
                "First Name");
        final TableColumn<Player, String> lastNameColumn = new TableColumn<>(
                "Last Name");
        final TableColumn<Player, Boolean> selectedColumn = new TableColumn<>(
                "selected");
        firstNameColumn
                .setCellValueFactory(new PropertyValueFactory<Player, String>(
                        "firstName"));
        lastNameColumn
                .setCellValueFactory(new PropertyValueFactory<Player, String>(
                        "lastName"));
        selectedColumn
                .setCellValueFactory(new PropertyValueFactory<Player, Boolean>(
                        "selected"));
        final Callback<TableColumn<Player, Boolean>, TableCell<Player, Boolean>> cellFactory = CheckBoxTableCell
                .forTableColumn(selectedColumn);
        selectedColumn
                .setCellFactory(new Callback<TableColumn<Player, Boolean>, TableCell<Player, Boolean>>() {
                    @Override
                    public TableCell<Player, Boolean> call(
                            TableColumn<Player, Boolean> column) {
                        TableCell<Player, Boolean> cell = cellFactory
                                .call(column);
                        cell.setAlignment(Pos.CENTER);
                        return cell;
                    }
                });
        selectedColumn.setCellFactory(cellFactory);
        selectedColumn.setEditable(false);

        table.setEditable(true);
        table.getColumns().addAll(selectedColumn, firstNameColumn,
                lastNameColumn);

        root.setCenter(table);

        Button button1 = new Button("add");
        button1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                data.add(new Player("hbghj", "hjhbbccc77", false));
            }
        });

        root.setTop(button1);
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ObservableList<Player> createData() {
        List<Player> players = Arrays.asList(
                new Player("Hugo", "Lloris", true), new Player("Brad",
                        "Friedel", false),
                new Player("Kyle", "Naughton", false), new Player("Younes",
                        "Kaboul", false), new Player("Benoit", "Assou-Ekotto",
                        false), new Player("Jan", "Vertonghen", false),
                new Player("Michael", "Dawson", false), new Player("William",
                        "Gallas", false), new Player("Kyle", "Walker", false),
                new Player("Scott", "Parker", false), new Player("Mousa",
                        "Dembele", false), new Player("Sandro", "Cordeiro",
                        false), new Player("Tom", "Huddlestone", false),
                new Player("Gylfi", "Sigurdsson", false), new Player("Gareth",
                        "Bale", false), new Player("Aaron", "Lennon", false),
                new Player("Jermane", "Defoe", false), new Player("Emmanuel",
                        "Adebayor", false));
        data = FXCollections
                .<Player> observableArrayList(
                        // need the extractor so that listeners to the data are notified
                        // (don't need for the table to update itself, though)
                        new Callback<Player, Observable[]>() {
                    @Override
                    public Observable[] call(Player player) {
                        return new Observable[] { player.selectedProperty() };
                    }
                }
    );
        data.addAll(players);
        data.addListener(new ListChangeListener<Player>() {
            @Override
            public void onChanged(
                    javafx.collections.ListChangeListener.Change<? extends Player> change) {
                System.out.println("List changed");
                while (change.next()) {
                    if (change.wasUpdated()) {
                        System.out.println("What code a do here");
                        System.out.println();
                    }
                }
            }
        });
        table.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener() {
                    @Override
                    public void changed(ObservableValue observableValue,
                            Object oldValue, Object newValue) {

                        Player currPlayer = (Player) newValue;
                        // need to handle null, as we get a null
                        // after having cleared the selection
                        System.out.println("in changed: " + oldValue + " / "
                                + newValue);
                        if (currPlayer == null)
                            return;
                        // table.getSelectionModel()
                        // .getSelectedItem();
                        Platform.runLater(() -> {
                            table.getSelectionModel().clearSelection();

                            for (Player pl : players) {

                                pl.setSelected(false);

                            }

                            currPlayer.setSelected(true);

                        });
                    }
                });
        return data;
    }

    public static class Player {

        private final StringProperty firstName;

        private final StringProperty lastName;

        private final BooleanProperty selected;

        Player(String firstName, String lastName, boolean international) {
            this.firstName = new SimpleStringProperty(this, "firstName",
                    firstName);
            this.lastName = new SimpleStringProperty(this, "lastName", lastName);
            this.selected = new SimpleBooleanProperty(this, "selected",
                    international);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public StringProperty firstNameProperty() {
            return firstName;
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public StringProperty lastNameProperty() {
            return lastName;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean international) {
            this.selected.set(international);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        @Override
        public String toString() {
            return firstName.get() + " " + lastName.get()
                    + (selected.get() ? " (injured)" : "");
        }
    }
}
