/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * Utility methods to access skin internals.
 * Not much used ... 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SkinUtils {

    /**
     * Returns the VirtualFlow that's managed by the given VirtualContainerBase.
     * Implemented to lookup the flow by its style selector.
     * <p>
     * Note: this utility method (or any other hack) for access 
     * still needed in fx11 (and later), it's protected in VirtualContainerBase!
     * 
     * @param skin 
     * @return
     */
    public static VirtualFlow<?> getVirtualFlow(VirtualContainerBase<?, ?> skin) {
        VirtualFlow<?> flow = (VirtualFlow<?>) skin.getSkinnable().lookup((".virtual-flow"));
        return flow;
    }
    
    private SkinUtils() {};
}
