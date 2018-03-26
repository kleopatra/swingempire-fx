/*
 * Created on 25.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import de.swingempire.fx.scene.control.cell.TableWithEngines.PartListCell;
import de.swingempire.fx.scene.control.cell.TableWithEngines.PartsTableCell;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TableWithEngines extends Application {
    public static class Engine {
        private final SimpleStringProperty name = new SimpleStringProperty("");

        Callback<Part, Observable[]> partsExtractor = part -> new Observable[] {
                part.nameProperty(), part.engineProperty().get().nameProperty() };

        private final ObservableList<Part> parts = FXCollections
                .observableArrayList(partsExtractor);

        public Engine(String name) {
            this.setName(name);
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public ObservableList<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private final SimpleStringProperty name = new SimpleStringProperty("");

        // Reference to the parent engine
        private final ObjectProperty<Engine> engine = new SimpleObjectProperty<>(
                null);

        public Part(String name, Engine engine) {
            this.setName(name);
            this.setEngine(engine);
        }

        public ObjectProperty<Engine> engineProperty() {
            return engine;
        }

        public Engine getEngine() {
            return engine.get();
        }

        public void setEngine(Engine engine) {
            this.engine.set(engine);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }
    }

    /**
     * If you edit the field name of the engine on the table the changed name
     * does not reflect on the listView within the table cell, it should
     * actually show the new name of the engine
     */

    public static class PartListCell extends ListCell<Part> {
        private Label name = new Label();

        public PartListCell() {

        }

        @Override
        protected void updateItem(Part part, boolean empty) {
            super.updateItem(part, empty);

            if (!empty && part != null) {
                name.setText(
                        part.getEngine().getName() + " / " + part.getName());
                setGraphic(name);
            } else {
                setGraphic(null);
            }
        }
    }

    public static class PartsTableCell
            extends TableCell<Engine, ObservableList<Part>> {
        private final ListView<Part> listView = new ListView<>();

        public PartsTableCell() {
            listView.setCellFactory(listView -> new PartListCell());
        }

        @Override
        protected void updateItem(ObservableList<Part> parts, boolean empty) {
            if (!empty && parts != null) {
                listView.setItems(parts);
                setGraphic(listView);
            } else {
                setGraphic(null);
            }
        }
    }

    Callback<Engine, Observable[]> engineExtractor = engine -> new Observable[] {
            engine.nameProperty(), };

    private final ObservableList<Engine> engines = FXCollections
            .observableArrayList(engineExtractor);

    public TableWithEngines() {
        Engine engine = new Engine("Engine-1");
        Part part1 = new Part("Part-1", engine);
        Part part2 = new Part("Part-1", engine);

        engine.getParts().addAll(part1, part2);

        engines.add(engine);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();

        TableColumn<Engine, String> nameColumn = new TableColumn<>("Name");
        TableColumn<Engine, ObservableList<Part>> partsColumn = new TableColumn<>(
                "Parts");

        // Set the Cell Factories
        nameColumn.setCellValueFactory(
                cellData -> cellData.getValue().nameProperty());
        // Make an editable text field
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
//        nameColumn.setOnEditCommit(event -> event.getTableView().getItems()
//                .get(event.getTablePosition().getRow())
//                .setName(event.getNewValue()));

        partsColumn.setCellFactory(param -> new PartsTableCell());
        partsColumn.setCellValueFactory(new PropertyValueFactory<>("parts"));

        TableView<Engine> tableView = new TableView<>(engines);
        tableView.setEditable(true);
        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(partsColumn);

        // Set the stage
        root.getChildren().add(tableView);
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

