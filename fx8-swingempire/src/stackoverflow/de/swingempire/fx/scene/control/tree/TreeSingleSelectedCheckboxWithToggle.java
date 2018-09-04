/*
 * Created on 03.09.2018
 *
 */
package de.swingempire.fx.scene.control.tree;

import java.util.ArrayList;
import java.util.List;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableMap;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/52139475/203657
 * select a single treeItem only 
 * 
 * here: experiment with a treeItem that's implements Toggle
 * answered
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSingleSelectedCheckboxWithToggle extends Application {
    
    /**
     * A custom CheckBoxTreeItem that implements Toggle.
     * 
     * To control which/how many items can be selected at any
     * time, add them to one or several ToggleGroups.
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class ToggleTreeItem<T> extends CheckBoxTreeItem<T>
        implements Toggle {

        
        public ToggleTreeItem() {
            super();
            init();
        }

        public ToggleTreeItem(T value) {
            super(value);
            init();
        }

        private void init() {
            selectedProperty().addListener(ov -> {
                final boolean selected = isSelected();
                final ToggleGroup tg = getToggleGroup();
                // Note: these changes need to be done before selectToggle/clearSelectedToggle since
                // those operations change properties and can execute user code, possibly modifying selected property again
                if (tg != null) {
                    if (selected) {
                        tg.selectToggle(ToggleTreeItem.this);
                    } else if (tg.getSelectedToggle() == ToggleTreeItem.this) {
                        FXUtils.invokeMethod(ToggleGroup.class, tg, "clearSelectedToggle");
//                        tg.clearSelectedToggle();
                    }
                }

            });
        }
        /**
         * The {@link ToggleGroup} to which this {@code ToggleButton} belongs. A
         * {@code ToggleButton} can only be in one group at any one time. If the
         * group is changed, then the button is removed from the old group prior to
         * being added to the new group.
         */
        private ObjectProperty<ToggleGroup> toggleGroup;
        @Override
        public final void setToggleGroup(ToggleGroup value) {
            toggleGroupProperty().set(value);
        }

        @Override
        public final ToggleGroup getToggleGroup() {
            return toggleGroup == null ? null : toggleGroup.get();
        }

        @Override
        public final ObjectProperty<ToggleGroup> toggleGroupProperty() {
            if (toggleGroup == null) {
                toggleGroup = new ObjectPropertyBase<ToggleGroup>() {
                    private ToggleGroup old;
                    @Override protected void invalidated() {
                        final ToggleGroup tg = get();
                        if (tg != null && !tg.getToggles().contains(ToggleTreeItem.this)) {
                            if (old != null) {
                                old.getToggles().remove(ToggleTreeItem.this);
                            }
                            tg.getToggles().add(ToggleTreeItem.this);
                        } else if (tg == null) {
                            old.getToggles().remove(ToggleTreeItem.this);
                        }

                        old = tg;
                    }

                    @Override
                    public Object getBean() {
                        return ToggleTreeItem.this;
                    }

                    @Override
                    public String getName() {
                        return "toggleGroup";
                    }
                };
            }
            return toggleGroup;
        }
        @Override
        public Object getUserData() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setUserData(Object value) {
            // TODO Auto-generated method stub
        }

        @Override
        public ObservableMap<Object, Object> getProperties() {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {

        ToggleGroup toggleGroup = new ToggleGroup();
        ToggleTreeItem<String> rootItem = new ToggleTreeItem<>("Root");
        toggleGroup.getToggles().add(rootItem);
        final List<ToggleTreeItem<String>> treeItems = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ToggleTreeItem<String> item = new ToggleTreeItem<>("L0" + i + "");
            item.setIndependent(true);
            treeItems.add(item);
            toggleGroup.getToggles().add(item);
            myList.add(new Dependant("0" + i + "", "type1"));
        }
        rootItem.getChildren().addAll(treeItems);

        rootItem.setExpanded(true);
        rootItem.setIndependent(true);
        ToggleTreeItem<String> rootItem2 = new ToggleTreeItem<>("folder");
        toggleGroup.getToggles().add(rootItem2);
        final List<ToggleTreeItem<String>> treeItems2 = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            ToggleTreeItem<String> item = new ToggleTreeItem<>("L1" + i + "");
            item.setIndependent(true);
            treeItems2.add(item);
            toggleGroup.getToggles().add(item);
            myList.add(new Dependant("0" + i + "", "type2"));
        }
        rootItem2.getChildren().addAll(treeItems2);
        rootItem2.setIndependent(true);
        rootItem.getChildren().set(2, rootItem2);

        TreeView tree = new TreeView<>(rootItem);

        tree.setCellFactory(CheckBoxTreeCell.forTreeView());

        tree.setRoot(rootItem);

        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

    public static class Dependant {

        String one;
        String two;
        public Dependant(String one, String two) {
            this.one = one;
            this.two = two;
        }
        
    }
    
    private List<Dependant> myList = new ArrayList();


    public static void main(String[] args) {
        launch(args);
    }

}

