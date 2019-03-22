/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.scene.control.skin.impl;

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
    
    /**
     * Returns the virtualFlow of this containerBase. Accessed refectively: needed
     * even in later versions of fx, because its scope is protected.
     * 
     * @param skin
     * @return
     */
    public static VirtualFlow<?> getVirtualFlow(VirtualContainerBase<?, ?> skin) {
        return (VirtualFlow<?>) FXUtils.invokeGetMethodValue(VirtualContainerBase.class, skin, "getVirtualFlow");
    }
    
    private SkinUtils() {};
}
