/*
 * Created on 02.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Posted to SO:
 * http://stackoverflow.com/q/35156039/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCellBehaviorReplace extends Application {

    private final ObservableList<Locale> locales =
            FXCollections.observableArrayList(Locale.getAvailableLocales());
    
    private Parent getContent() {
        TableView<Locale> table = createLocaleTable();
        BorderPane content = new BorderPane(table);
        return content;
    }
    
    private TableView<Locale> createLocaleTable() {
        TableView<Locale> table = new TableView<>(locales);
        
        TableColumn<Locale, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        name.setCellFactory(p -> new PlainCustomTableCell<>());
        
        TableColumn<Locale, String> lang = new TableColumn<>("Language");
        lang.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        lang.setCellFactory(p -> new PlainCustomTableCell<>());
        
        table.getColumns().addAll(name, lang);
        return table;
    }

    /**
     * Custom skin that installs custom Behavior. Note: this is dirty!
     * Access super's behavior, dispose to get rid off its handlers, install
     * custom behavior.
     */
    public static class PlainCustomTableCellSkin<S, T> extends TableCellSkin<S, T> {

        private BehaviorBase<?> replacedBehavior;
        public PlainCustomTableCellSkin(TableCell<S, T> control) {
            super(control);
            replaceBehavior();
        }

        private void replaceBehavior() {
            BehaviorBase<?> old = (BehaviorBase<?>) invokeGetField(TableCellSkin.class, this, "behavior");
            old.dispose();
            cleanupInputMap(old.getInputMap());
            // at this point, InputMap mappings are empty:
            // System.out.println("old mappings: " + old.getInputMap().getMappings().size());
            replacedBehavior = new PlainCustomTableCellBehavior<>(getSkinnable());
        }

        /**
         * This is a hack around InputMap not cleaning up internals on removing mappings.
         * We remove MousePressed/MouseReleased/MouseDragged mappings from the internal map.
         * Beware: obviously this is dirty!
         * 
         * @param inputMap
         */
        private void cleanupInputMap(InputMap<?> inputMap) {
            Map eventTypeMappings = (Map) invokeGetField(InputMap.class, inputMap, "eventTypeMappings");
            eventTypeMappings.remove(MouseEvent.MOUSE_PRESSED);
            eventTypeMappings.remove(MouseEvent.MOUSE_RELEASED);
            eventTypeMappings.remove(MouseEvent.MOUSE_DRAGGED);
        }


        @Override
        public void dispose() {
            replacedBehavior.dispose();
            super.dispose();
        }
        
    }
    
    /**
     * Custom behavior that's meant to override basic handlers. Here: short-circuit
     * mousePressed.
     */
    public static class PlainCustomTableCellBehavior<S, T> extends TableCellBehavior<S, T> {

        public PlainCustomTableCellBehavior(TableCell<S, T> control) {
            super(control);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (true) {
                LOG.info("short-circuit super: " + this + getNode().getItem());
                return;
            }
            super.mousePressed(e);
        }
        
    }


    /**
     * C&P of default tableCell in TableColumn. Extended to install custom
     * skin.
     */
    public static class PlainCustomTableCell<S, T> extends TableCell<S, T> {
        
        public PlainCustomTableCell() {
        }
        
        @Override protected void updateItem(T item, boolean empty) {
            if (item == getItem()) return;

            super.updateItem(item, empty);

            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else if (item instanceof Node) {
                super.setText(null);
                super.setGraphic((Node)item);
            } else {
                super.setText(item.toString());
                super.setGraphic(null);
            }
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new PlainCustomTableCellSkin<>(this);
        }
        
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 400, 200));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Reflectively access super field.
     */
    public static Object invokeGetField(Class source, Object target, String name) {
        try {
            Field field = source.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCellBehaviorReplace.class.getName());
}
