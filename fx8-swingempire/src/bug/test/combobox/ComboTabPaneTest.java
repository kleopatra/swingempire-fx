package test.combobox;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.behavior.ComboBoxListViewBehavior;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8186023
 * 
 * reported by wolfgang
 */
public class ComboTabPaneTest extends Application
{

  public static void main(String[] args)
  {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage)
  {
    Application.setUserAgentStylesheet(getClass().getResource("/test/combobox/testcombo.css").toExternalForm());
    stage.setScene(new Scene(createContent()));
    stage.setTitle(getClass().getSimpleName());
    stage.show();
  }

  private Parent createContent()
  {
    TabPane tabPane = new TabPane();
    // Note: update package if not in test.combobox 
    tabPane.setStyle("-fx-skin: 'test.combobox.ComboTabPaneTest$TabPaneSkin'");
    tabPane.getTabs().add(new Tab("Tab", createTabContent()));
    
    BorderPane content = new BorderPane();
    content.setCenter(tabPane); //createTabContent());
    return content;
  }
  
  private Node createTabContent()
  {
      ComboBoxBaseBehavior b;
    FlowPane p = new FlowPane();
    p.setPadding(new Insets(40));
    // the style is loaded inside the css
    // -> weird appearence as described in report
    ComboBox<String> combo  = new ComboBox<>();  
    // setting style here -> appearence okay, leaving only NPE
    combo.setStyle("-fx-skin: 'test.combobox.ComboTabPaneTest$ComboBoxListViewSkin'");
    System.out.println("skin" + combo.getSkin());
    combo.setItems(FXCollections.<String>observableArrayList("Regular", "ReadOnly", "Disabled", "Disabled/ReadOnly"));
    combo.setDisable(false);
    combo.setEditable(false);
    
    combo.setValue("ReadOnly");
    p.getChildren().add(combo);
    return p;
  }
  
  
  // TabPaneSkin class
  public static class TabPaneSkin extends javafx.scene.control.skin.TabPaneSkin
  {    
    public TabPaneSkin(TabPane tabPane)
    {
      super(tabPane);
    }
  } 
  
  // ComboBoxSkin class
  public static class ComboBoxListViewSkin<T> extends javafx.scene.control.skin.ComboBoxListViewSkin<T>
  {    
    public ComboBoxListViewSkin(ComboBox<T> combo)
    {
      super(combo);
      System.out.println(this);
      System.out.println("editor " +((ComboBox<String>) getSkinnable()).getEditor());
      System.out.println("getEditor " + getEditor());
      ComboBoxListViewBehavior beh = (ComboBoxListViewBehavior) FXUtils.invokeGetMethodValue(javafx.scene.control.skin.ComboBoxListViewSkin.class, 
              this, "getBehavior");
      System.out.println("" + beh.getNode());
      StackPane arrowButton = null;
//      new RuntimeException("dumping").printStackTrace();
      try
      {
        Field field = ComboBoxBaseSkin.class.getDeclaredField("arrowButton");
        field.setAccessible(true);
        arrowButton = (StackPane)field.get(this);
        System.err.println("Parent of arrow-button: " + arrowButton + " " + arrowButton.getParent());      
      }
      catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
      {
        e.printStackTrace();
      }
    }

    @Override
    protected TextField getEditor() {
//        LOG.info("getEditor?");
        return getSkinnable().isEditable() ? ((ComboBox)getSkinnable()).getEditor() : null;
    }
    
    
  }    
  
  @SuppressWarnings("unused")
private static final Logger LOG = Logger
        .getLogger(ComboTabPaneTest.class.getName());
}

