/*
 * Created on 24.03.2022
 *
 */
package de.swingempire.fx.scene.control.table;


import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
* https://stackoverflow.com/q/71582199/203657
* NPE when clicking into editable cell.
*
* fx8 only - worksforfx18+ (NPE only when clicking into empty cell)
*/
public class CancelTableEditDemo extends Application {
   public static void main(String... a) {
       Application.launch(a);
   }

   @Override
   public void start(final Stage primaryStage) throws Exception {
       final ObservableList<TableDataObj> items = FXCollections.observableArrayList();
       final int no = 2;
       for (int i = 0; i < no; i++) {
           final String firstName = "First Name " + i;
           final String lastName = "Last Name " + i;
           final String city = "City " + i;
           items.add(new TableDataObj(i, firstName, lastName, city));
       }

       final TableView<TableDataObj> table = buildTable();
       table.setItems(items);

       final VBox root = new VBox(new RadioButton("Use this for focus changing"), table);
       root.setSpacing(10);
       root.setPadding(new Insets(10));
       VBox.setVgrow(table, Priority.ALWAYS);

       final Scene scene = new Scene(root);
       primaryStage.setScene(scene);
       primaryStage.setTitle("Cancel Table Edit Demo");
       primaryStage.show();
       primaryStage.setX(10);
   }

   @SuppressWarnings("unchecked")
   private TableView<TableDataObj> buildTable() {
       final TableView<TableDataObj> tableView = new TableView<>();
       tableView.setEditable(true);
       final TableColumn<TableDataObj, Integer> idCol = new TableColumn<>();
       idCol.setText("Id");
       idCol.setCellValueFactory(param -> param.getValue().idProperty().asObject());

       final TableColumn<TableDataObj, String> fnCol = new TableColumn<>();
       fnCol.setText("First Name");
       fnCol.setCellValueFactory(param -> param.getValue().firstNameProperty());
       fnCol.setPrefWidth(150);

       final TableColumn<TableDataObj, String> lnCol = new TableColumn<>();
       lnCol.setText("Last Name");
       lnCol.setCellValueFactory(param -> param.getValue().lastNameProperty());
       lnCol.setPrefWidth(150);

       final TableColumn<TableDataObj, String> cityCol = new TableColumn<>();
       cityCol.setEditable(true);
       cityCol.setText("City");
       cityCol.setCellValueFactory(param -> param.getValue().cityProperty());
       cityCol.setPrefWidth(150);
       cityCol.setCellFactory(param -> {
           final EditingCell<TableDataObj, String> cell = new EditingCell<>();
           cell.setOnMouseClicked(e -> {
               tableView.edit(cell.getTableRow().getIndex(), cityCol);
           });
           return cell;
       });
       cityCol.setOnEditStart(e -> {
           printEditEvent(e);
//           System.out.println("On City edit start :: " + e); //e.getRowValue());
       });
       cityCol.setOnEditCancel(e -> {
           printEditEvent(e);
//           System.out.println("On City edit cancel :: " + e); //e.getRowValue());
       });
       cityCol.setOnEditCommit(e -> {

           printEditEvent(e);
//           System.out.println("On City edit commit :: val : " + e.getNewValue() + " :: " + e.getRowValue());
           e.getRowValue().setCity(e.getNewValue());
       });
       tableView.getColumns().addAll(idCol, fnCol, lnCol, cityCol);
       return tableView;
   }

   private void printEditEvent(CellEditEvent e) {
       TablePosition p = e.getTablePosition();
       int row = p.getRow();
       System.out.println(e.getEventType() + " :: pos: " + row);
   }
   /**
    * Editing Cell
    */
   class EditingCell<T, S> extends TableCell<T, S> {

       private TextField textField;

       @Override
       public void cancelEdit() {
           super.cancelEdit();
           updateItem(getItem(), getItem() == null);
       }

       @Override
       public void commitEdit(final S newValue) {
           super.commitEdit(newValue);
       }

       @Override
       public void startEdit() {
           super.startEdit();
           updateItem(getItem(), getItem() == null);
           textField.selectAll();
           textField.requestFocus();
       }

       @Override
       public void updateItem(final S item, final boolean empty) {
           super.updateItem(item, empty);
           if (empty) {
               setText(null);
               setGraphic(textField);
           } else {
               if (isEditing()) {
                   if (textField == null) {
                       createTextField();
                   }
                   textField.setText(getString());
                   setGraphic(textField);
                   setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
               } else {
                   setText(item != null ? item.toString() : "");
                   setContentDisplay(ContentDisplay.TEXT_ONLY);
               }
           }
       }

       private void createTextField() {
           textField = new TextField(getString());
           textField.setMinWidth(getWidth() - getGraphicTextGap() * 2);

           textField.setOnKeyPressed(keyEvent -> {
               if (keyEvent.getCode() == KeyCode.ESCAPE) {
                   cancelEdit();
                   keyEvent.consume();
               } else if (keyEvent.getCode() == KeyCode.ENTER) {
                   commitEdit((S) textField.getText()); // For now casting directly for testing
                   keyEvent.consume();
               }
           });

           /* Cancel edit when loosing focus. */
           textField.focusedProperty().addListener((obs, prevFocus, focused) -> {
               if (!focused) {
                   cancelEdit();
               }
           });
       }

       private String getString() {
           return getItem() == null ? "" : getItem().toString();
       }
   }

   /**
    * Data object.
    */
   class TableDataObj {
       private final IntegerProperty id = new SimpleIntegerProperty();
       private final StringProperty firstName = new SimpleStringProperty();
       private final StringProperty lastName = new SimpleStringProperty();
       private final StringProperty city = new SimpleStringProperty();

       public TableDataObj(final int i, final String fn, final String ln, final String cty) {
           setId(i);
           setFirstName(fn);
           setLastName(ln);
           setCity(cty);
       }

       public StringProperty cityProperty() {
           return city;
       }

       public StringProperty firstNameProperty() {
           return firstName;
       }

       public String getCity() {
           return city.get();
       }

       public String getFirstName() {
           return firstName.get();
       }

       public int getId() {
           return id.get();
       }

       public String getLastName() {
           return lastName.get();
       }

       public IntegerProperty idProperty() {
           return id;
       }

       public StringProperty lastNameProperty() {
           return lastName;
       }

       public void setCity(final String city1) {
           city.set(city1);
       }

       public void setFirstName(final String firstName1) {
           firstName.set(firstName1);
       }

       public void setId(final int idA) {
           id.set(idA);
       }

       public void setLastName(final String lastName1) {
           lastName.set(lastName1);
       }

       @Override
       public String toString() {
           return "TableDataObj{" +
                   "firstName=" + firstName.get() +
                   ", lastName=" + lastName.get() +
                   ", city=" + city.get() +
                   '}';
       }
   }
}