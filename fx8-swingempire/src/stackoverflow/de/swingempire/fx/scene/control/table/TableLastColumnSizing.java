/*
 * Created on 25.04.2018
 *
 */
package de.swingempire.fx.scene.control.table;

import java.util.stream.IntStream;

import de.swingempire.fx.scene.control.table.TableLastColumnSizing.TestObject;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 
 * https://stackoverflow.com/q/50012281/203657
 * last column not fully shown in fx8
 * 
 * looks okay in fx9
 * 
 * 
 */
public class TableLastColumnSizing extends Application {

    private TableView<TestObject> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        IntStream.range(1, 26).forEach(counter -> {
            TableColumn<TestObject, String> column = new TableColumn<>("Field " + counter);
            column.setCellValueFactory(new PropertyValueFactory<>("field" + counter));
            column.setCellFactory(col -> new TableCell<TestObject, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty)
                        setText(null);
                    else
                        setText(item);
                }
            });

            if (counter == 25)
                column.setStyle("-fx-alignment: CENTER_RIGHT;");

            table.getColumns().add(column);
        });

        table.getSelectionModel().setCellSelectionEnabled(true);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(new TestObject()));

        BorderPane borderPane = new BorderPane(table);
        borderPane.setRight(new Label("Panel allowing ScenicView to\nhighlight full extent of the issue"));

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("TableView Column Issue");
        stage.setWidth(800);
        stage.setHeight(300);
        stage.show();
    }

    public static class TestObject {

        private final SimpleStringProperty field1;
        private final SimpleStringProperty field2;
        private final SimpleStringProperty field3;
        private final SimpleStringProperty field4;
        private final SimpleStringProperty field5;
        private final SimpleStringProperty field6;
        private final SimpleStringProperty field7;
        private final SimpleStringProperty field8;
        private final SimpleStringProperty field9;
        private final SimpleStringProperty field10;
        private final SimpleStringProperty field11;
        private final SimpleStringProperty field12;
        private final SimpleStringProperty field13;
        private final SimpleStringProperty field14;
        private final SimpleStringProperty field15;
        private final SimpleStringProperty field16;
        private final SimpleStringProperty field17;
        private final SimpleStringProperty field18;
        private final SimpleStringProperty field19;
        private final SimpleStringProperty field20;
        private final SimpleStringProperty field21;
        private final SimpleStringProperty field22;
        private final SimpleStringProperty field23;
        private final SimpleStringProperty field24;
        private final SimpleStringProperty field25;

        private SimpleStringProperty newProp() {
            return new SimpleStringProperty("Scroll to the far right column");
        }

        private TestObject() {
            this.field1 = newProp();
            this.field2 = newProp();
            this.field3 = newProp();
            this.field4 = newProp();
            this.field5 = newProp();
            this.field6 = newProp();
            this.field7 = newProp();
            this.field8 = newProp();
            this.field9 = newProp();
            this.field10 = newProp();
            this.field11 = newProp();
            this.field12 = newProp();
            this.field13 = newProp();
            this.field14 = newProp();
            this.field15 = newProp();
            this.field16 = newProp();
            this.field17 = newProp();
            this.field18 = newProp();
            this.field19 = newProp();
            this.field20 = newProp();
            this.field21 = newProp();
            this.field22 = newProp();
            this.field23 = newProp();
            this.field24 = newProp();
            this.field25 = new SimpleStringProperty("Can you see down to zero? 9876543210");
        }

        public String getField1() { return field1.get(); }
        public void setField(String str) { field1.set(str); }

        public String getField2() { return field2.get(); }
        public void setField2(String str) { field2.set(str); }

        public String getField3() { return field3.get(); }
        public void setField3(String str) { field3.set(str); }

        public String getField4() { return field4.get(); }
        public void setField4(String str) { field4.set(str); }

        public String getField5() { return field5.get(); }
        public void setField5(String str) { field5.set(str); }

        public String getField6() { return field6.get(); }
        public void setField6(String str) { field6.set(str); }

        public String getField7() { return field7.get(); }
        public void setField7(String str) { field7.set(str); }

        public String getField8() { return field8.get(); }
        public void setField8(String str) { field8.set(str); }

        public String getField9() { return field9.get(); }
        public void setField9(String str) { field9.set(str); }

        public String getField10() { return field10.get(); }
        public void setField10(String str) { field10.set(str); }

        public String getField11() { return field11.get(); }
        public void setField11(String str) { field11.set(str); }

        public String getField12() { return field12.get(); }
        public void setField12(String str) { field12.set(str); }

        public String getField13() { return field13.get(); }
        public void setField13(String str) { field13.set(str); }

        public String getField14() { return field14.get(); }
        public void setField14(String str) { field14.set(str); }

        public String getField15() { return field15.get(); }
        public void setField15(String str) { field15.set(str); }

        public String getField16() { return field16.get(); }
        public void setField16(String str) { field16.set(str); }

        public String getField17() { return field17.get(); }
        public void setField17(String str) { field17.set(str); }

        public String getField18() { return field18.get(); }
        public void setField18(String str) { field18.set(str); }

        public String getField19() { return field19.get(); }
        public void setField19(String str) { field19.set(str); }

        public String getField20() { return field20.get(); }
        public void setField20(String str) { field20.set(str); }

        public String getField21() { return field21.get(); }
        public void setField21(String str) { field21.set(str); }

        public String getField22() { return field22.get(); }
        public void setField22(String str) { field22.set(str); }

        public String getField23() { return field23.get(); }
        public void setField23(String str) { field23.set(str); }

        public String getField24() { return field24.get(); }
        public void setField24(String str) { field24.set(str); }

        public String getField25() { return field25.get(); }
        public void setField25(String str) { field25.set(str); }
    }
}

