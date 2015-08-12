/*
 * Created on 12.08.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import de.swingempire.fx.scene.control.cell.TableChoiceBoxTest.Feature;

/**
 * Example by James_D
 * http://stackoverflow.com/a/22768113/203657
 */
public class TableChoiceBoxTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        final TableView<Feature> table = new TableView<>();
        table.setEditable(true);
        final TableColumn<Feature, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        final TableColumn<Feature, String> logLevelCol = new TableColumn<>("Log level");
        logLevelCol.setCellValueFactory(new PropertyValueFactory<>("logLevel"));
        logLevelCol.setPrefWidth(150);

        final ObservableList<String> logLevelList = FXCollections.observableArrayList("FATAL", "ERROR", "WARN", "INFO", "INOUT", "DEBUG");
        logLevelCol.setCellFactory(ChoiceBoxTableCell.forTableColumn(logLevelList));

        table.getColumns().addAll(nameCol, logLevelCol);

        table.getItems().setAll(
            IntStream.rangeClosed(1, 20)
                .mapToObj(i -> new Feature("Item "+i, "FATAL")) 
                .collect(Collectors.toList())
            );

        Button showDataButton = new Button("Dump data");
        showDataButton.setOnAction(event -> table.getItems().forEach(System.out::println));

        BorderPane root = new BorderPane();
        root.setCenter(table);
        root.setBottom(showDataButton);

        Scene scene = new Scene(root, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static class Feature {
        private final StringProperty name ;
        private final StringProperty logLevel ;

        public Feature(String name, String logLevel) {
            this.name = new SimpleStringProperty(this, "name", name);
            this.logLevel = new SimpleStringProperty(this, "logLevel", logLevel);
        }

        public StringProperty nameProperty() {
            return name ;
        }
        public final String getName() {
            return name.get();
        }
        public final void setName(String name) {
            this.name.set(name);
        }

        public StringProperty logLevelProperty() {
            return logLevel ;
        }
        public final String getLogLevel() {
            return logLevel.get();
        }
        public final void setLogLevel(String logLevel) {
            this.logLevel.set(logLevel);
        }

        @Override
        public String toString() {
            return getName() + ": " + getLogLevel();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}