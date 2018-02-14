/*
 * Created on 13.02.2018
 *
 */
package de.swingempire.fx.scene.control.skin;

import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SkinUtils {

    public static VirtualFlow<?> getVirtualFlow(VirtualContainerBase<?, ?> skin) {
        return de.swingempire.fx.scene.control.skin.impl.SkinUtils.getVirtualFlow(skin);
    }
    
    private SkinUtils() {};
}
