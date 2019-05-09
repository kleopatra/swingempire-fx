/*
 * Created on 05.05.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public interface CopyCellDecorator {

    default String getCopyText() {
        return getText();
    }
    
    abstract String getText();
}
