/*
 * Created on 21.01.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/54292210/203657
 * open dropdown of combo on starting the edit
 * 
 * - looks fishy but working as expected 
 * - subclassing core ComboBoxTableCell is fine as well
 * 
 * The NPE if table child of TitledPane (in fx8, fixed in fx11) is due to
 * https://bugs.openjdk.java.net/browse/JDK-8196827
 * comboSkin doesn't guard itself against null scene in show
 * (actually, positionAndShowPopup which is called from show)
 * hack is custom skin that guards's against null scene in show
 * 
 * another quirk (in fx8): layout of popup not adjusted to changing column width
 * - incorrect the very first time it's opened
 * - second time it's adjusted
 * - resize column: width the same as the second time always
 * 
 * in fx11: incorrect on first opening after resize of colum
 * due to "early" showing?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboTableCellShow extends Application {

    public class FromComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {
        
        public FromComboBoxTableCell(T... items) {
            super(items);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEditing() && getGraphic() instanceof ComboBox) {
                Platform.runLater(( ) -> {
                    // does not help with layout issue
                    ((ComboBox) getGraphic()).show();
                });
            }
        }
        
        
        
    }
    public class ComboTableCell<T,S> extends TableCell<T,S> {

        private ComboBox<S> combo;

        public ComboTableCell(Collection<S> items) {
            combo = new ComboBox<>();
            combo.setItems(FXCollections.observableArrayList(items));
            combo.prefWidthProperty().bind(widthProperty());
            combo.valueProperty().addListener((observable, oldValue, newValue) -> commitEdit(newValue));
//          1. Solution with mouse event
//          this.setOnMouseClicked(event -> {
//              if(event.getClickCount() == 2){
//                  combo.getSelectionModel().select(getItem());
//                  setText(null);
//                  setGraphic(combo);
//                  if(!combo.isShowing()){
//                      combo.show();
//                  }
//              }
//          });
        }

    //  2. Solution with startEdit
        @Override
        public void startEdit() {
            combo.getSelectionModel().select(getItem());
            super.startEdit();
            setText(null);
            setGraphic(combo);
            if(!combo.isShowing()){
                combo.show();
            }
        }

        @Override
        protected void updateItem(S item, boolean empty) {
            super.updateItem(item, empty);
            if(empty){
                setText(null);
                setGraphic(null);
                return;
            }
            setText(getItem().toString());
            setGraphic(null);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().toString());
            setGraphic(null);
            if(combo.isShowing()){
                combo.hide();
            }
        }

        @Override
        public void commitEdit(S newValue) {
            super.commitEdit(newValue);
            setGraphic(null);
            setText(getItem().toString());
            if(combo.isShowing()){
                combo.hide();
            }
            setGraphic(null);
            setText(getItem().toString());
        }
    }

    
    private Parent createContent() {
        TableView<Model> table = new TableView();
        table.setEditable(true);

        TableColumn<Model, String> col = new TableColumn("data");
        col.setCellValueFactory(data -> data.getValue().text);
//        col.setCellFactory(factory -> new ComboTableCell<>(Arrays.asList("a","b","c")));
        col.setCellFactory(factory -> new FromComboBoxTableCell("a","b","c"));

        table.getColumns().addAll(col);
        table.setItems(FXCollections.observableArrayList(Arrays.asList(new Model("a"),new Model("b"))));
        
        // spurious npe in ComboBoxPopupControl.positionAndShowPopup
        // after expanding/collapsing the titledPane several (?) times
        // not in fx11 - but: popup not really closed on collapsing the titledPane
        // on re-opening, it appears somewhere in the middle
        TitledPane titled = new TitledPane("some titled", table);
        BorderPane content = new BorderPane(titled);
        return content;
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
            .getLogger(ComboTableCellShow.class.getName());

    static class Model{

        private StringProperty text;

        public Model(String text) {
            this.text = new SimpleStringProperty(text);
        }

        public String getText() {
            return text.get();
        }

        public StringProperty textProperty() {
            return text;
        }
    }
}
