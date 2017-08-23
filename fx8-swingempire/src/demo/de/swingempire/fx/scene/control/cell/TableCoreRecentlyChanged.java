/*
 * Created on 06.02.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.fx.util.FXUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 * 
 * from SO: 
 * Implement a recently-changed marker
 * 
 * https://stackoverflow.com/q/45791367/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableCoreRecentlyChanged extends Application {

    public static class RecentChanged extends TableCell<Dummy, String> {
        
        private ChangeListener<Boolean> recentListener = (src, ov, nv) -> updateRecentStyle(nv);
        private Dummy lastDummy;
        
        /*
         * Just to see any effect.
         */
        protected void updateRecentStyle(boolean highlight) {
            if (highlight) {
                setStyle("-fx-background-color: #99ff99");
            } else {
                setStyle("-fx-background-color: #009900");
            }
        }
        
        @Override
        public void updateIndex(int index) {
            if (lastDummy != null) {
                lastDummy.recentlyChangedProperty().removeListener(recentListener);
                lastDummy = null;
            }
            updateRecentStyle(false);
            super.updateIndex(index);
            if (getTableRow() != null && getTableRow().getItem() != null) {
                lastDummy = getTableRow().getItem();
                updateRecentStyle(lastDummy.recentlyChangedProperty().get());
                lastDummy.recentlyChangedProperty().addListener(recentListener);
            } 
        }

        @Override 
        protected void updateItem(String item, boolean empty) {
            if (item == getItem()) return;
        
            super.updateItem(item, empty);
        
            if (item == null) {
                super.setText(null);
                super.setGraphic(null);
            } else {
                super.setText(item);
                super.setGraphic(null);
            }
        }

    }
    
    private Parent getContent() {
        TableView<Dummy> table = new TableView<>(createData(50));
        table.setEditable(true);
        
        TableColumn<Dummy, String> column = new TableColumn<>("Value");
        column.setCellValueFactory(c -> c.getValue().valueProperty());
        column.setCellFactory(e -> new RecentChanged());
        column.setMinWidth(200);
        table.getColumns().addAll(column);
        
        int editIndex = 20; 
        
        Button changeValue = new Button("Edit");
        changeValue.setOnAction(e -> {
            for (int index = editIndex; index <(editIndex +3); index++) {
                Dummy dummy = table.getItems().get(index);
                dummy.setValue(dummy.getValue()+"x");
                
            }
        });
        HBox buttons = new HBox(10, changeValue);
        BorderPane content = new BorderPane(table);
        content.setBottom(buttons);
        return content;
    }

    private ObservableList<Dummy> createData(int size) {
        return FXCollections.observableArrayList(
                Stream.generate(Dummy::new)
                .limit(size)
                .collect(Collectors.toList()));
    }
    
    private static class Dummy {
        private static int count;
        // PENDING JW: not good enough ... doesn't catch quick changes
        // probably need to make the invalidation of value
        // replay the timeline directly, then bind its status
        // to a boolean
        ReadOnlyBooleanWrapper recentlyChanged = new ReadOnlyBooleanWrapper() {

            Timeline recentTimer;
            @Override
            protected void invalidated() {
                if (get()) {
                    if (recentTimer == null) {
                        recentTimer = new Timeline(new KeyFrame(
                                Duration.millis(2500),
                                ae -> set(false)));
                    }
                    recentTimer.playFromStart();
                } else {
                    if (recentTimer != null) recentTimer.stop();
                }
            }
            
        };
        StringProperty value = new SimpleStringProperty(this, "value", "initial " + count++) {

            @Override
            protected void invalidated() {
                recentlyChanged.set(true);
            }
            
        };
        
        public StringProperty valueProperty() {return value;}
        public String getValue() {return valueProperty().get(); }
        public void setValue(String text) {valueProperty().set(text); }
        public ReadOnlyBooleanProperty recentlyChangedProperty() { return recentlyChanged.getReadOnlyProperty(); }
        public String toString() {return "[dummy: " + getValue() + "]";}
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(new Scene(getContent()));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableCoreRecentlyChanged.class.getName());
}
