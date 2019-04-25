/*
 * Created on 25.04.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Change row style based on a (valid) property in the item. 
 * <p>
 * This example tries out several approaches, one tab per approach. All tableRows
 * use PseudoClass for toggling state, they differ in how-to toggle the state change.
 * 
 * <p>
 * 
 * There are several questions on SO around the issue, f.i.
 * https://stackoverflow.com/q/52519470/203657
 * 
 * <p>
 * at it's base, there's a bug in TableRow (left-over from identity vs. equality check)
 * https://bugs.openjdk.java.net/browse/JDK-8092821
 * which leads to row.updateItem not called on list change notifications of type update.
 * <p>
 * 
 * ItemTableRow:
 * This workaround is from fabian: add/remove a listener to the item's validProperty
 * in updateItem and let that listener update the the style.
 * 
 * <p>
 * TableRowCustomIsItemChanged:
 * Workaround with overridden isItemChanged - implemented to check item's validProperty and OR with
 * super.. 
 * 
 * @see RowStylingPseudoClass
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class RowStylingUpdate extends Application {
    private static final PseudoClass VALID = PseudoClass.getPseudoClass("valid");

    private ObservableList<Item> data;
    
    private Parent createContent() {
        TabPane pane = new TabPane();
        
        // tab with core tableRow: toggle pseudo-class in updateItem
        TableView<Item> core = createTable(true);
        core.setRowFactory(v -> new CoreTableRow());
        pane.getTabs().add(new Tab("Plain core", core));
        
        // tab with custom tableRow: override isItemChanged
        TableView<Item> isItemChanged = createTable(true);
        isItemChanged.setRowFactory(v -> new TableRowCustomIsItemChanged());
        pane.getTabs().add(new Tab("ItemChanged", isItemChanged));
        
        // tab with custom tableRow: add/remove listener to item's valid changes
        TableView<Item> listening = createTable(true);
        listening.setRowFactory(v -> new ItemTableRow());
        pane.getTabs().add(new Tab("listening", listening));
        
        // validity column not included
        // tab with custom TableRow: override isItemChanged
        TableView<Item> isItemChangedUncontained = createTable(false);
        isItemChangedUncontained.setRowFactory(v -> new TableRowCustomIsItemChanged());
        pane.getTabs().add(new Tab("ItemChanged, uncontained", isItemChangedUncontained));
        
        // tab with custom tableRow: add/remove listener to item's valid changes
        TableView<Item> listeningUncontained = createTable(false);
        listeningUncontained.setRowFactory(v -> new ItemTableRow());
        pane.getTabs().add(new Tab("listening uncontained", listeningUncontained));
        
        // custom tableRow with itemChanged, uncontained, use extractor
        // note: this doesn't share the data! though the content of the underlying list is shared, such that
        // the item state is toggled as well
        TableView<Item> itemChangedExtractor = createTable(false);
        ObservableList<Item> extractor = FXCollections.observableList(data, item -> new Observable[] {item.validProperty()});
        itemChangedExtractor.setItems(extractor);
        itemChangedExtractor.setRowFactory(v -> new TableRowCustomIsItemChanged());
        pane.getTabs().add(new Tab("itemChanged extractor", itemChangedExtractor)); 
        
        Button toggleFirst =  new Button("Toggle valid of first");
        toggleFirst.setOnAction(e -> data.get(0).toggleValid()); 
        
        BorderPane content = new BorderPane(pane);
        content.setBottom(new HBox(10, toggleFirst));
        return content;
    }
    
    /**
     * TableRow with custom isItemChanged: returns true if super returns true
     * or same item with changed valid. Needs an extractor on the items list
     * if the valid column is not included or not visible.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    private static class TableRowCustomIsItemChanged extends TableRow<Item> {
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

        private final ChangeListener<Boolean> listener = (obs, oldVal, newVal) -> updateValidPseudoClass(newVal);
        private final WeakChangeListener<Boolean> weakListener = new WeakChangeListener<>(listener);

        public ItemTableRow() {
          getStyleClass().add("item-table-row");
        }

        @Override
        protected void updateItem(Item item, boolean empty) {
          Item oldItem = getItem();
          if (oldItem != null) {
            oldItem.validProperty().removeListener(weakListener);
          }
          super.updateItem(item, empty);
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

    /**
     * Plain TableRow that updates its style in updateItem.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    private static class CoreTableRow extends TableRow<Item> {
        public CoreTableRow() {
            getStyleClass().add("item-table-row");
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

    private TableView<Item> createTable(boolean containsValid) {
        TableView<Item> table = new TableView<>(getData());
        table.setTableMenuButtonVisible(true);
        table.setEditable(true);

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(features -> features.getValue().nameProperty());
        table.getColumns().add(nameCol);

        if (containsValid) {
            TableColumn<Item, Boolean> validCol = new TableColumn<>("Valid");
            validCol.setCellFactory(CheckBoxTableCell.forTableColumn(validCol));
            validCol.setCellValueFactory(features -> features.getValue().validProperty());
            table.getColumns().add(validCol);
        }
        return table;
    }
    
    private ObservableList<Item> getData() {
       if (data == null) {
           data = createDummyData(50);
       }
       return data;
    }
    
    private ObservableList<Item> createDummyData(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> "Item #" + i)
                .map(name -> new Item(name, Math.random() >= 0.5))
                .collect(Collectors
                        .toCollection(FXCollections::observableArrayList));
    }

    /**
     * simple data class
     */
    private static class Item {

        private final StringProperty name = new SimpleStringProperty(this,
                "name");

        public final void setName(String name) {
            this.name.set(name);
        }

        public final String getName() {
            return name.get();
        }

        public final StringProperty nameProperty() {
            return name;
        }

        private final BooleanProperty valid = new SimpleBooleanProperty(this,
                "valid");

        public final void setValid(boolean valid) {
            this.valid.set(valid);
        }

        public final void toggleValid() {
            setValid(!isValid());
        };

        public final boolean isValid() {
            return valid.get();
        }

        public final BooleanProperty validProperty() {
            return valid;
        }

        public Item(String name, boolean valid) {
            setName(name);
            setValid(valid);
        }

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.getScene().getStylesheets().add(getClass().getResource("rowstylingpseudoclass.css").toExternalForm());
        String version = System.getProperty("java.version")+ "-" + System.getProperty("java.vm.version");
        stage.setTitle(version);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

/*
 content of rowstylingpseudoclass.css
 
 .item-table-row:selected {
    -fx-background-color: -fx-control-inner-background, green;
}

.item-table-row:valid {
    -fx-background-color: -fx-control-inner-background, yellow;
}

.item-table-row:valid:selected {
    -fx-background-color: -fx-control-inner-background, red;
}


     
 */
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(RowStylingUpdate.class.getName());

}
