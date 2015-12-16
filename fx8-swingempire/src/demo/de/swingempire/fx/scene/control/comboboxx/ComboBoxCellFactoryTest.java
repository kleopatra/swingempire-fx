/*
 * Created on 16.12.2015
 *
 */
package de.swingempire.fx.scene.control.comboboxx;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * http://stackoverflow.com/q/34316254/203657
 * 
 * selected cell without custom marker
 */
public class ComboBoxCellFactoryTest extends Application
{
  public static void main(String[] args)
  {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage)
  {
    Parent content = createContent();
    Scene scene = new Scene(content, 400, 300);
    stage.setScene(scene);    
    stage.show();
  }

  public Parent createContent()
  {
    FlowPane content = new FlowPane(10, 10);

    ComboBox<String> combo = new ComboBox<String>();    
//    ComboBoxX<String> combo = new ComboBoxX<String>();    
    combo.setItems(FXCollections.observableArrayList("Item 1", "Item 2", "Item 3", "Item 4"));
    combo.getSelectionModel().selectLast();
    combo.setCellFactory(createCell(combo));
    
    ListView<String> list = new ListView<>();
    list.setItems(combo.getItems());
    list.setCellFactory(createCell(null));
    content.getChildren().addAll(combo, list);
    return content;
  }

protected Callback<ListView<String>, ListCell<String>> createCell(
        ComboBox<String> combo) {
    return new Callback<ListView<String>, ListCell<String>>()
    {
      @Override
      public ListCell<String> call(ListView<String> p)
      {
        return new ListCell<String>()
        {
          private final Rectangle rectangle;
          {
            rectangle = new Rectangle(10, 10);
            selectedProperty().addListener((c, ov, nv) -> {
//                updateItem(getItem(), getItem() == null);
            });
          }

          @Override
          protected void updateItem(String item, boolean empty)
          {
            super.updateItem(item, empty);

            if (empty || item == null)
            {
              setText(null);
              setGraphic(null);
            }
            else
            {
//              boolean selected = combo.getValue().equals(item);
              boolean selected = isSelected();
              rectangle.setFill(selected ? Color.GREENYELLOW : Color.RED);
              setGraphic(rectangle);
              setText(item);
              LOG.info("update: " + item + " / " +selected);
            }
          }
        };
      }
    };
}  
  
  @SuppressWarnings("unused")
private static final Logger LOG = Logger
        .getLogger(ComboBoxCellFactoryTest.class.getName());
}

