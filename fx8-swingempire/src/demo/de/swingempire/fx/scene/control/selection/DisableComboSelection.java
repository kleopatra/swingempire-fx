/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.List;
import java.util.logging.Logger;

import de.swingempire.fx.scene.control.selection.DisableComboSelection.DisableObject;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/48771072/203657
 * "disable" item -> visuals okay, but don't prevent selection by keyboard
 * (only by mouse)
 * 
 * Need custom selectionModel .. or what? 
 * 
 * Revised: probably not a good idea, don't - re-think ux.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DisableComboSelection extends Application {

    public static class DisableObject {
        StringProperty text;
        BooleanProperty disabled;
        
        public DisableObject(String text) {
            this.text = new SimpleStringProperty(this, "text", text) {

                @Override
                protected void invalidated() {
                    disabled.set(checkDisabled());
                }
                
            };
            this.disabled = new SimpleBooleanProperty(this, "disabled", checkDisabled());
        }
        
        private boolean checkDisabled() {
            return getText().length() > 3;
        }

        public StringProperty textProperty() {
            return text;
        }
        
        public String getText() {
            return textProperty().get();
        }
        
        public void setText(String text) {
            textProperty().set(text);
        }
        
        public BooleanProperty disabledProperty() {
            return disabled;
        }
        
        private void setDisabled(boolean disabled) {
            disabledProperty().set(disabled);
        }
        
        public boolean isDisabled() {
            return disabledProperty().get();
        }
        
        
        @Override
        public String toString() {
            return getText() + ": " + isDisabled();
        }

        public static ObservableList<DisableObject> disabledObjects() {
            return FXCollections.observableList(List.of(new DisableObject("AX"), 
                    new DisableObject("YYA"), new DisableObject("XXXXX"), new DisableObject("SS")), 
                    e -> {return new Observable[] {e.disabledProperty()}; 
                    });
        }
    }
    
    private Parent createContent() {
        ComboBox<DisableObject> box = new ComboBox<>(DisableObject.disabledObjects());
        
        box.setCellFactory(e -> {
            return createListCell();
        });
//        box.setButtonCell(createListCell());
        
        box.valueProperty().addListener((src, ov, nv) -> LOG.info("value changed: " + nv));
        BorderPane pane = new BorderPane(box);
        return pane;
    }

    /**
     * @return
     */
    protected ListCell<DisableObject> createListCell() {
        return new ListCell<DisableObject>() {
            @Override
            protected void updateItem(DisableObject item, boolean empty){
                super.updateItem(item, empty);
                if(item != null || !empty){
                    setText(empty ? "" : item.getText()); 
                    updateDisableState();

                }
            }

            protected void updateDisableState(){
                boolean disable = isEmpty() || getItem().isDisabled();
                setDisable(disable);
            }
           
        };
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DisableComboSelection.class.getName());


}
