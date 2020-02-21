/*
 * Created on 21.02.2020
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.Locale;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/60330779/203657
 * not entirely clear what the OP wants .. "bidi binding of selected row state" 
 * 
 * - is not really possible because index/item are read-only
 * - CheckBoxTableCell requires real property (that is both read-write) to even show anything
 * - custom cell with checkbox should disable fire to not allow invalid selection state
 * - alternatively, un/bind check selection to cellObservableValue?
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableBindSelectCell extends Application {

    private Parent createContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        TableColumn<Locale, String> name = new TableColumn<>("display name");
        name.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        
        TableColumn<Locale, Boolean> selected = new TableColumn<>("selected");
        selected.setCellValueFactory(cc -> {
            Locale locale = cc.getValue();
            ReadOnlyObjectProperty<Locale> selBinding = table.getSelectionModel().selectedItemProperty();
            return Bindings.equal(locale, selBinding);
        });
        selected.setCellFactory(cc -> {
            TableCell<Locale, Boolean> cell = new TableCell<>() {
                CheckBox check = new CheckBox() {

                    @Override
                    public void fire() {
                        // do nothing - visualizing read-only property
                        // could do better, like actually changing the table's selection
                    }
                    
                };
                {
                    getStyleClass().add("check-box-table-cell");
                    check.setOnAction(e -> {e.consume();});
                }
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        check.setSelected(item);
                        setGraphic(check);
                    }
                }
                
            };
            return cell;
        });
        
        // checkBoxTableCell only working - if the callback returns a property!
        // read-only not supported.
//        selected.setCellFactory(CheckBoxTableCell.forTableColumn(selected));
//        selected.setCellFactory(param -> {
//            final CheckBoxTableCell<Locale, Boolean> cell = new CheckBoxTableCell<>();
//            cell.setSelectedStateCallback(index -> {
//                final boolean sel = table.getSelectionModel().isSelected(index);
//                ReadOnlyIntegerProperty selectedIndex = table.getSelectionModel().selectedIndexProperty();
//                BooleanBinding selBinding = Bindings.equal(index, selectedIndex);
//                if (index == 5) {
//                    selBinding.addListener((src, ov, nv) -> {
//                        System.out.println("sel of index 5 changed: " + nv);
//                    });
//                }
//                return selBinding;
//            });
//            return cell;
//        });
//        
        table.getColumns().addAll(name, selected);
        BorderPane content = new BorderPane(table);
        return content;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableBindSelectCell.class.getName());

}
