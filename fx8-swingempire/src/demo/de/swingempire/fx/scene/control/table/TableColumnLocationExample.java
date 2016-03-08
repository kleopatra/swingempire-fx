/*
 * Created on 15.06.2015
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TableColumnLocationExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        TableView<Person> table = new TableView<>();
        table.setSkin(new MyTableViewSkin<>(table));
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

    }

    /**
     * @param table
     */
    private void buttonsPerHeader(TableView<Person> table, Pane root) {
        if (!(table.getSkin() instanceof TableViewSkinBase)) return;
        MyTableViewSkin skin = (MyTableViewSkin) table.getSkin();
        TableHeaderRow headerRow = skin.getTableHeader();
        for (TableColumn col : table.getColumns()) {
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
    public static class Person {
        private StringProperty firstName = new SimpleStringProperty();
        private StringProperty lastName = new SimpleStringProperty();
        private StringProperty email = new SimpleStringProperty();

        public Person(String firstName, String lastName, String email) {
            setFirstName(firstName);
            setLastName(lastName);
            setEmail(email);
        }

        public final StringProperty firstNameProperty() {
            return this.firstName;
        }

        public final String getFirstName() {
            return this.firstNameProperty().get();
        }

        public final void setFirstName(final String firstName) {
            this.firstNameProperty().set(firstName);
        }

        public final StringProperty lastNameProperty() {
            return this.lastName;
        }

        public final String getLastName() {
            return this.lastNameProperty().get();
        }

        public final void setLastName(final String lastName) {
            this.lastNameProperty().set(lastName);
        }

        public final StringProperty emailProperty() {
            return this.email;
        }

        public final String getEmail() {
            return this.emailProperty().get();
        }

        public final void setEmail(final String email) {
            this.emailProperty().set(email);
        }


    }

    /**
     * Override to get access to TableHeaderRow.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
public static class MyTableViewSkin<T> extends TableViewSkin<T> {

    private TableHeaderRow header;
    
    /**
     * @param control
     */
    public MyTableViewSkin(TableView<T> control) {
        super(control);
    }

    /**
     * Implemented to grab header. Super method is 
     * package and final.
     * @return
     */
    public TableHeaderRow getTableHeader() {
        return header;
    }
    @Override
    protected TableHeaderRow createTableHeaderRow() {
        header = super.createTableHeaderRow();
        return header;
    }
    
    
}
    public static void main(String[] args) {
        launch(args);
    }
}

