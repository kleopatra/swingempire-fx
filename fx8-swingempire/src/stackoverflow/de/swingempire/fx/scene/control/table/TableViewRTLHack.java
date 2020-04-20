/*
 * Created on 11.08.2017
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.KeyMapping;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;
import com.sun.javafx.scene.control.inputmap.KeyBinding;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/61323825/203657
 * table navigation doesn't respect node orientation (namely rtl)
 * 
 * it's a bug fixed in fx15 https://bugs.openjdk.java.net/browse/JDK-8235480
 * in between (since 9), can tweak the input map - replace all
 * horizontal navigational mappings with exchanged handlers.
 * 
 */
public class TableViewRTLHack extends Application {

    private Parent getContent() {
        ObservableList<Locale> locales = FXCollections.observableArrayList(
                Locale.getAvailableLocales());
        locales.remove(10, locales.size());
        table = new TableView<>(locales);
        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        TableColumn<Locale, String> display = new TableColumn<>("DisplayName");
        display.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        table.getColumns().addAll(display, countryCode, language, variant);

        BorderPane pane = new BorderPane(table);
        return pane;
    }

    
    protected void hackNavigation(TableView<?> table) {
        TableViewSkin<?> skin = (TableViewSkin<?>) table.getSkin();
        // access private field reflectively 
        // use your own favorite utility method :)
        TableViewBehavior<?> behavior = (TableViewBehavior<?>) 
                FXUtils.invokeGetFieldValue(TableViewSkin.class, skin, "behavior");
        // access mappings
        ObservableList<Mapping<?>> mappings = behavior.getInputMap().getMappings();
        // lookup the original mappings for left/right
        KeyBinding leftBinding = new KeyBinding(KeyCode.LEFT);
        KeyBinding rightBinding = new KeyBinding(KeyCode.RIGHT);
        KeyMapping leftMapping = getMapping(mappings, leftBinding); 
        KeyMapping rightMapping = getMapping(mappings, rightBinding);
        // remove the original mappings
        mappings.removeAll(leftMapping, rightMapping);
        // create new mappings with the opposite event handlers and add them
        KeyMapping replaceRight = new KeyMapping(rightBinding, leftMapping.getEventHandler());
        KeyMapping replaceLeft = new KeyMapping(leftBinding, rightMapping.getEventHandler());
        mappings.addAll(replaceRight, replaceLeft);
    }

    /**
     * Utility method to get hold of a KeyMapping for a binding. 
     * Note: only use if certain that it is contained, or guard against failure 
     */
    protected KeyMapping getMapping(ObservableList<Mapping<?>> mappings, KeyBinding keyBinding) {
        Optional<KeyMapping> opt = mappings.stream()
                .filter(mapping -> mapping instanceof KeyMapping)
                .map(mapping -> (KeyMapping) mapping)
                .filter(keyMapping -> keyMapping.getMappingKey().equals(keyBinding))
                .findAny()
                ;
        return opt.get();
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent(), 300, 300));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
        
        hackNavigation(table);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableViewRTLHack.class.getName());
    private TableView<Locale> table;

}
