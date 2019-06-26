/*
 * Created on 18.06.2019
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * https://stackoverflow.com/q/56643974/203657
 * DatePicker in TableCell not responsive (happens at initial usage and after re-use once per-cell)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TableWithEditableDatePickerCell extends Application {

    private final ObservableList<DataModel> ol = FXCollections.observableArrayList();
    private final TableView<DataModel> tv = new TableView<>();

    private Parent createContent() {

        loadDummyData();

        createTableViewColumns();
        // no effect
//        tv.getSelectionModel().setCellSelectionEnabled(true);
        tv.setEditable(true);
        tv.setItems(ol);
        tv.getSelectionModel().selectFirst();

        BorderPane content = new BorderPane();
        content.setCenter(tv);

        return content;
    }

    private void createTableViewColumns() {

        TableColumn<DataModel,String> col1 = new TableColumn<>("field1");
        TableColumn<DataModel,LocalDate> col2 = new TableColumn<>("field2");
        TableColumn<DataModel,String> col3 = new TableColumn<>("field3");

        col1.setCellValueFactory(cellData -> cellData.getValue().field1Property());
        col1.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        col2.setCellValueFactory(cellData -> cellData.getValue().field2Property());

        //**************************************************************
        //DatePicker TableCell begins here

        col2.setCellFactory(dp -> {

            DatePicker datePicker = new DatePicker();

            StringConverter<LocalDate> dateConverter = new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate object) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
                    return ( object == null ? "" : dateFormatter.format(object) );
                }

                @Override
                public LocalDate fromString(String string) {
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
                    return string.isEmpty() ? null : LocalDate.parse(string, dateFormatter);
                }

            };

            datePicker.setConverter(dateConverter);
            datePicker.setEditable(true);
//            datePicker.setDisable(false);

            TableCell<DataModel, LocalDate> cell = new TableCell<>() {

                {
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setGraphic(datePicker);
                }
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (getIndex() == 0) {
                        LOG.info("" + isEditing());
                    }


//                    setContentDisplay(isEditing() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY);
                    if (empty || item == null) {
                        setText(null);
                        datePicker.setValue(null);
//                        setGraphic(null);
//                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    } else {
                        //It works I set content display to GRAPHIC_ONLY
//                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        //setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        setText(dateConverter.toString(item));
                        datePicker.setValue((LocalDate) item);
//                        setGraphic(datePicker);
                    }
                    

                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    if (getIndex() == 0) {
                        LOG.info("" + isEditing() + (getGraphic()== datePicker));
                    }
                    
                    if (isEditing()) {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        
                    }
                    if ( getGraphic() != null && getGraphic() instanceof DatePicker ) {
                        //The problem only occurs when I set content display to TEXT_ONLY in updateItem().

                        //If I requestFocus() on the graphic, the DatePicker gets the focus but I cannot edit the date
                        //by clicking in the editor and typing something in.

                        //If I do NOT requestFocus() on the graphic, the cell containing the DatePicker gets focus but
                        //I can then click in the DatePicker's editor and type something in.  However, I cannot just
                        //start typing as the DatePicker doesn't have focus.

                        //This happens irrespective of whether I double-click on a cell to start the edit or
                        //navigate to the cell via the keyboard and then hit F2.

                        //The behaviour only appears the first time I edit a DatePicker in a given row.
                        //The second and subsequent edits for the same row work fine.

                        Platform.runLater(() -> {
                            
                            getGraphic().requestFocus();
                            
                            //Requesting focus on the DatePicker's editor doesn't appear to have any effect, either with or 
                            //without first requesting focus on the graphic.
                            ( (DatePicker) getGraphic() ).getEditor().requestFocus();
                            ( (DatePicker) getGraphic() ).getEditor().selectAll();
                        });
                    }

                }

                @Override
                public void cancelEdit() {
                    if (getIndex() == 0) {
                        LOG.info("");
                    }
                    super.cancelEdit();
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                }
                
                private void log(String message) {
                }
            };

            return cell;

        });
        //**************************************************************

        col2.setPrefWidth(120);

        col3.setCellValueFactory(cellData -> cellData.getValue().field3Property());
        col3.setCellFactory(TextFieldTableCell.<DataModel, String>forTableColumn(new DefaultStringConverter()));

        tv.getColumns().addAll(Arrays.asList(col1, col2, col3));

    }

    private void loadDummyData() {

//        for (int i = 0; i< 20; i++ ) {
//            ol.add(new DataModel("" + i, LocalDate.parse("2001-01-01"), "x"));
//        }
        ol.add(new DataModel("1", LocalDate.parse("2001-01-01"), "x"));
        ol.add(new DataModel("2", LocalDate.parse("2001-01-01"), "x"));
        ol.add(new DataModel("3", LocalDate.parse("2001-01-01"), "x"));
        ol.add(new DataModel("4", LocalDate.parse("2001-01-01"), "x"));
        ol.add(new DataModel("5", LocalDate.parse("2001-01-01"), "x"));

    }

    private class DataModel {

        private final StringProperty field1;
        private final ObjectProperty<LocalDate> field2;
        private final StringProperty field3;

        public DataModel(
            String field1,
            LocalDate field2,
            String field3
        ) {
            this.field1 = new SimpleStringProperty(field1);
            this.field2 = new SimpleObjectProperty<>(field2);
            this.field3 = new SimpleStringProperty(field3);
        }

        public String getField1() {return field1.get().trim();}
        public void setField1(String field1) {this.field1.set(field1);}
        public StringProperty field1Property() {return field1;}

        public LocalDate getField2() {return field2.get();}
        public void setField2(LocalDate field2) {this.field2.set(field2);}
        public ObjectProperty<LocalDate> field2Property() {return field2;}

        public String getField3() {return field3.get().trim();}
        public void setField3(String field3) {this.field3.set(field3);}
        public StringProperty field3Property() {return field3;}

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle("OpenJFX11 - DatePicker in TableView");
        stage.setWidth(600D);
        stage.setHeight(400D);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableWithEditableDatePickerCell.class.getName());
}

