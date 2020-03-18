/*
 * Created on 18.03.2020
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
public class TreeCellHighlightOnValue extends Application {

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
            Event.fireEvent(this, new TreeModificationEvent<T>(valueChangedEvent(), this, getValue()));
        }
        
    }
    
    public static class TreeItemSelectionModel extends SingleSelectionModel<SelectableTreeItem> {

        private ObservableList<SelectableTreeItem> items;

        public TreeItemSelectionModel(ObservableList<SelectableTreeItem> items) {
            this.items = items;
            // tbd: listen to changes of list
            selectedItemProperty().addListener((src, ov, nv) -> selectedItemChanged(ov));
        }
        
        /**
         * @param ov
         * @return
         */
        protected void selectedItemChanged(SelectableTreeItem oldSelected) {
            if (oldSelected != null) {
                oldSelected.setSelected(false);
            } 
            if (getSelectedItem() != null) {
                getSelectedItem().setSelected(true);
            }
        }

        @Override
        protected SelectableTreeItem getModelItem(int index) {
            return items.get(index);
        }

        @Override
        protected int getItemCount() {
            return items.size();
        }
        
    }
//    private TreeCell<TreeData> previous;

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeView<TreeData> treeView = new TreeView<>();
        treeView.setEditable(true);

        TreeItem<TreeData> root = new TreeItem<>(new TreeData("root", "root"));
        root.setExpanded(true);
        for (int i = 0; i <= 4; i++) {
            TreeItem<TreeData> level1 = new TreeItem<>(new TreeData("Number " + i, "level"));
            level1.setExpanded(true);
            for (int j = 0; j <= 2; j++)
                level1.getChildren().add(new TreeItem<>(new TreeData("Subnumber " + i + "." + j, "child")));
            root.getChildren().add(level1);
        }
        treeView.setRoot(root);

        treeView.setCellFactory(tv -> {
            TreeCellImpl treeCell = new TreeCellImpl();

            treeCell.addEventFilter(MouseEvent.MOUSE_PRESSED, (MouseEvent e) -> {
                TreeData cellItem = ((TreeCellImpl) e.getSource()).getItem();
                // disable double click expand/collapse
                if (e.getClickCount() % 2 == 0 && e.getButton().equals(MouseButton.PRIMARY)) {
                    if (cellItem.type.equals("root") || cellItem.type.equals("level"))
                        e.consume();
                }
                // on one double click
                if (e.getClickCount() == 2 && e.getButton().equals(MouseButton.PRIMARY)
                        && cellItem.type.equals("level")) {
                    // go into edit mode
                    treeView.edit(((TreeCellImpl) e.getSource()).getTreeItem());
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
            boolean editable = !empty && item != null && item.type.equals("level");
            setEditable(editable);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item.toString());
                setTextFill(Color.BLACK);
//                if (getItem().type.equals("level") && getItem().flag)
//                    setTextFill(Color.GREEN);
            }
            setGraphic(null);
        }

        @Override
        public void startEdit() {
            super.startEdit();
            if (isEditing()) {
              setTextFill(Color.GREEN);
            } else {
                setTextFill(Color.BLACK);
            }
            System.out.println("EDIT started " + getItem() + isEditing());
//            if (getItem().type.equals("level")) {
//                if (previous != null)
//                    previous.cancelEdit();
//                previous = this;
//                getItem().flag = true;
//                commitEdit(getItem());
//            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            System.out.println("CANCEL");
            setTextFill(Color.BLACK);
//            if (getItem().type.equals("level")) {
//                getItem().flag = false;
//                commitEdit(getItem());
//            }
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

