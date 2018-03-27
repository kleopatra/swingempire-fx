/*
 * Created on 05.09.2017
 *
 */
package de.swingempire.fx.scene.control.edit;


import de.swingempire.fx.scene.control.ControlUtils;
import de.swingempire.fx.scene.control.cell.CellDecorator;
import de.swingempire.fx.scene.control.cell.TextFieldCellDecorator;
import de.swingempire.fx.scene.control.edit.TreeViewEditCommitInterfereHack.XTextFieldTreeCell;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * Hacking around core bug with a custom TreeCell, that updates its item
 * from the backing tree vs. the new value. Seems to work, but not
 * formally tested!
 * 
 * ---- the issue:
 * TreeViewCell: must not interfere with custom commit handler
 * reported:
 * https://bugs.openjdk.java.net/browse/JDK-8187309
 */
public class TreeViewEditCommitInterfereHack extends Application {
    
    /**
     * Custom TreeCell that shows
     */
    public static class XTreeCell<T> extends TreeCell<T> implements CellDecorator<TreeView<T>, T> {
        
        @Override
        public void commitEdit(T newValue) {
            if (! isEditing()) return;
            final TreeItem<T> treeItem = getTreeItem();
            final TreeView<T> tree = getTreeView();
            if (tree != null) {
                // Inform the TreeView of the edit being ready to be committed.
                tree.fireEvent(new TreeView.EditEvent<T>(tree,
                        TreeView.<T>editCommitEvent(),
                        treeItem,
                        getItem(),
                        newValue));
            }

            // inform parent classes of the commit, so that they can switch us
            // out of the editing state.
            // This MUST come before the updateItem call below, otherwise it will
            // call cancelEdit(), resulting in both commit and cancel events being
            // fired (as identified in RT-29650)
            cellCommitEdit(newValue);

            // update the item within this cell, so that it represents the new value
            // this is what core does ... must not! may lead to data-corruption
//            if (treeItem != null) {
//                treeItem.setValue(newValue);
//                updateTreeItem(treeItem);
//                updateItem(newValue, false);
//            }
            updateItem(treeItem.getValue(), false);
            
            if (tree != null) {
                // reset the editing item in the TreetView
                tree.edit(null);

                // request focus back onto the tree, only if the current focus
                // owner has the tree as a parent (otherwise the user might have
                // clicked out of the tree entirely and given focus to something else.
                // It would be rude of us to request it back again.
                ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tree);
            }
        }

        @Override
        public ReadOnlyObjectProperty<TreeView<T>> controlProperty() {
            return treeViewProperty();
        }


    }

    private TreeItem inEditCommit;
    
    @Override
    public void start(Stage stage) throws Exception {
        TreeItem<String> rootItem = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child", new Label("X"));
        rootItem.getChildren().add(child);
        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(XTextFieldTreeCell.forTreeView());
        // custom commit handler: replace value on some condition only
        treeView.setOnEditCommit(t -> {
            String ov = t.getOldValue();
            String nv = t.getNewValue();
            if (nv.length() > ov.length()) {
                t.getTreeItem().setValue(nv);
            }
            inEditCommit = t.getTreeItem();
        });

        Button check = new Button("Print child value");
        check.setOnAction(e -> System.out.println("" + child + (inEditCommit == child)));
        BorderPane pane = new BorderPane(treeView);
        pane.setBottom(check);
        Scene scene = new Scene(pane, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * c&p of core TextFieldTreeCell, no change
     */
    public static class XTextFieldTreeCell<T> extends XTreeCell<T> 
        implements TextFieldCellDecorator<TreeView<T>, T> {
        /***************************************************************************
         *                                                                         *
         * Static cell factories                                                   *
         *                                                                         *
         **************************************************************************/
    
        /**
         * Provides a {@link TextField} that allows editing of the cell content when
         * the cell is double-clicked, or when
         * {@link TreeView#edit(javafx.scene.control.TreeItem)} is called.
         * This method will only work on {@link TreeView} instances which are of
         * type String.
         *
         * @return A {@link Callback} that can be inserted into the
         *      {@link TreeView#cellFactoryProperty() cell factory property} of a
         *      TreeView, that enables textual editing of the content.
         */
        public static Callback<TreeView<String>, TreeCell<String>> forTreeView() {
            return forTreeView(new DefaultStringConverter());
        }
    
        /**
         * Provides a {@link TextField} that allows editing of the cell content when
         * the cell is double-clicked, or when
         * {@link TreeView#edit(javafx.scene.control.TreeItem)} is called. This
         * method will work on any {@link TreeView} instance,
         * regardless of its generic type. However, to enable this, a
         * {@link StringConverter} must be provided that will convert the given String
         * (from what the user typed in) into an instance of type T. This item will
         * then be passed along to the {@link TreeView#onEditCommitProperty()}
         * callback.
         *
         * @param <T> The type of the elements contained within the TreeView
         * @param converter A {@link StringConverter} that can convert the given String
         *      (from what the user typed in) into an instance of type T.
         * @return A {@link Callback} that can be inserted into the
         *      {@link TreeView#cellFactoryProperty() cell factory property} of a
         *      TreeView, that enables textual editing of the content.
         */
        public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(
                final StringConverter<T> converter) {
            return list -> new XTextFieldTreeCell<T>(converter);
        }
    
    
    
        /***************************************************************************
         *                                                                         *
         * Fields                                                                  *
         *                                                                         *
         **************************************************************************/
    
        private TextField textField;
        private HBox hbox;
    
    
    
        /***************************************************************************
         *                                                                         *
         * Constructors                                                            *
         *                                                                         *
         **************************************************************************/
    
        /**
         * Creates a default TextFieldTreeCell with a null converter. Without a
         * {@link StringConverter} specified, this cell will not be able to accept
         * input from the TextField (as it will not know how to convert this back
         * to the domain object). It is therefore strongly encouraged to not use
         * this constructor unless you intend to set the converter separately.
         */
        public XTextFieldTreeCell() {
            this(null);
        }
    
        /**
         * Creates a TextFieldTreeCell that provides a {@link TextField} when put
         * into editing mode that allows editing of the cell content. This method
         * will work on any TreeView instance, regardless of its generic type.
         * However, to enable this, a {@link StringConverter} must be provided that
         * will convert the given String (from what the user typed in) into an
         * instance of type T. This item will then be passed along to the
         * {@link TreeView#onEditCommitProperty()} callback.
         *
         * @param converter A {@link StringConverter converter} that can convert
         *      the given String (from what the user typed in) into an instance of
         *      type T.
         */
        public XTextFieldTreeCell(StringConverter<T> converter) {
            this.getStyleClass().add("text-field-tree-cell");
            setConverter(converter);
        }
    
    
    
        /***************************************************************************
         *                                                                         *
         * Properties                                                              *
         *                                                                         *
         **************************************************************************/
    
        // --- converter
        private ObjectProperty<StringConverter<T>> converter =
                new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    
        /**
         * The {@link StringConverter} property.
         * @return the {@link StringConverter} property
         */
        public final ObjectProperty<StringConverter<T>> converterProperty() {
            return converter;
        }
    
    
        /***************************************************************************
         *                                                                         *
         * Public API                                                              *
         *                                                                         *
         **************************************************************************/
    
        @Override
        public TextField getTextField() {
            if (textField == null) {
                textField = createTextField();
            }
            return textField;
        }

        /** {@inheritDoc} */
        @Override public void startEdit() {
            if (! isEditable() || ! getTreeView().isEditable()) {
                return;
            }
            super.startEdit();
    
            if (isEditing()) {
                startEditTextField();
//                StringConverter<T> converter = getConverter();
//                if (textField == null) {
//                    textField = CellUtils.createTextField(this, converter);
//                }
//                if (hbox == null) {
//                    hbox = new HBox(3);//CellUtils.TREE_VIEW_HBOX_GRAPHIC_PADDING);
//                }
//    
//                CellUtils.startEdit(this, converter, hbox, getTreeItemGraphic(), textField);
            }
        }
    
        /** {@inheritDoc} */
        @Override 
        public void cancelEdit() {
            // super handles setting not-editing state on this cell and on the virtual control
            super.cancelEdit();
            cancelEditTextField();
//            CellUtils.cancelEdit(this, getConverter(), getTreeItemGraphic());
        }
    
        @Override
        public void cancelEditTextField() {
            
        }
        /** {@inheritDoc} */
        @Override public void updateItem(T item, boolean empty) {
            cellUpdateItem(item, empty);
//            super.updateItem(item, empty);
            updateItemTextField(item, empty);
//            CellUtils.updateItem(this, getConverter(), hbox, getTreeItemGraphic(), textField);
        }
    
    
    
        /***************************************************************************
         *                                                                         *
         * Private Implementation                                                  *
         *                                                                         *
         **************************************************************************/
    
        private Node getTreeItemGraphic() {
            TreeItem<T> treeItem = getTreeItem();
            return treeItem == null ? null : treeItem.getGraphic();
        }
    
    }
}