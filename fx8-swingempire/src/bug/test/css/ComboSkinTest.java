/*
 * Created on 09.08.2017
 *
 */
package test.css;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8185854
 * 
 * reported by wolfgang
 */
public class ComboSkinTest extends Application
{
  @Override
  public void start(Stage primaryStage)
  {
    Application.setUserAgentStylesheet(getClass().getResource("/test/css/style.css").toExternalForm());    
    Scene scene = new Scene(createContent(), 400, 400);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args)
  {
    launch(args);
  }
  
  public Parent createContent()
  {
    final FlowPane comboPane = new FlowPane(10, 10);
    comboPane.getChildren().add(createComboBox(true, false));

    TabPane tabPane = new TabPane();
    addTab(tabPane, "ComboBox", comboPane);
    
    BorderPane p = new BorderPane();
    p.setCenter(tabPane);
    return p;
  }  
  
  private void addTab(TabPane tabPane, String name, Node content)
  {
    Tab tab = new Tab();
    tab.setText(name);
    tab.setContent(content);
    tabPane.getTabs().add(tab);    
  }

  private ComboBox<String> createComboBox(boolean readonly, boolean disabled)
  {
    ComboBox<String> combo = new ComboBox<String>();
    combo.setDisable(disabled);
    combo.setEditable(!readonly);
    return combo;      
  }  
}

