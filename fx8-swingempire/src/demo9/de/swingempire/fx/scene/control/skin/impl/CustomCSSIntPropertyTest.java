/*
 * Created on 11.01.2019
 *
 */
package de.swingempire.fx.scene.control.skin.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Example from Wolfgang.
 */
public class CustomCSSIntPropertyTest extends Application
{
 private MyPane myPane;
      
 public static void main(String[] args)
 {
   Application.launch(args);
 }

 @Override
 public void start(Stage stage)
 {
   System.err.println("Version: " + System.getProperty("javafx.runtime.version"));
   Scene scene = new Scene(createContentPane(), 800, 600);
   stage.setScene(scene);
   stage.setTitle(getClass().getSimpleName());
   stage.show();

   System.err.println("*" + myPane.getForzenColumnsLeft());
   myPane.setForzenColumnsLeft(0);
   System.err.println("*" + myPane.getForzenColumnsLeft());
   myPane.applyCss();
   System.err.println("*" + myPane.getForzenColumnsLeft());
 }

 private Parent createContentPane()
 {
   BorderPane content = new BorderPane();
   myPane = new MyPane();
   content.setCenter(myPane);
   return content;
 }

 public static class MyPane extends StackPane
 {
   public MyPane()
   {
      setStyle("-sfx-frozen-columns-left: 2");
   }

   /***************************************************************************
    *                                                                         *
    *                         Stylesheet Handling                             *
    *                                                                         *
    **************************************************************************/
  
   public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
   {
     return StyleableProperties.STYLEABLES;
   }

   @Override
   public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
   {
     return getClassCssMetaData();
   }

   private IntegerProperty frozenLeft = new StyleableIntegerProperty()
   {
     @Override
     public Object getBean() {return MyPane.this;}

     @Override
     public String getName() {return "frozenColumnsLeft";}

     @Override
     public CssMetaData<MyPane, Number> getCssMetaData() {return StyleableProperties.FROZEN_COLUMNS_LEFT;}

     @Override
     public void invalidated()
     {
       final Integer newValue = get();
       //TODO remove
       System.err.println("-> " + newValue + " " + newValue.getClass());
     }
   };   
  
   public final void setForzenColumnsLeft(Integer value) {frozenLeft.set(value);}
   public final Integer getForzenColumnsLeft() {return frozenLeft.get();}
   public final IntegerProperty myNumberProperty() {return frozenLeft;}
      
   private static class StyleableProperties
   {
     private static final CssMetaData<MyPane, Number> FROZEN_COLUMNS_LEFT = new CssMetaData<MyPane, Number>("-sfx-frozen-columns-left", StyleConverter.getSizeConverter())
     {
       @Override
       public boolean isSettable(MyPane node)
       {
         return node.frozenLeft == null || !node.frozenLeft.isBound();
       }

       @Override
       public StyleableIntegerProperty getStyleableProperty(MyPane node)
       {
         return (StyleableIntegerProperty) node.myNumberProperty();
       }
     };

     private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

     static
     {
             // IMPORTANT: pass CssMetaData from super class, otherwise they are no longer available in the derived control!
       final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<CssMetaData<? extends Styleable, ?>>(StackPane.getClassCssMetaData());
       styleables.add(FROZEN_COLUMNS_LEFT);
       //TODO right frozen columns support
       //styleables.add(FROZEN_COLUMNS_RIGHT);
       STYLEABLES = Collections.unmodifiableList(styleables);
     }
   }   
  }
}