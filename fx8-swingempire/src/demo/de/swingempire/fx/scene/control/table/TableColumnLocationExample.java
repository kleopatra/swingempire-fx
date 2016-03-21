/*
 * Created on 15.06.2015
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

//PENDING JW: version dependency!
// fx-9
//import javafx.scene.control.skin.TableColumnHeader;
//import javafx.scene.control.skin.TableHeaderRow;
// fx-8
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.skin.patch.TableViewSkin;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TableColumnLocationExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<Person> table = new TableView<>();
        table.setSkin(new TableViewSkin<>(table));
        table.getColumns().add(column("First Name", Person::firstNameProperty, 120));
        table.getColumns().add(column("Last Name", Person::lastNameProperty, 120));
        table.getColumns().add(column("Email", Person::emailProperty, 250));

        table.getItems().addAll(
                new Person("Jacob", "Smith", "jacob.smith@example.com"),
                new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                new Person("Ethan", "Williams", "ethan.williams@example.com"),
                new Person("Emma", "Jones", "emma.jones@example.com"),
                new Person("Michael", "Brown", "michael.brown@example.com")        
        );

        Pane root = new Pane(table);
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

//        buttonsPerLabel(table, root);
        buttonsPerHeader(table, root);
        // very quick check if SkinDecorator is working
        // change fixed after having been added to the scene
        // to demonstrate that the changeListener on the property is removed
        // table.setFixedCellSize(100);
    }

    /**
     * @param table
     */
    private void buttonsPerHeader(TableView<Person> table, Pane root) {
        if (!(table.getSkin() instanceof TableViewSkin)) return;
        TableViewSkin<?> skin = (TableViewSkin<?>) table.getSkin();
        TableHeaderRow headerRow = skin.getTableHeader();
        for (TableColumn<?, ?> col : table.getColumns()) {
            // Lookup method didn't make it into the public, Bug or feature?
            TableColumnHeader header = 
                (TableColumnHeader) //headerRow.getColumnHeaderFor(col);
                FXUtils.invokeGetMethodValue(TableHeaderRow.class, headerRow, 
                        "getColumnHeaderFor", TableColumnBase.class, col);
            Button button = new Button(col.getText());

            button.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> 
                header.getBoundsInLocal().getWidth(), header.boundsInLocalProperty()));
            button.minWidthProperty().bind(button.prefWidthProperty());
            button.maxWidthProperty().bind(button.prefWidthProperty());

            button.layoutXProperty().bind(Bindings.createDoubleBinding(() -> 
                header.getLocalToSceneTransform().transform(header.getBoundsInLocal()).getMinX(),
                header.boundsInLocalProperty(), header.localToSceneTransformProperty()));

            button.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                table.getBoundsInParent().getMaxY() ,table.boundsInParentProperty()));

            root.getChildren().add(button);
        }
        
    }

    protected void buttonsPerLabel(TableView<Person> table, Pane root) {
        for (TableColumn<Person, ?> col : table.getColumns()) {
            Optional<Label> header = findLabelForTableColumnHeader(col.getText(), root);
            header.ifPresent(label ->  {
                LOG.info("parent for: " + col.getText() + header.get().getParent().getClass());
                Button button = new Button(col.getText());

                button.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> 
                    label.getBoundsInLocal().getWidth(), label.boundsInLocalProperty()));
                button.minWidthProperty().bind(button.prefWidthProperty());
                button.maxWidthProperty().bind(button.prefWidthProperty());

                button.layoutXProperty().bind(Bindings.createDoubleBinding(() -> 
                    label.getLocalToSceneTransform().transform(label.getBoundsInLocal()).getMinX(),
                    label.boundsInLocalProperty(), label.localToSceneTransformProperty()));

                button.layoutYProperty().bind(Bindings.createDoubleBinding(() ->
                    table.getBoundsInParent().getMaxY() ,table.boundsInParentProperty()));

                root.getChildren().add(button);

            });
        }
    }

    private Optional<Label> findLabelForTableColumnHeader(String text, Parent root) {
        return root.lookupAll(".table-view .column-header .label")
                .stream()
                .map(Label.class::cast)
                .filter(label -> label.getText().equals(text))
                .findAny(); // assumes all columns have unique text...
    }



    private <S,T> TableColumn<S,T> column(String title, Function<S,ObservableValue<T>> property, double width) {
        TableColumn<S,T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.apply(cellData.getValue()));
        col.setPrefWidth(width);
        return col ;
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableColumnLocationExample.class.getName());

    public static void main(String[] args) {
        launch(args);
    }
}

