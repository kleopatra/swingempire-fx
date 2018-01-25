/*
 * Created on 25.01.2018
 *
 */
package control.skin;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;

/**
 * TableViewSkin that doesn't show the placeholder.
 * The basic trick is keep the placeholder/flow in-/visible at all 
 * times (similar to https://stackoverflow.com/a/27543830/203657).
 * <p> 
 * 
 * Updated for fx9 plus ensure to update the layout of the flow as
 * needed.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class NoPlaceHolderTableViewSkin<T> extends TableViewSkin<T>{

    private VirtualFlow<?> flowAlias;
    private TableHeaderRow headerAlias;
    private Parent placeholderRegionAlias;
    private ChangeListener<Boolean> visibleListener = (src, ov, nv) -> visibleChanged(nv);
    private ListChangeListener<Node> childrenListener = c -> childrenChanged(c);
    
    /**
     * Instantiates the skin.
     * @param table the table to skin.
     */
    public NoPlaceHolderTableViewSkin(TableView<T> table) {
        super(table);
        flowAlias = (VirtualFlow<?>) table.lookup(".virtual-flow");
        headerAlias = (TableHeaderRow) table.lookup(".column-header-background");
        
        // startet with a not-empty list, placeholder not yet instantiatet
        // so add alistener to the children until it will be added
        if (!installPlaceholderRegion(getChildren())) {
            installChildrenListener();
        }
    }


    /**
     * Searches the given list for a Parent with style class "placeholder" and
     * wires its visibility handling if found.
     * @param addedSubList
     * @return true if placeholder found and installed, false otherwise.
     */
    protected boolean installPlaceholderRegion(
            List<? extends Node> addedSubList) {
        if (placeholderRegionAlias !=  null) 
            throw new IllegalStateException("placeholder must not be installed more than once");
        List<Node> parents = addedSubList.stream()
                .filter(e -> e.getStyleClass().contains("placeholder"))
                .collect(Collectors.toList());
        if (!parents.isEmpty()) {
            placeholderRegionAlias = (Parent) parents.get(0);
            placeholderRegionAlias.visibleProperty().addListener(visibleListener);
            visibleChanged(true);
            return true;
        }
        return false;
    }


    protected void visibleChanged(Boolean nv) {
        if (nv) {
            flowAlias.setVisible(true);
            placeholderRegionAlias.setVisible(false);
        }
    }


    /**
     * Layout of flow unconditionally.
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    protected void layoutFlow(double x, double y, double width,
            double height) {
        // super didn't layout the flow if empty- do it now
        final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
        double headerHeight = headerAlias.getHeight();
        y += headerHeight;
        double flowHeight = Math.floor(height - headerHeight);
        layoutInArea(flowAlias, x, y,
                width, flowHeight,
                baselineOffset, HPos.CENTER, VPos.CENTER);
    }


    /**
     * Returns a boolean indicating whether the flow should be layout.
     * This implementation returns true if table is empty.
     * @return
     */
    protected boolean shouldLayoutFlow() {
        return getItemCount() == 0;
    }


    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to layout the flow always.
     */
    @Override
    protected void layoutChildren(double x, double y, double width,
            double height) {
        super.layoutChildren(x, y, width, height);
        if (shouldLayoutFlow()) {
            layoutFlow(x, y, width, height);
            
        }
    }

    /**
     * Listener callback from children modifications.
     * Meant to find the placeholder when it is added.
     * This implementation passes all added sublists to 
     * hasPlaceHolderRegion for search and install the 
     * placeholder. Removes itself as listener if installed.
     * 
     * @param c the change 
     */
    protected void childrenChanged(Change<? extends Node> c) {
        while (c.next()) {
            if (c.wasAdded()) {
                if (installPlaceholderRegion(c.getAddedSubList())) {
                    uninstallChildrenListener();
                    return;
                }
                
            }
        }
    }


    /**
     * Installs a ListChangeListener on the children which calls
     * childrenChanged on receiving change notification. 
     * 
     */
    protected void installChildrenListener() {
        getChildren().addListener(childrenListener);
    }
    
    /**
     * Uninstalls a ListChangeListener on the children:
     */
    protected void uninstallChildrenListener() {
        getChildren().removeListener(childrenListener);
    }
    
    
}