/*
 * Created on 13.10.2017
 *
 */
package de.swingempire.fx.control;

import java.util.Map;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TitledPane;
import javafx.scene.control.skin.AccordionSkin;
import javafx.scene.control.skin.TitledPaneSkin;
import javafx.stage.Stage;

/**
 * Trying to allow multiple expanded titledPanes. Layout code 
 * broken ...
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TitledPaneSample extends Application {
    final String[] imageNames = new String[]{"Apples", "Flowers", "Leaves"};
    final TitledPane[] tps = new TitledPane[imageNames.length];
           
    public static class MyAccordionSkin extends AccordionSkin {

        public MyAccordionSkin(Accordion control) {
            super(control);
            control.getPanes().addListener((ListChangeListener<TitledPane>) c ->{
               panesChanged(c); 
            });
            panesChanged(null);
        }
        
        /** {@inheritDoc} */
        @Override 
        protected void layoutChildren(final double x, double y,
                final double w, final double h) {
            final boolean rebuild = true; //forceRelayout || (relayout && previousHeight != h);
//            forceRelayout = false;
//            previousHeight = h;

            // Compute height of all the collapsed panes
            double collapsedPanesHeight = 0;
            for (TitledPane tp : getSkinnable().getPanes()) {
                if (!tp.isExpanded()) {
//                    TitledPaneSkin childSkin = (TitledPaneSkin) ((TitledPane)tp).getSkin();
                    collapsedPanesHeight += snapSizeY(getTitleRegionSize(tp, w));
                }
            }
            final double maxTitledPaneHeight = h - collapsedPanesHeight;

            for (TitledPane tp : getSkinnable().getPanes()) {
                double ph = getTitledPaneHeightForAccordion(tp, maxTitledPaneHeight, w);
                tp.resize(w, ph);

                boolean needsRelocate = true;
                if (needsRelocate) {
                    tp.relocate(x, y);
                    y += ph;
                }
            }
        }

        /**
         * @param tp
         * @param maxTitledPaneHeight
         * @return
         */
        private double getTitledPaneHeightForAccordion(TitledPane tp,
                double maxTitledPaneHeight, double w) {
            Skin skin = tp.getSkin();
            double ph;
            if (skin instanceof TitledPaneSkin) {
                setMaxTitledPaneHeightForAccordion((TitledPaneSkin) skin, maxTitledPaneHeight);
                return getTitledPaneHeightForAccordion((TitledPaneSkin) skin);
//                ((TitledPaneSkin)skin).setMaxTitledPaneHeightForAccordion(maxTitledPaneHeight);
//                ph = snapSizeY(((TitledPaneSkin)skin).getTitledPaneHeightForAccordion());
            } else {
                return tp.prefHeight(w);
            }
            
        }

        private void setMaxTitledPaneHeightForAccordion(TitledPaneSkin skin, double maxTitledPaneHeight) {
            FXUtils.invokeGetMethodValue(TitledPaneSkin.class, skin, "setMaxTitledPaneHeightForAccordion", Double.TYPE, maxTitledPaneHeight);
        }
        
        private double getTitledPaneHeightForAccordion(TitledPaneSkin skin) {
            return (double) FXUtils.invokeGetMethodValue(TitledPaneSkin.class, skin, "getTitledPaneHeightForAccordion");
        }
        
        /** {@inheritDoc} */
        @Override 
        protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            double h = 0;
            
            for (Node child: getChildren()) {
                TitledPane pane = (TitledPane)child;
                if (pane.isExpanded()) {
                    h += pane.minHeight(width);
                } else {
                    h+= getTitleRegionSize(pane, width);
                }
            }

            return h + topInset + bottomInset;
        }

        /** {@inheritDoc} */
        @Override 
        protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            double h = 0;


            for (Node child: getChildren()) {
                TitledPane pane = (TitledPane)child;
                if (pane.isExpanded()) {
                    h += pane.prefHeight(width);
                } else {
                    h+= getTitleRegionSize(pane, width);
                }
            }

            return h + topInset + bottomInset;
        }


        /**
         * @param pane
         * @param width
         * @return
         */
        private double getTitleRegionSize(TitledPane pane, double width) {
            final Skin<?> skin = pane.getSkin();
            if (skin instanceof TitledPaneSkin) {
                TitledPaneSkin childSkin = (TitledPaneSkin) skin;
                return getTitleRegionSize(childSkin, width);
            } else {
                return pane.prefHeight(width);
            }
        }
        
        private double getTitleRegionSize(TitledPaneSkin childSkin, double width) {
            return (double) FXUtils.invokeGetMethodValue(TitledPaneSkin.class, childSkin, "getTitleRegionSize", Double.TYPE, width);
        }
        
        /**
         * Callback from listener to list titledPanes of accordion.
         * This implemenation removes all listeners installed by super 
         * and replaces with our own.
         * 
         * @param c
         */
        private void panesChanged(Change<? extends TitledPane> c) {
            removeSuperTitledPaneListeners();
            if (c != null) {
                
                while(c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().stream().forEach(tp -> registerChangeListener(tp.expandedProperty(), ov -> expandedChanged(ov)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().stream().forEach(tp -> unregisterChangeListeners(tp.expandedProperty()));
                    }
                }
            } else {
                getSkinnable().getPanes().stream().forEach(tp -> registerChangeListener(tp.expandedProperty(), ov -> expandedChanged(ov)));
            }
        }

        private void expandedChanged(ObservableValue<?> expand) {
            getSkinnable().requestLayout();
        }
        
        private void removeSuperTitledPaneListeners() {
            Map<TitledPane, ChangeListener<Boolean>> listeners = getSuperTitledPaneListenerMap();
            for (TitledPane pane : listeners.keySet()) {
                ChangeListener<Boolean> l = listeners.get(pane);
                pane.expandedProperty().removeListener(l);
            }
        }

        /**
         * @return
         */
        private Map<TitledPane, ChangeListener<Boolean>> getSuperTitledPaneListenerMap() {
            return (Map<TitledPane, ChangeListener<Boolean>>) FXUtils.invokeGetFieldValue(AccordionSkin.class, this, "listeners");
        }
        
    }
    @Override 
    public void start(Stage stage) {
        stage.setTitle("TitledPane");
        Scene scene = new Scene(new Group(), 80, 180);
                               
        final Accordion accordion = new Accordion () {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyAccordionSkin(this);
            }
            
        };        
        
        for (int i = 0; i < imageNames.length; i++) {        
            Label label = new Label("Label in: " + imageNames[i]);
            tps[i] = new TitledPane(imageNames[i],label); 
        }   
        accordion.getPanes().addAll(tps);
        accordion.setExpandedPane(tps[0]);
 
        Group root = (Group)scene.getRoot();
        root.getChildren().add(accordion);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}