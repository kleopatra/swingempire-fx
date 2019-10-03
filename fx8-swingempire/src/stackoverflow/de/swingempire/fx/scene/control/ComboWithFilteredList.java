/*
 * Created on 02.10.2019
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.fx.scene.control.ComboWithFilteredList.Article;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * https://stackoverflow.com/q/58166360/203657
 * problem is unclear: needs double delete to update filter? 
 * 
 * worksforme
 */
public class ComboWithFilteredList extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ComboBoxSample");
        Scene scene = new Scene(new Group(), 450, 250);

        ObservableList<Article> ArticlesList = observableArrayList();

        FilteredList<Article> filteredArticlesList = new FilteredList<>(
                ArticlesList, p -> true);

        ArticlesList.addAll(new Article(1, "hp"), new Article(2, "lenovo"),
                new Article(3, "Asus"));

        ComboBox<Article> articlesComboBox = new ComboBox<>();
        articlesComboBox.setItems(ArticlesList);

        articlesComboBox.setConverter(new StringConverter<Article>() {

            @Override
            public String toString(Article object) {
                if (object == null) {
                    return null;
                } else {
                    return object.getDesignation();
                }
            }

            @Override
            public Article fromString(String des) {
                return ArticlesList
                        .stream().filter(item -> String
                                .valueOf(item.getDesignation()).equals(des))
                        .findFirst().orElse(null);

            }
        });

        articlesComboBox.getEditor().textProperty()
                .addListener((obs, oldVal, newVal) -> {

                    articlesComboBox.show();
                    final TextField editor = articlesComboBox.getEditor();
                    final Article art = articlesComboBox.getValue();
                    final Article selected = articlesComboBox
                            .getSelectionModel().getSelectedItem();

                    Platform.runLater(() -> {
                        if (selected == null || !selected.toString()
                                .equals(editor.getText())) {
                            filteredArticlesList.setPredicate(item -> {
                                if (item.getDesignation().toUpperCase()
                                        .startsWith(newVal.toUpperCase())) {
                                    return true;
                                } else {
                                    return false;
                                }
                            });
                        } else {
                            System.out.println(selected);
                        }
                    });

                    articlesComboBox.setItems(filteredArticlesList);

                });

        articlesComboBox.setEditable(true);

        GridPane grid = new GridPane();
        grid.setVgap(4);
        grid.setHgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));
        grid.add(new Label("To: "), 0, 0);
        grid.add(articlesComboBox, 1, 0);

        Group root = (Group) scene.getRoot();
        root.getChildren().add(grid);
        stage.setScene(scene);
        stage.show();
    }

    class Article {

        SimpleIntegerProperty article_id;

        SimpleStringProperty designation;

        public Article(int article_id, String designation) {
            this.article_id = new SimpleIntegerProperty(article_id);
            this.designation = new SimpleStringProperty(designation);
        }

        public int getArticle_id() {
            return article_id.get();
        }

        public void setArticle_id(int article_id) {
            this.article_id = new SimpleIntegerProperty(article_id);
        }

        public String getDesignation() {
            return designation.get();
        }

        public void setDesignation(String designation) {
            this.designation = new SimpleStringProperty(designation);
        }

        @Override
        public String toString() {
            return getDesignation();
        }

    }

}

