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
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeSingleSelectedCheckboxWithToggle extends Application {
    public static void main(String[] args) {
        launch(args);
    }

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

        public final ObjectProperty<ToggleGroup> toggleGroupProperty() {
            if (toggleGroup == null) {
                toggleGroup = new ObjectPropertyBase<ToggleGroup>() {
                    private ToggleGroup old;
//                    private ChangeListener<Toggle> listener = (o, oV, nV) ->
//                        ParentHelper.getTraversalEngine(ToggleButton.this).setOverriddenFocusTraversability(nV != null ? isSelected() : null);

                    @Override protected void invalidated() {
                        final ToggleGroup tg = get();
                        if (tg != null && !tg.getToggles().contains(ToggleTreeItem.this)) {
                            if (old != null) {
                                old.getToggles().remove(ToggleTreeItem.this);
                            }
                            tg.getToggles().add(ToggleTreeItem.this);
//                            final ParentTraversalEngine parentTraversalEngine = new ParentTraversalEngine(ToggleButton.this);
//                            ParentHelper.setTraversalEngine(ToggleButton.this, parentTraversalEngine);
                            // If there's no toggle selected, do not override
//                            parentTraversalEngine.setOverriddenFocusTraversability(tg.getSelectedToggle() != null ? isSelected() : null);
//                            tg.selectedToggleProperty().addListener(listener);
                        } else if (tg == null) {
//                            old.selectedToggleProperty().removeListener(listener);
                            old.getToggles().remove(ToggleTreeItem.this);
//                            ParentHelper.setTraversalEngine(ToggleButton.this, null);
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
    
    public static class Dependant {

        String one;
        String two;
        public Dependant(String one, String two) {
            this.one = one;
            this.two = two;
        }
        
        
    }
    
    
    private List<Dependant> myList = new ArrayList();

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage primaryStage) {

        ToggleTreeItem<String> rootItem = new ToggleTreeItem("Root");
        final List<ToggleTreeItem<String>> treeItems = new ArrayList(6);
        ToggleGroup group1 = new ToggleGroup();
        for (int i = 0; i < 6; i++) {
            ToggleTreeItem<String> item = new ToggleTreeItem("L0" + i + "");
            item.setIndependent(true);
            treeItems.add(item);
            group1.getToggles().add(item);
            myList.add(new Dependant("0" + i + "", "type1"));
        }
        rootItem.getChildren().addAll(treeItems);

        rootItem.setExpanded(true);
        rootItem.setIndependent(true);
        ToggleTreeItem<String> rootItem2 = new ToggleTreeItem("folder");
        final List<ToggleTreeItem<String>> treeItems2 = new ArrayList(6);
        for (int i = 0; i < 6; i++) {
            ToggleTreeItem<String> item = new ToggleTreeItem("L1" + i + "");
            item.setIndependent(true);
            treeItems2.add(item);
            myList.add(new Dependant("0" + i + "", "type2"));
        }
        rootItem2.getChildren().addAll(treeItems2);
        rootItem2.setIndependent(true);
        rootItem.getChildren().set(2, rootItem2);

        TreeView tree = new TreeView(rootItem);

        tree.setCellFactory((Object item) -> {

            final CheckBoxTreeCell<String> cell = new CheckBoxTreeCell();

//            cell.itemProperty().addListener((obs, s, s1) -> {
//
//                cell.disableProperty().unbind();
//                if (s1 != null && !s1.isEmpty()) {
//                    BooleanProperty prop = new SimpleBooleanProperty();
//                    prop.set((s1.equals("folder")));
//                    cell.disableProperty().bind(prop);
//                }
//            });
            return cell;
        });

        tree.setRoot(rootItem);

        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }

}

