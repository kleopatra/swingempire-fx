/*
 * Created on 29.08.2017
 *
 */
package test.tableeditcore;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Can't really test, too many changes too deep in bowels..
 */
public class TableViewJon<S> extends TableView<S> {

    private InvalidationListener focusOwnerListener = o -> {
        if (!ControlUtils.isFocusOnNodeOrAnyChild(this)) {
            edit(-1, null);
        }
    };

    private WeakInvalidationListener weakFocusOwnerListener = new WeakInvalidationListener(
            focusOwnerListener);

    /**
     * 
     */
    public TableViewJon() {
        this(FXCollections.observableArrayList());
    }

    /**
     * @param arg0
     */
    public TableViewJon(ObservableList<S> arg0) {
        super(arg0);
        sceneProperty().addListener((o, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.focusOwnerProperty().removeListener(weakFocusOwnerListener);
            }
            if (newScene != null) {
                newScene.focusOwnerProperty().addListener(weakFocusOwnerListener);
            }
        });

    }

}
