/*
 * Created on 18.03.2020
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60730686/203657
 * highlight cell based on flag
 * 
 * there should be exactly one flag true in all items.
 */
public class TreeWithSelectableItem extends Application {

    /**
     * Specialized TreeItem with a selected property. Fires a 
     * TreeModificationEvent on change.
     */
    public static class SelectableTreeItem<T> extends TreeItem<T> {

        private BooleanProperty selected;
        
        public SelectableTreeItem() {
            this(null);
        }

        public SelectableTreeItem(T value) {
            super(value);
            selected = new SimpleBooleanProperty(this, "selected", false) {

                @Override
                protected void invalidated() {
                    selectedChanged();
                }
                
            };
        }
        
        public BooleanProperty selectedProperty() {
            return selected;
        }
        
        public boolean isSelected() {
            return selectedProperty().get();
        }
        
        public void setSelected(boolean selected) {
            selectedProperty().set(selected);
        }
        
        protected void selectedChanged() {
            // tbd: update the value properties if needed
            Event.fireEvent(this, new TreeModificationEvent<T>(valueChangedEvent(), this));
        }
        
    }
    
    /**
     * Specialized SingleSelectionModel that updates the selected property of 
     * contained SelectableTreeItems.
     * 
     */
    public static class SelectableTreeItemToggler<T> extends SingleSelectionModel<SelectableTreeItem<T>> {

        private ObservableList<SelectableTreeItem<T>> items;

        public SelectableTreeItemToggler(ObservableList<SelectableTreeItem<T>> items) {
            this.items = items;
            // tbd: listen to modification of items list
            // listen to changes of our selectedItem and update the state of the treeItems
            selectedItemProperty().addListener((src, ov, nv) -> selectedItemChanged(ov));
        }
        
        /**
         * Callback from notification on selectedItem.
         * 
         */
        protected void selectedItemChanged(SelectableTreeItem<T> oldSelected) {
            if (oldSelected != null) {
                oldSelected.setSelected(false);
            } 
            if (getSelectedItem() != null) {
                getSelectedItem().setSelected(true);
            }
        }

        @Override
        protected SelectableTreeItem<T> getModelItem(int index) {
            return items.get(index);
        }

        @Override
        protected int getItemCount() {
            return items.size();
        }
        
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeView<TreeData> treeView = new TreeView<>();
        treeView.setEditable(true);

        ObservableList<SelectableTreeItem<TreeData>> selectableItems = FXCollections.observableArrayList();
        TreeItem<TreeData> root = new TreeItem<>(new TreeData("root", "root"));
        root.setExpanded(true);
        for (int i = 0; i <= 4; i++) {
            // use custom treeItem for level1
            SelectableTreeItem<TreeData> level1 = new SelectableTreeItem<>(new TreeData("Number " + i, "level"));
            level1.setExpanded(true);
            // add to selected items
            selectableItems.add(level1);
            for (int j = 0; j <= 2; j++)
                level1.getChildren().add(new TreeItem<>(new TreeData("Subnumber " + i + "." + j, "child")));
            root.getChildren().add(level1);
        }
        treeView.setRoot(root);

        // create custom selectionModel
        SelectableTreeItemToggler<TreeData> sm = new SelectableTreeItemToggler<>(selectableItems);
        treeView.setCellFactory(tv -> {
            TreeCellImpl treeCell = new TreeCellImpl();

            treeCell.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
                TreeData cellItem = treeCell.getItem();
                TreeItem<TreeData> treeItem = treeCell.getTreeItem();
                // disable double click expand/collapse
                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
                    if (cellItem.type.equals("root") || treeItem instanceof SelectableTreeItem)
                        e.consume();
                }
                // on one double click
                if (e.getClickCount() == 2 && e.getButton().equals(MouseButton.PRIMARY)
                        && treeItem instanceof SelectableTreeItem) {
                    sm.select((SelectableTreeItem<TreeData>) treeItem);
                }
            });

            return treeCell;
        });

        primaryStage.setScene(new Scene(treeView));
        primaryStage.show();
    }

    private final class TreeCellImpl extends TreeCell<TreeData> {
        
      @Override
      protected void updateItem(TreeData item, boolean empty) {
          super.updateItem(item, empty);
          if (empty || item == null) {
              setText(null);
          } else {
              setText(item.toString());
              if (getTreeItem() instanceof SelectableTreeItem) {
                  SelectableTreeItem<TreeData> treeItem = (SelectableTreeItem<TreeData>) getTreeItem();
                  setTextFill(treeItem.isSelected() ? Color.GREEN : Color.BLACK);
              } else {
                  setTextFill(Color.BLACK);
              }
          }
          setGraphic(null);
      }
        
    }
    
    private static final class TreeData {
        String text;
        String type;
        boolean flag;

        public TreeData(String text, String type) {
            this.text = text;
            this.type = type;
        }

        @Override
        public String toString() {
            return text;
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}

