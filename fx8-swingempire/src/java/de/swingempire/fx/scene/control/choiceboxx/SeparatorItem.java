/*
 * Created on 28.09.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

/**
 * A tagging interface used to denote a separator.
 * 
 * Note: this is not really nice because 
 * <li> needs cooperation of both 
 * ChoiceBoxSelectionModel and ChoiceBoxSkin. The latter
 * is deeply buried in private code
 * <li> needs a dummy extension of the custom type 
 *   that implements it
 * <li> isn't applicable for primitive types, in particular String  
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface SeparatorItem {

}
