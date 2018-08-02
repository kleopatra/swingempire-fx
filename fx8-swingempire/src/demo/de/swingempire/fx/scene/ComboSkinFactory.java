/*
 * Created on 02.08.2018
 *
 */
package de.swingempire.fx.scene;

import de.swingempire.fx.scene.control.skin.ComboSkinDecorator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * Factory for various test scenarios around https://bugs.openjdk.java.net/browse/JDK-8197846.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboSkinFactory {

    /**
     * Creates and returns a plain core combobox skin with its show method overridden
     * to work around https://bugs.openjdk.java.net/browse/JDK-8196827
     * @param combo
     * @return
     */
    public static <T> ComboBoxListViewSkin<T> createCoreComboBoxSkin(ComboBox<T> combo) {
        return new CoreComboBoxListViewSkin<T>(combo);
    }

    /**
     * Plain core combobox skin with its show method overridden
     * to work around https://bugs.openjdk.java.net/browse/JDK-8196827
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class CoreComboBoxListViewSkin<T> extends ComboBoxListViewSkin<T> {
        
        /**
         * @param control
         */
        public CoreComboBoxListViewSkin(ComboBox<T> control) {
            super(control);
        }

        @Override
        public void show() {
            if (getSkinnable().getScene() == null) return;
            super.show();
        }
        
    }
    /**
     * Creates and returns a comboBox skin that registers a scene listener
     * on the combo to enforce sync of combo/popup to work around 
     * https://bugs.openjdk.java.net/browse/JDK-8197846
     * 
     * It extends CoreComboBoxSkin
     * to work around https://bugs.openjdk.java.net/browse/JDK-8196827
     * 
     * @author Jeanette Winzenburg, Berlin
     */
    public static class YComboBoxListViewSkin<T> extends CoreComboBoxListViewSkin<T>
//            implements ComboSkinDecorator 
            {

        /**
         * @param combo
         */
        public YComboBoxListViewSkin(ComboBox<T> combo) {
            super(combo);
            // listener to keep popup showing in sync with combo showing
            registerChangeListener(combo.sceneProperty(), e -> {
                if (combo.getScene() == null) {
                    // arguable:
                    // on removal, the popup is hidden,
                    // force combo to hide also
//                combo.hide();
                } else if (combo.isShowing()) {
                    // ensure popup is showing
                    show();
                }
            });
            // this listener (its fx8 equivalent) has been removed in
            // http://hg.openjdk.java.net/openjfx/8u40/rt/file/bc4910bf1984/modules/controls/src/main/java/com/sun/javafx/scene/control/skin/ComboBoxListViewSkin.java
            // has no effect, though - probably because the showing property
            // does not change on removal/re-addition
//        registerChangeListener(combo.showingProperty(), e -> {
//            if (combo.isShowing()) {
//                getPopupContent().setManaged(true);
//            } else {
//                getPopupContent().setManaged(false);
//            }
//        });

        }

        /**
         * Bugfix: super show assumes scene != null and throws NPE when opening.
         * Late detection (and fix) in
         * https://bugs.openjdk.java.net/browse/JDK-8196827
         */
        @Override
        public void show() {
            if (getSkinnable() == null || getSkinnable().getScene() == null) return;
            super.show();
        }
        
    }

    /**
     * Creates and returns an extended ComboBox skin that allows access to its
     * popup (via reflection ComboSkinDecorator) and enforces popup creation
     * in its constructor (why?)
     *  
     * @param combo
     * @return
     */
    public static <T> ComboBoxListViewSkin<T> createXComboBoxSkin(ComboBox<T> combo) {
        return new XComboBoxListViewSkin<>(combo);
    }
    
    /**
     * ComboSkin with access to its popup and enforced popup creation in contructor
     * (why?)
     * 
     * It extends CoreComboBoxSkin
     * to work around https://bugs.openjdk.java.net/browse/JDK-8196827
     */
    public static class XComboBoxListViewSkin<T> extends CoreComboBoxListViewSkin<T> 
        implements ComboSkinDecorator {

        public XComboBoxListViewSkin(ComboBox<T> combo) {
            super(combo);
            // enforce creation of the popup
            getPopupControl();
        }

       
    }

    private ComboSkinFactory() {
    }
}
