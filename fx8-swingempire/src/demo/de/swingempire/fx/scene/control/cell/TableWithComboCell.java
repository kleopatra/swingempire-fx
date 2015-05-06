/*
 * Created on 06.05.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Original problem: combo popup closed when adding items
 * http://stackoverflow.com/q/30073165/203657
 * 
 * Real problem: editing canceled when adding items (or 
 * other modifications to the list) - see with simple
 * TextFieldTableCell
 */
public class TableWithComboCell extends Application implements Runnable
{

  private int count;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Override
  public void start(Stage stage) 
  {
    final TableView<MenuItem> table = new TableView<>();
    final TableColumn<MenuItem, String> column = new TableColumn<>("Name");
    table.setEditable(true);
    column.setEditable(true);

    table.setItems(FXCollections.observableArrayList(
            new MenuItem("my"), new MenuItem("initial"), new MenuItem("items")));

//    column.setCellFactory(cell -> new EditableComboBoxCell());
    
    column.setCellFactory(item -> {
        ComboBoxTableCell cell = new ComboBoxTableCell(
                "other", "dummy", "option");
//                new MenuItem("other"), new MenuItem("dummy"), new MenuItem("option"));
        cell.setComboBoxEditable(true);
        return cell;
    });
    column.setCellValueFactory(new PropertyValueFactory("text"));

    column.setCellFactory(TextFieldTableCell.forTableColumn());
    table.getColumns().add(column);

    // Simple thread loop to demonstrate adding items

//    new Thread(() ->
//    {
//        try
//        {
//            while (true)
//            {
//                Thread.sleep(2000);
//                Platform.runLater(() -> table.getItems().add("foo"));
//            }
//
//        } catch (InterruptedException e)
//        {
//        }
//    }).start();

    Button add = new Button("Add");
    add.setOnAction(e -> {
        table.getItems().add(new MenuItem("foo " + count++));
    });
    BorderPane pane = new BorderPane(table);
    pane.setBottom(add);
    Scene scene = new Scene(pane);

    stage.setScene(scene);

    stage.show();

  }

    
  @Override
  public void run()
  {
    launch();
  }
  
  public static class EditableComboBoxCell extends TableCell<String, String>
  {

    private ComboBox<Object> comboBox;

    @Override
    public void startEdit()
    {
      super.startEdit();
      comboBox = new ComboBox<>();
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setGraphic(comboBox);
    }

    @Override
    public void cancelEdit()
    {
      super.cancelEdit();
      comboBox = null;
      setContentDisplay(ContentDisplay.TEXT_ONLY);
      setText(getItem());
    }

    @Override
    public void updateItem(final String item, final boolean empty)
    {
      super.updateItem(item, empty);
      if (empty)
      {
        setText(null);
        setGraphic(null);
      }
      else
      {
        if (isEditing())
        {
          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(comboBox);
        }
        else
        {
          setContentDisplay(ContentDisplay.TEXT_ONLY);
          setText(getItem());
        }
      }
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

}

