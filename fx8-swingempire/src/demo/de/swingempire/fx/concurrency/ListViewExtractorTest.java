/*
 * Created on 30.01.2018
 *
 */
package de.swingempire.fx.concurrency;

import java.util.List;

import de.swingempire.fx.collection.FXThreadTransformationList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Problem with threading:
 * https://stackoverflow.com/q/48519057/203657
 * 
 * Can isolate the off-fx-thread in backing list by a secondary list listening, 
 * can't handle extractor.
 * 
 * Idea was to use extractor on backing list -> now problem is that there's no
 * official way to propagate that notification to the secondary list.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public final class ListViewExtractorTest extends Application {

    private static char PERSON_IDENTIFIER = 'A';
    private static final ObservableList<Person> originalList = FXCollections.observableArrayList(
            personUI -> new Observable[]{personUI.adultProperty()});

    static {
        for ( int a = 0; a < 3; a++ ) {
            originalList.add(new Person(Math.random() > 0.5));
        }
    }

    private static class Person {

        private final char ID;
        private final BooleanProperty adult;

        private Person(boolean adult) {
            this.ID = PERSON_IDENTIFIER++;
            this.adult = new SimpleBooleanProperty(adult);
            Thread randomizerThread = new Thread(() -> {
                while ( true ) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    setAdult(Math.random() > 0.5);
                    System.out.println("Updated :" + toString());
                }
            });
            randomizerThread.setDaemon(true);
            randomizerThread.start();
        }

        public boolean isAdult() {
            return adult.get();
        }

        public BooleanProperty adultProperty() {
            return adult;
        }

        public void setAdult(boolean adult) {
            this.adult.set(adult);
        }

        @Override
        public String toString() {
            return "Person{" +
                    "ID=" + ID +
                    ", adult=" + adult.get() +
                    '}';
        }
    }

    public ListView<Person> withTransform() {
        ObservableList<Person> secondList = new FXThreadTransformationList<>(originalList);
        ListView<Person> listView = new ListView<>(secondList);
        return listView;
    }

    /**
     * @return
     */
    public ListView<Person> withPerson() {
        ObservableList<Person> secondList = FXCollections.observableArrayList();//personUI -> new Observable[]{personUI.adultProperty()});
        for ( Person person : originalList ) {
            secondList.add(person);
        }
        originalList.addListener((ListChangeListener<? super Person>) change -> {
            
            while(change.next()) {
//                if (change.wasPermutated()) {
////                    secondList.subList(change.getFrom(), change.getTo()).clear();
////                    List<? extends Person> addPeople = change.getList().subList(change.getFrom(), change.getTo());
////                    for ( int i = 0; i < addPeople.size(); i++ ) {
////                        final int index = i;
////                        Person addPerson = addPeople.get(i);
////                        Platform.runLater(() -> secondList.add(change.getFrom() + index, new PersonUI(addPerson)));
////                    }
//                } else {
                    if (change.wasRemoved()) {
                        Platform.runLater(() -> secondList.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear());
                    }
                    
                    if (change.wasAdded()) {
                        List<? extends Person> addedSubList = change.getAddedSubList();
                        for ( int i = 0; i < addedSubList.size(); i++ ) {
                            final int index = i;
                            Person person = addedSubList.get(i);
                            Platform.runLater(() -> secondList.add(change.getFrom() + index, person));
                        }
                    }
                    if (change.wasUpdated()) {
                        Platform.runLater(() -> {
                            // what to do here? settin not the same as update;
                            for (int i = change.getFrom(); i < change.getTo(); i++) {
                                secondList.set(i, secondList.get(i));
                            }
                        });
                    }
                }
//            }
        });
        ListView<Person> listViewPerson = new ListView<>(secondList);
        return listViewPerson;
    }
    /**
     * @return
     */
    public ListView<PersonUI> withPersonUI() {
        ObservableList<PersonUI> secondList = FXCollections.observableArrayList(personUI -> new Observable[]{personUI.adultProperty()});
        for ( Person person : originalList ) {
            secondList.add(new PersonUI(person));
        }
        originalList.addListener((ListChangeListener<? super Person>) change -> {

            while(change.next()) {
                if (change.wasPermutated()) {
//                    secondList.subList(change.getFrom(), change.getTo()).clear();
//                    List<? extends Person> addPeople = change.getList().subList(change.getFrom(), change.getTo());
//                    for ( int i = 0; i < addPeople.size(); i++ ) {
//                        final int index = i;
//                        Person addPerson = addPeople.get(i);
//                        Platform.runLater(() -> secondList.add(change.getFrom() + index, new PersonUI(addPerson)));
//                    }
                } else {
                    if (change.wasRemoved()) {
                        Platform.runLater(() -> secondList.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear());
                    }

                    if (change.wasAdded()) {
                        List<? extends Person> addedSubList = change.getAddedSubList();
                        for ( int i = 0; i < addedSubList.size(); i++ ) {
                            final int index = i;
                            Person person = addedSubList.get(i);
                            Platform.runLater(() -> secondList.add(change.getFrom() + index, new PersonUI(person)));
                        }
                    }
                }
            }
        });
        ListView<PersonUI> listViewPerson = new ListView<>(secondList);
        return listViewPerson;
    }


    @Override
        public void start(Stage stage) throws Exception {
    //        ListView<PersonUI> listViewPerson = withPersonUI();
//            ListView<Person> listViewPerson = withPerson();
            ListView<Person> listViewPerson = withTransform();
            stage.setScene(new Scene(listViewPerson));
            stage.setTitle("Hello");
            stage.show();
        }

    public static void main(String[] args) {
        launch();
    }


    /**
     * Original: isolate backing person from ui. This here is listening to 
     * the backing person and updates its own property on the fx-thread.
     * 
     */
    private static class PersonUI {
    
        private final Person person;
        private final BooleanProperty adult;
    
        public PersonUI(Person person) {
            this.person = person;
            adult = new SimpleBooleanProperty(person.isAdult());
            person.adultProperty().addListener((observableValue, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    this.adult.set(newValue);
                    System.out.println("Updating person UI: " + toString() + "/// new adult value: " + this.adult.get());
                });
            });
        }
    
        public boolean isAdult() {
            return adult.get();
        }
    
        public BooleanProperty adultProperty() {
            return adult;
        }
    
        @Override
        public String toString() {
            return person.toString();
        }
    }
}