/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.util.impl;

import de.swingempire.fx.util.FXUtils;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Class accessing skin internals, isolates version specifics.
 * <p>
 * Note: giving up on fx8 for now, because the changed package of the skin classes would leak.
 * Not a big loss -mostly unused ?
 * <p>
 * 
 *  PENDING: accessing FXUtils, that is bidi package dependency. Think about moving
 *  as ReflectUtils into this impl package.
 *  
 * @author Jeanette Winzenburg, Berlin
 */
public class SkinUtils {
    
    public static VirtualFlow<?> getVirtualFlow(VirtualContainerBase<?, ?> skin) {
        return (VirtualFlow<?>) FXUtils.invokeGetMethodValue(VirtualContainerBase.class, skin, "getVirtualFlow");
    }
    
    private SkinUtils() {};
}
