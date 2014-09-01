/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.sun.glass.ui.View;

public class ChoiceComboItemsMain extends Application {
    private Parent rootPane;
    @Override
    public void start(Stage arg0) throws Exception {
      Scene scene = new Scene(rootPane);
      arg0.setScene(scene);
      arg0.show();
    }
    @Override
    public void init() throws Exception {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("choicecomboitems.fxml"));
      rootPane = loader.load();
      loader.<ChoiceComboItemsView>getController().init();
    }
    public static void main(String[] args) {
      launch(args);
    }
  }

