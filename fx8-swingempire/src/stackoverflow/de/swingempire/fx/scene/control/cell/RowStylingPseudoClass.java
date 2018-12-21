/*
 * Created on 19.10.2018
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class RowStylingPseudoClass extends Application {

    @Override
    public void start(Stage primaryStage) {
      TableView<Item> table = new TableView<>(createDummyData(10));
//      table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
      table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

      table.setRowFactory(t -> new ItemTableRow());

      TableColumn<Item, String> nameCol = new TableColumn<>("Name");
      nameCol.setCellFactory(c -> {
          TableCell<Item, String> cell = new TableCell<>() {

            @Override
            protected void updateItem(String item, boolean empty) {
                String old = getItem();
                super.updateItem(item, empty);
//                LOG.info("old/new in cell: " + old + " / " + getItem());
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
              
          };
          return cell;
      });
      nameCol.setCellValueFactory(features -> features.getValue().nameProperty());
      table.getColumns().add(nameCol);

      TableColumn<Item, Boolean> validCol = new TableColumn<>("Valid");
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

    public class Item {

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

