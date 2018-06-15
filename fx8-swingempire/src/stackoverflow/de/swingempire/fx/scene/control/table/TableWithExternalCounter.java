/*
 * Created on 14.06.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Keep running numbers (in categories) "running" after sorting.
 * 
 * Need some kind of binding? listen to sorted and update? or use a
 * wrapper list somehow?
 * 
 * https://stackoverflow.com/q/50845010/203657
 * @author Jeanette Winzenburg, Berlin
 */
public class TableWithExternalCounter extends Application {

    /**
     * Updates the counter data from the given source list, assuming that
     * both have the same size (if that's not true, adjust counter size
     * as needed)
     */
    private void updateDogCounterFrom(ObservableList<ObjectProperty<Integer>> dogCounter, 
            ObservableList<? extends PetOwner> owners) {
        int count = 0;
        for (int i = 0; i < owners.size(); i++) {
            PetOwner owner = owners.get(i);
            if (owner.petProperty().get() == Pet.DOG) {
                dogCounter.get(i).set(++count);
            } else {
                dogCounter.get(i).set(-1);
            }
        }    
    }
    private Parent createContent() {
        // the base data
        ObservableList<PetOwner> owners = PetOwner.owners();
        // a list for the counters, that must be kept in sync with changes in the table
        ObservableList<ObjectProperty<Integer>> dogCounter = FXCollections.observableArrayList();
        owners.forEach(owner -> dogCounter.add(new SimpleObjectProperty<Integer>(-1)));
        // initial sync
        updateDogCounterFrom(dogCounter, owners);
        
        SortedList<PetOwner> sorted = new SortedList<>(owners);
        sorted.addListener((ListChangeListener<? super PetOwner>) c -> {
            // sync after change
            updateDogCounterFrom(dogCounter, c.getList());
            FXUtils.prettyPrint(c);
        });
        TableView<PetOwner> table = new TableView<>(sorted);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        
        TableColumn<PetOwner, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<PetOwner, Pet> pet = new TableColumn<>("Pet");
        pet.setCellValueFactory(new PropertyValueFactory<>("pet"));
        TableColumn<PetOwner, Integer> dogIndex = new TableColumn<>("Running Dog#");
        dogIndex.setSortable(false);
        dogIndex.setCellValueFactory(cd -> {
            // astonishingly, this is called for every cell after sorting, 
            // that is all cells are newly created
            int index = sorted.indexOf(cd.getValue());
            LOG.info("index/owner/counter" + index + "/" + cd.getValue() + "/" + dogCounter.get(index));
            return dogCounter.get(index);
        });
        dogIndex.setCellFactory(cb -> {
            return new TableCell<PetOwner, Integer>() {

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && item.intValue() < 0) {
                        setText("");
                    } else {
                        setText(String.valueOf(item));
                    }
                }
                
            };
        });
        
        table.getColumns().addAll(name, pet, dogIndex);
        Button debug = new Button("Log");
        debug.setOnAction(e -> {
            for (int i = 0; i < table.getItems().size(); i++) {
                PetOwner owner = table.getItems().get(i);
                LOG.info("index/owner/counter" + i + "/" + owner + "/" + dogCounter.get(i));
            }
        });
        Button refresh = new Button("refresh");
        // had no effect, was wrong counter value!
        refresh.setOnAction(e -> table.refresh());
        Button changeCounter = new Button("force counter");
        // updated?
        changeCounter.setOnAction(e -> dogCounter.get(0).set(100));
        Button updatePet = new Button("toggle pet");
        updatePet.setOnAction(e -> owners.get(10).togglePet());
        
        HBox buttons = new HBox(10, debug, refresh, changeCounter, updatePet);
        
        BorderPane pane = new BorderPane(table);
        pane.setBottom(buttons);
        return pane;
    }
    
    private enum Pet {
        CAT, DOG
    }
    
    public static class PetOwner {
        ObjectProperty<Pet> pet;
        StringProperty name;
        
        PetOwner(String name, Pet pet) {
            this.pet = new SimpleObjectProperty<>(this, "pet", pet);
            this.name = new SimpleStringProperty(this, "name", name);
        }
        
        public ObjectProperty<Pet> petProperty() {
            return pet;
        }
        
        public StringProperty nameProperty() {
            return name;
        }
        
        public void togglePet() {
            petProperty().set(petProperty().get() == Pet.DOG ? Pet.CAT : Pet.DOG);
        }
        public static ObservableList<PetOwner> owners() {
            ObservableList<PetOwner> owners = FXCollections.observableArrayList(
                    owner -> new Observable[] {owner.petProperty()});
            for (int i = 0; i < 20; i++) {
                owners.add(new PetOwner("O "  + i, i % 3 == 0 ? Pet.CAT : Pet.DOG) );
            }
            return owners;
        }

        @Override
        public String toString() {
            return name.get( ) + " " + pet.get();
        }
        
        
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
            .getLogger(TableWithExternalCounter.class.getName());

}
