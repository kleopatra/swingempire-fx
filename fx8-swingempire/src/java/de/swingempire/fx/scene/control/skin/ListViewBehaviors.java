/*
 * Created on 17.03.2016
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.util.Callback;

/**
 * A bunch of setters that allow ListViewSkin to inject
 * custom behavior.
 * 
 * PENDING JW: this is simply extracted from core ListViewBehavior,
 * not well specified.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface ListViewBehaviors {

    public void setOnScrollPageUp(Callback<Boolean, Integer> c);
    public void setOnScrollPageDown(Callback<Boolean, Integer> c);
    public void setOnFocusPreviousRow(Runnable r);
    public void setOnFocusNextRow(Runnable r);
    public void setOnSelectPreviousRow(Runnable r);
    public void setOnSelectNextRow(Runnable r);
    public void setOnMoveToFirstCell(Runnable r);
    public void setOnMoveToLastCell(Runnable r);

}
