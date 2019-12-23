/*
 * Created on 16.12.2019
 *
 */
package de.swingempire.fx.scene.control.text;

import java.util.Objects;
import java.util.function.UnaryOperator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/a/59351560/203657
 * 
 * requirement a bit unclear - just for fun: allow only contained text
 * such that at the end a complete target text is available.
 */
public class TextFormatterFilterContains extends Application {
    String info = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Nam tortor felis, pulvinar in scelerisque cursus, pulvinar at ante. " +
            "Nulla consequat congue lectus in sodales.";

    public static void main(String[] args) {
      launch(args);
    }

    
    @Override
    public void start(Stage primaryStage) {
      TextArea area = new TextArea();
      area.setPrefRowCount(5);
      area.setPrefColumnCount(25);
      area.setTextFormatter(new TextFormatter<>(new ExpectedTextFilter(info)));
      
      StackPane root = new StackPane(area);
      root.setPadding(new Insets(10));

      primaryStage.setScene(new Scene(root));
      primaryStage.show();
    }

    private static class ExpectedTextFilter implements UnaryOperator<Change> {

      private final String expectedText;

      ExpectedTextFilter(String expectedText) {
        this.expectedText = Objects.requireNonNull(expectedText);
      }

      @Override
      public Change apply(Change change) {
        if (change.isContentChange()) {
            // original from slaw
//          if (change.isReplaced()) {
//            // simply don't allow replacements
//            return null;
//          } else if (change.isDeleted()) {
//            // only allow deletions from the end of the control's text
//            return change.getRangeEnd() == change.getControlText().length() ? change : null;
//          } else {
//            return expectedText.startsWith(change.getText(), change.getRangeStart()) ? change : null;
//          }
            if (change.isAdded()) {
                // mine - weird when starting in the middle, will have to type backwards ;)
//                return expectedText.contains(change.getControlNewText()) ? change : null;
                return expectedText.startsWith(change.getText(), change.getRangeStart()) ? change : null;
            }
            return null;
        }
        return change;
      }
    }
  }

