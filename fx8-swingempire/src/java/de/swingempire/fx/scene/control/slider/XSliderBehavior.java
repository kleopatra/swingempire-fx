/*
 * Created on 04.12.2015
 *
 */
package de.swingempire.fx.scene.control.slider;

import com.sun.javafx.scene.control.behavior.SliderBehavior;

import javafx.scene.control.Slider;

/**
 * Experimenting with using NumberAxis for all value/coordinate mapping.
 * Adding methods that take the new value (vs. a bare-bones mouseEvent)
 * 
 * <p> 
 * 
 * Note: the mouseEvent isn't used in the track/thumbPress methods anyway!
 * And it is not useful (at least not in the trackXX) because there's an
 * offset to the track that the behaviour can't know of.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XSliderBehavior extends SliderBehavior {

    /**
     * @param slider
     */
    public XSliderBehavior(Slider slider) {
        super(slider);
    }

//-------------- new methods
    
    public void valueUpdateByTrack(double value) {
        if (!getControl().isFocused()) getControl().requestFocus();
        getControl().adjustValue(value);
    }
    
    /**
     * Rename of super.getNode(). PENDING JW: cleanup
     * @return
     */
    private Slider getControl() {
        return getNode();
    }

    public void thumbPressed(double value) {
        if (!getControl().isFocused()) getControl().requestFocus();
        getControl().setValueChanging(true);
    }
    
    public void thumbDragged(double value) {
        getControl().setValue(value);    
    }
    
    public void thumbReleased(double value) {
        // simply delegate to method with mouseEvent (isnÄt used anyway
        thumbReleased(null);
//        getControl().setValueChanging(false);
//        if (getControl().isSnapToTicks()) {
//            // can't, snapping is private
//            getControl().setValue(snapValueToTicks(getControl().getValue()));
//        }
    }
}
