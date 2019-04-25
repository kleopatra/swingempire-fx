/*
 * Created on 19.10.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.swingempire.fx.scene.control.cell.TableRowValidMarker.RowTest;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Change row style based on a (valid) property in the item.
 * There are several questions on SO around the issue, f.i.
 * https://stackoverflow.com/q/52519470/203657
 * 
 * at it's base, there's a bug in TableRow (left-over from identity vs. equality check)
 * https://bugs.openjdk.java.net/browse/JDK-8092821
 * which leads to row.updateItem not called on list change notifications of type update.
 * 
 * ItemTableRow:
 * This workaround is from fabian: add/remove a listener to the item's validProperty
 * in updateItem and let that listener update the the style.
 * 
 * TableRowCustomIsItemChanged:
 * Workaround with overridden isItemChanged - implemented to check item's validProperty and OR with
 * super. valid column must be contained and visible or items must be configured with extractor
 * 
 * @author Jeanette Winzenburg, Berlin
 * @author fabian
 * 
 * @see RowStylingUpdate
 */
public class RowStylingPseudoClass extends Application {

    @Override
    public void start(Stage primaryStage) {
      TableView<Item> table = new TableView<>(createDummyData(10));
      // setting editable is needed for CheckBoxTableCell!
      table.setEditable(true);
//      table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
      table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

//      table.setRowFactory(t -> new ItemTableRow());
      table.setRowFactory(t -> new TableRowCustomIsItemChanged());

      TableColumn<Item, String> nameCol = new TableColumn<>("Name");
      nameCol.setCellValueFactory(features -> features.getValue().nameProperty());
      table.getColumns().add(nameCol);

      TableColumn<Item, Boolean> validCol = new TableColumn<>("Valid");
      validCol.setCellFactory(CheckBoxTableCell.forTableColumn(validCol));
      validCol.setCellValueFactory(features -> features.getValue().validProperty());
      table.getColumns().add(validCol);

      primaryStage.setScene(new Scene(new StackPane(table), 800, 600));
      primaryStage.getScene().getStylesheets().add(getClass().getResource("rowstylingpseudoclass.css").toExternalForm());
      primaryStage.setTitle("JavaFX Application");
      primaryStage.show();
    }


    private ObservableList<Item> createDummyData(int count) {
      return IntStream.rangeClosed(1, count)
          .mapToObj(i -> "Item #" + i)
          .map(name -> new Item(name, Math.random() >= 0.5))
          .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    
    public static class TableRowCustomIsItemChanged extends TableRow<Item> {
        private static final PseudoClass VALID = PseudoClass.getPseudoClass("valid");
        public TableRowCustomIsItemChanged() {
            getStyleClass().add("item-table-row");
          }


        @Override
        protected boolean isItemChanged(Item oldItem, Item newItem) {
            boolean changed = super.isItemChanged(oldItem, newItem);
            if (oldItem != null && newItem != null && newItem == oldItem) {
                changed = changed || (oldItem.isValid() != newItem.isValid());
            }
            return true;
        }


        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);
          if (empty || item == null) {
            updateValidPseudoClass(false);
          } else {
            updateValidPseudoClass(item.isValid());
          }
        }

        private void updateValidPseudoClass(boolean active) {
            pseudoClassStateChanged(VALID, active);
          }

       
    }
    /**
     * Un/Registers listener to validProperty and toggles style (here: pseudoClass) on change
     * 
     * will work always, whether or not the property is shown in a column
     * 
     * @author fabian
     */ 
    public static class ItemTableRow extends TableRow<Item> {

        private static final PseudoClass VALID = PseudoClass.getPseudoClass("valid");

        private final ChangeListener<Boolean> listener = (obs, oldVal, newVal) -> updateValidPseudoClass(newVal);
        private final WeakChangeListener<Boolean> weakListener = new WeakChangeListener<>(listener);

        public ItemTableRow() {
          getStyleClass().add("item-table-row");
        }

        
        @Override
        public void updateIndex(int i) {
            // TODO Auto-generated method stub
            super.updateIndex(i);
        }


        @Override
        protected void updateItem(Item item, boolean empty) {
          Item oldItem = getItem();
          if (oldItem != null) {
            oldItem.validProperty().removeListener(weakListener);
          }
          super.updateItem(item, empty);
//          LOG.info("old/new in row: " + oldItem + "/" + getItem());
          if (empty || item == null) {
            updateValidPseudoClass(false);
          } else {
            item.validProperty().addListener(weakListener);
            updateValidPseudoClass(item.isValid());
          }
        }

        private void updateValidPseudoClass(boolean active) {
          pseudoClassStateChanged(VALID, active);
        }

      }

    private static class Item {

        private final StringProperty name = new SimpleStringProperty(this, "name");
        public final void setName(String name) { this.name.set(name); }
        public final String getName() { return name.get(); }
        public final StringProperty nameProperty() { return name; }

        private final BooleanProperty valid = new SimpleBooleanProperty(this, "valid");
        public final void setValid(boolean valid) { this.valid.set(valid); }
        public final boolean isValid() { return valid.get(); }
        public final BooleanProperty validProperty() { return valid; }

        public Item() {}

        public Item(String name, boolean valid) {
          setName(name);
          setValid(valid);
        }

      }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(RowStylingPseudoClass.class.getName());
  }

