/*
 * Created on 18.02.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.InputMap.MouseMapping;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Make middle mouse button behave the same way as primary.
 * https://stackoverflow.com/q/54739214/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableRowCustomMouse extends Application {

    public static class CustomMouseTableCellSkin<T, S> extends TableCellSkin<T, S> {

        EventHandler<MouseEvent> original;

        public CustomMouseTableCellSkin(TableCell<T, S> control) {
            super(control);
            adjustMouseBehavior();
        }

        private void adjustMouseBehavior() {
            // dirty: reflective access to behavior
            TableCellBehavior<T, S> behavior = 
                    (TableCellBehavior<T, S>) FXUtils.invokeGetFieldValue(TableCellSkin.class, this, "behavior");
            InputMap<TableCell<T, S>> inputMap = behavior.getInputMap();
            ObservableList<Mapping<?>> mappings = inputMap.getMappings();
            List<Mapping<?>> pressedMapping = mappings.stream()
                    .filter(mapping -> mapping.getEventType() == MouseEvent.MOUSE_PRESSED)
                    .collect(Collectors.toList());
            if (pressedMapping.size() == 1) {
                Mapping<?> originalMapping = pressedMapping.get(0);
                original = (EventHandler<MouseEvent>) pressedMapping.get(0).getEventHandler();
                if (original != null) {
                    EventHandler<MouseEvent> replaced = this::replaceMouseEvent;
                    mappings.remove(originalMapping);
                    mappings.add(new MouseMapping(MouseEvent.MOUSE_PRESSED, replaced));
                }
            }
        }

        private void replaceMouseEvent(MouseEvent e) {
            MouseEvent replaced = e;
            if (e.isMiddleButtonDown()) {
                replaced = new MouseEvent(e.getSource(), e.getTarget(), e.getEventType(),
                        e.getX(), e.getY(),
                        e.getScreenX(), e.getScreenY(),
                        MouseButton.PRIMARY,
                        e.getClickCount(),
                        e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(),
                        true, false, false,
                        e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(),
                        null
                        );
            }
            original.handle(replaced);
        }

    }
    private Parent createContent() {
        TableView<Person> table = new TableView<>(Person.persons());
        TableColumn<Person, String> first = new TableColumn("First Name");
        first.setCellValueFactory(cc -> cc.getValue().firstNameProperty());
        TableColumn<Person, String> last = new TableColumn<>("Last Name");
        last.setCellValueFactory(cc -> cc.getValue().lastNameProperty());
        table.getColumns().addAll(first, last);
        BorderPane content = new BorderPane(table);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.getScene().getStylesheets()
        .add(getClass().getResource("customtablecellskin.css").toExternalForm());
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
    .getLogger(TableRowCustomMouse.class.getName());

}
