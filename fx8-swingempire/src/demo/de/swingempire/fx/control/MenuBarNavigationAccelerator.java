/*
 * Created on 13.04.2020
 *
 */
package de.swingempire.fx.control;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8242545
 * higlight with accelerator once disables navigation by keys
 * 
 */
public class MenuBarNavigationAccelerator extends Application {
    public static void main(String[] pArgs) {launch(pArgs);}

    public void start(Stage pStage) {
      final BorderPane borderPane = new BorderPane();
      final MenuBar menuBar = new MenuBar();
      menuBar.getMenus().addAll(
        Stream.of("_Alpha", "_Bravo", "_Charlie", "").map(Menu::new).collect(
          Collectors.toList()
        )
      );
      borderPane.setTop(menuBar);
      borderPane.setCenter(new TableView<>());
      pStage.setScene(new Scene(borderPane));
      pStage.setTitle(FXUtils.version());
      pStage.show();
    }
  } 