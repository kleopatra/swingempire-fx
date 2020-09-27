/*
 * Created on 05.08.2020
 *
 */
package de.swingempire.fx.fxml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * https://stackoverflow.com/q/63256644/203657
 * 
 * cell factory returns null for rowItem
 */
public class SearchMovies {
    @FXML TextField searchField;
    @FXML TableView<Movie> tableView;
    @FXML TableColumn<Movie, String> titleColumn;
    @FXML TableColumn<Movie, String> directorColumn;
    @FXML TableColumn<Movie, Void> likeColumn;

    private ArrayList<Movie> movieList = new ArrayList<>(Arrays.asList(
        new Movie("Persona", "Ingmar Bergman"),
        new Movie("City of God", "Fernando Meirelles"),
        new Movie("Pulp Fiction", "Quentin Tarantino"),
        new Movie("Gone Girl", "David Fincher"),
        new Movie("Fight Club", "David Fincher"),
        new Movie("Perfect Blue", "Satoshi Kon"),
        new Movie("Prisoners", "Denis Villeneuve"),
        new Movie("Sin City", "Frank Miller"),
        new Movie("Mean Girls", "Mark Waters"),
        new Movie("Breakfast Club", "John Hughes"),
        new Movie("Sixteen Candles", "John Hughes"),
        new Movie("Reservoir Dogs", "Quentin Tarantino")
    ));

    private ObservableList<Movie> observableMovieList = FXCollections.observableArrayList(movieList);

    private FilteredList<Movie> filteredList = new FilteredList<>(observableMovieList);
    @FXML
    public void initialize() {
        titleColumn
                .setCellValueFactory(item -> item.getValue().titleProperty());
        directorColumn.setCellValueFactory(
                item -> item.getValue().directorProperty());

        likeColumn.setCellFactory(col -> new TableCell<Movie, Void>() {
            private final Button button = new Button("LIKE");

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Movie movie = getTableRow().getItem();
                    if (movie == null) {
                        System.out.println("got null for " + getIndex());
                    }
                    System.out.println(
//                            "Rendering row for " + movie + " at " + getIndex());
                     movie.getTitle() + " by " + movie.getDirector());
                    button.setOnAction(e -> System.out.println("Someone liked "
                            + movie.getTitle() + " " + movie.getDirector()));
                    setGraphic(button);
                }
            }
        });

//        tableView.setItems(observableMovieList);
        tableView.setItems(filteredList);
    }

    @FXML
    public void search() {
        String query = searchField.getText();

        if (query == null || query.isBlank() || query.isEmpty()) {
            filteredList.setPredicate(null);
            return;
        }

        Predicate<Movie> filter = movie -> {
            return movie.getTitle().contains(query) || movie.getDirector().contains(query);
        };
        filteredList.setPredicate(filter);
//        observableMovieList = FXCollections.observableArrayList(filterMovies(query));
//        tableView.setItems(observableMovieList);
    }

    private ArrayList<Movie> filterMovies(String query) {
        ArrayList<Movie> filteredMovies = new ArrayList<>();

        for (Movie movie : movieList) {
            if (movie.getTitle().contains(query) || movie.getDirector().contains(query)) {
                filteredMovies.add(movie);
            }
        }

        return filteredMovies;
    }

    private class Movie {
        private final StringProperty title;
        private final StringProperty director;

        public Movie(String title, String director) {
            this.title = new SimpleStringProperty(title);
            this.director = new SimpleStringProperty(director);
        }

        public String getTitle() {
            return title.get();
        }

        public StringProperty titleProperty() {
            return title;
        }

        public void setTitle(String title) {
            this.title.set(title);
        }

        public String getDirector() {
            return director.get();
        }

        public StringProperty directorProperty() {
            return director;
        }

        public void setDirector(String director) {
            this.director.set(director);
        }

        @Override
        public String toString() {
            return getTitle() + ", " + getDirector();
        }
        
        
    }
}