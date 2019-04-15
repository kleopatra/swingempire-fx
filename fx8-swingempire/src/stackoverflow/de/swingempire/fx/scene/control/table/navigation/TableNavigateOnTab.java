/*
 * Created on 15.04.2019
 *
 */
package de.swingempire.fx.scene.control.table.navigation;

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;

import static de.swingempire.fx.util.FXUtils.*;
import static javafx.scene.input.KeyCode.*;

import de.swingempire.fx.scene.control.skin.TableViewSkinDecorator;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

/**
 * SO: table custom navigation (move selection on tab)
 * https://stackoverflow.com/q/55545537/203657
 */
public class TableNavigateOnTab extends Application {

    private EventHandler<KeyEvent> keyEditingHandler;
    private ObservableList<DataModel> ol = FXCollections.observableArrayList();
    private TableView<DataModel> tv = new TableView<>() {

        @Override
        protected Skin<?> createDefaultSkin() {
//            return new NavTableSkin<>(this);
            return new CustomMappingSkin<>(this);
        }
        
    };

    public static class CustomMappingSkin<T> extends TableViewSkin<T> {

        public CustomMappingSkin(TableView<T> control) {
            super(control);
            installCustomMappings();
        }

        private void installCustomMappings() {
            // reflective access to super's private field, use your own utility method
            TableViewBehavior<T> behavior = (TableViewBehavior<T>) invokeGetFieldValue(TableViewSkin.class, this, "behavior");
            InputMap<TableView<T>> inputMap = behavior.getInputMap();
            KeyCode right = TAB;
            // remove super's tab binding
            inputMap.lookupMapping(new KeyBinding(right)).ifPresent(m -> inputMap.getMappings().remove(m));
            // add custom
            inputMap.getMappings().add(new InputMap.KeyMapping(right, 
                    e -> getSkinnable().getSelectionModel().selectNext()));
        }
        
    }
    public static class NavTableSkin<T> extends TableViewSkin<T> implements TableViewSkinDecorator<T> {

         public NavTableSkin(TableView<T> control) {
            super(control);
            InputMap<TableView<T>> inputMap = getTableViewBehavior().getInputMap();
            KeyCode right = TAB;
            Optional<Mapping<?>> tabMapping = inputMap.lookupMapping(new KeyBinding(right));
//            tabMapping.ifPresent(m -> inputMap.getMappings().remove(m));
            LOG.info("tabMapping: " + tabMapping.get().getEventHandler());
            Optional<Mapping<?>> after = inputMap.lookupMapping(new KeyBinding(right));
            LOG.info("expected: empty after remove: " + after);
            inputMap.getMappings().add(new InputMap.KeyMapping(right, e -> control.getSelectionModel().selectNext()));
            
        }
        
    }
    private Parent createContent() {

        loadDummyData();
        createTableColumns();
        tv.getSelectionModel().setCellSelectionEnabled(true);
        tv.setEditable(true);
        tv.setItems(ol);

        // register after scene/skin is attached, doesn't help
//        tv.skinProperty().addListener((src, ov, nv) -> {
//            tv.setOnKeyPressed(event -> {
//                doTheKeyEvent(event);
//            });
//            
//        });
        //******************************************************************************************
        //JavaFX8 code that doesn't appear to work in JavaFX11
        /*
        tv.setOnKeyPressed(event -> {
            doTheKeyEvent(event);
        });
*/
        //******************************************************************************************
        //Code needed to achieve the same end in JavaFX11
        /*
        registerKeyEventHandler();
        addKeyEditingHandler();
*/
        BorderPane content = new BorderPane();
        content.setCenter(tv);

        HBox hb = new HBox();
        hb.setPadding(new Insets(10D));
        hb.setSpacing(10D);
        hb.setAlignment(Pos.CENTER);
        Button btn1 = new Button("any old object");
        Button btn2 = new Button("another object");
        hb.getChildren().addAll(Arrays.asList(btn1, btn2));

        content.setTop(hb);

        return content;
    }

    public void registerKeyEventHandler() {

        keyEditingHandler = (KeyEvent event) -> {
            doTheKeyEvent(event);
        };

    }    

    private <S> void doTheKeyEvent(KeyEvent event) {

        final KeyCombination shiftTAB = new KeyCodeCombination(KeyCode.TAB, KeyCombination.SHIFT_DOWN);
        @SuppressWarnings("unchecked") TablePosition<DataModel, ?> pos = tv.getFocusModel().getFocusedCell();

        if ( shiftTAB.match(event) ) {

            tv.getSelectionModel().selectLeftCell();
            event.consume();

        } else if ( event.getCode() == KeyCode.TAB ) {

            tv.getSelectionModel().selectRightCell();
            event.consume();

        //... test for other keys and key combinations
        //... otherwise fall through to edit the TableCell

        } else if ( ! event.isControlDown() && ! event.isAltDown() ){

            tv.edit(pos.getRow(), tv.getVisibleLeafColumn(pos.getColumn()));

        }

    }

    public void addKeyEditingHandler() {

        tv.addEventFilter(KeyEvent.KEY_PRESSED, keyEditingHandler);

    }

    private void createTableColumns() {

        TableColumn<DataModel,String> col1 = new TableColumn<>("field1");
        TableColumn<DataModel,String> col2 = new TableColumn<>("field2");
        TableColumn<DataModel,String> col3 = new TableColumn<>("field3");
        TableColumn<DataModel,String> col4 = new TableColumn<>("field4");
        TableColumn<DataModel,String> col5 = new TableColumn<>("field5");

        col1.setCellValueFactory(cellData -> cellData.getValue().field1Property());
        col1.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        col2.setCellValueFactory(cellData -> cellData.getValue().field2Property());
        col2.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        col3.setCellValueFactory(cellData -> cellData.getValue().field3Property());
        col3.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        col4.setCellValueFactory(cellData -> cellData.getValue().field4Property());
        col4.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        col5.setCellValueFactory(cellData -> cellData.getValue().field5Property());
        col5.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        tv.getColumns().addAll(Arrays.asList(col1, col2, col3, col4, col5));

    }

    private void loadDummyData() {

        ol.add(new DataModel("1", "a", "x", "y", "z"));
        ol.add(new DataModel("2", "a", "x", "y", "z"));
        ol.add(new DataModel("3", "a", "x", "y", "z"));
        ol.add(new DataModel("4", "a", "x", "y", "z"));
        ol.add(new DataModel("5", "a", "x", "y", "z"));

    }

    private class DataModel {

        private final StringProperty field1;
        private final StringProperty field2;
        private final StringProperty field3;
        private final StringProperty field4;
        private final StringProperty field5;

        public DataModel(
            String field1,
            String field2,
            String field3,
            String field4,
            String field5
        ) {
            this.field1 = new SimpleStringProperty(field1);
            this.field2 = new SimpleStringProperty(field2);
            this.field3 = new SimpleStringProperty(field3);
            this.field4 = new SimpleStringProperty(field4);
            this.field5 = new SimpleStringProperty(field5);
        }

        public String getField1() {return field1.get().trim();}
        public void setField1(String field1) {this.field1.set(field1);}
        public StringProperty field1Property() {return field1;}

        public String getField2() {return field2.get().trim();}
        public void setField2(String field2) {this.field2.set(field2);}
        public StringProperty field2Property() {return field2;}

        public String getField3() {return field3.get().trim();}
        public void setField3(String field3) {this.field3.set(field3);}
        public StringProperty field3Property() {return field3;}

        public String getField4() {return field4.get().trim();}
        public void setField4(String field4) {this.field4.set(field4);}
        public StringProperty field4Property() {return field4;}

        public String getField5() {return field5.get().trim();}
        public void setField5(String field5) {this.field5.set(field5);}
        public StringProperty field5Property() {return field5;}

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle("JavaFX11 - TableView keyboard navigation");
        stage.setWidth(600D);
        stage.setHeight(600D);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableNavigateOnTab.class.getName());
}

