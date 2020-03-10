/*
 * Created on 10.03.2020
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.Map;
import static java.util.Map.entry;

import de.swingempire.fx.scene.control.cell.TableRowConditionalStyling.GCodeItem;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


public class TableRowConditionalStyling extends Application {

    private final ObservableList<GCodeItem> gcodeItems = FXCollections.observableArrayList(
        item -> new Observable[]{item.validatedProperty(), item.errorDescriptionProperty()});
    private final TableView<GCodeItem> tblGCode = new TableView<>();

    @Override
    public void start(Stage stage) {

        TableColumn<GCodeItem, String> colGCode = new TableColumn<>("GCode");
        colGCode.setCellValueFactory(new PropertyValueFactory<>("gcode"));
        TableColumn<GCodeItem, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("validationResponse"));

        // Set first column to be editable
        tblGCode.setEditable(true);
        colGCode.setEditable(true);
        colGCode.setCellFactory(TextFieldTableCell.forTableColumn());
//        colGCode.setOnEditCommit((TableColumn.CellEditEvent<GCodeItem, String> t) -> {
//            ((GCodeItem) t.getTableView().getItems().get(t.getTablePosition().getRow())).setGcode(t.getNewValue());
//        });

        // Set row factory
        tblGCode.setRowFactory(tbl -> new TableRow<GCodeItem>() {
            private final Tooltip tip = new Tooltip();
            {
                tip.setShowDelay(new Duration(250));
            }

            
            @Override
            protected boolean isItemChanged(GCodeItem oldItem,
                    GCodeItem newItem) {
                // TODO Auto-generated method stub
//                return super.isItemChanged(oldItem, newItem);
                return true;
            }


            @Override
            protected void updateItem(GCodeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setStyle("");
                    setTooltip(null);
                    
                } else 
                    if(item == null ) {
                        setStyle("");
                        setTooltip(null);
                    } else {
                    if(item.isValidated()) {
                        if(item.hasError()) {
                            setStyle("-fx-background-color: #ffcccc"); // red
                            tip.setText(item.getErrorDescription());
                            setTooltip(tip);
                        } else {
                            setStyle("-fx-background-color: #ccffdd"); // green
                            setTooltip(null);
                        }
                    } else {
                        setStyle("");                                
                        setTooltip(null);
                    }
                }
                //tblGCode.refresh(); // this works to give desired styling, but breaks editing
            }
            
            
        });

        tblGCode.getColumns().setAll(colGCode, colStatus);
        tblGCode.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // buttons to simulate issue
        Button btnPopulate = new Button("1. Populate Table");
        btnPopulate.setOnAction(eh -> populateTable());
        Button btnValidate = new Button("2. Validate Table");
        btnValidate.setOnAction(eh -> simulateValidation());

        var scene = new Scene(new VBox(tblGCode, btnPopulate, btnValidate), 640, 320);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void populateTable() {
        // simulates updating of ObservableList with first couple of dozen lines of a file
        gcodeItems.add(new GCodeItem("(1001)"));
        gcodeItems.add(new GCodeItem("(T4  D=0.25 CR=0 - ZMIN=-0.4824 - flat end mill)"));
        gcodeItems.add(new GCodeItem("G90 G94"));
        gcodeItems.add(new GCodeItem("G17"));
        gcodeItems.add(new GCodeItem("G20"));
        gcodeItems.add(new GCodeItem("G28 G91 Z0"));
        gcodeItems.add(new GCodeItem("G90"));
        gcodeItems.add(new GCodeItem(""));
        gcodeItems.add(new GCodeItem("(Face1)"));
        gcodeItems.add(new GCodeItem("T4 M6"));
        gcodeItems.add(new GCodeItem("S5000 M3"));
        gcodeItems.add(new GCodeItem("G54"));
        gcodeItems.add(new GCodeItem("M8"));
        gcodeItems.add(new GCodeItem("G0 X1.3842 Y-1.1452"));
        gcodeItems.add(new GCodeItem("Z0.6"));
        gcodeItems.add(new GCodeItem("Z0.2"));
        gcodeItems.add(new GCodeItem("G1 Z0.015 F20"));
        gcodeItems.add(new GCodeItem("G18 G3 X1.3592 Z-0.01 I-0.025 K0"));
        gcodeItems.add(new GCodeItem("G1 X1.2492"));
        gcodeItems.add(new GCodeItem("X-1.2492 F40"));
        gcodeItems.add(new GCodeItem("X-1.25"));
        gcodeItems.add(new GCodeItem("G17 G2 X-1.25 Y-0.9178 I0 J0.1137"));
        gcodeItems.add(new GCodeItem("G1 X1.25"));
        gcodeItems.add(new GCodeItem("G3 X1.25 Y-0.6904 I0 J0.1137"));

        // Add list to table
        tblGCode.setItems(gcodeItems);
    }

    private void simulateValidation() {
        // sets validationResponse on certain rows (not every row is validated)
        gcodeItems.get(2).setValidationResponse("ok");
        gcodeItems.get(3).setValidationResponse("ok");
        gcodeItems.get(4).setValidationResponse("ok");
        gcodeItems.get(5).setValidationResponse("ok");
        gcodeItems.get(6).setValidationResponse("ok");
        gcodeItems.get(9).setValidationResponse("error:20");
        gcodeItems.get(10).setValidationResponse("ok");
        gcodeItems.get(11).setValidationResponse("ok");
        gcodeItems.get(12).setValidationResponse("ok");
        gcodeItems.get(13).setValidationResponse("ok");
        gcodeItems.get(14).setValidationResponse("ok");
        gcodeItems.get(15).setValidationResponse("ok");
        gcodeItems.get(16).setValidationResponse("ok");
        gcodeItems.get(17).setValidationResponse("ok");
        gcodeItems.get(18).setValidationResponse("ok");
        gcodeItems.get(19).setValidationResponse("ok");
        gcodeItems.get(20).setValidationResponse("ok");
        gcodeItems.get(21).setValidationResponse("ok");
        gcodeItems.get(22).setValidationResponse("ok");
        gcodeItems.get(23).setValidationResponse("ok");
    }
    
    public class GCodeItem {

        private final SimpleStringProperty gcode;
        private final SimpleStringProperty validationResponse;
        private ReadOnlyBooleanWrapper validated;
        private ReadOnlyBooleanWrapper hasError;
        private ReadOnlyIntegerWrapper errorNumber;
        private ReadOnlyStringWrapper errorDescription;

        public GCodeItem(String gcode) {
            this.gcode = new SimpleStringProperty(gcode);
            this.validationResponse = new SimpleStringProperty("");
            this.validated = new ReadOnlyBooleanWrapper();
            this.hasError = new ReadOnlyBooleanWrapper();
            this.errorNumber = new ReadOnlyIntegerWrapper();
            this.errorDescription = new ReadOnlyStringWrapper();

            validated.bind(Bindings.createBooleanBinding(
                () -> ! "".equals(getValidationResponse()),
                validationResponse
            ));

            hasError.bind(Bindings.createBooleanBinding(
                () -> ! ("ok".equals(getValidationResponse()) ||
                        "".equals(getValidationResponse())),
                validationResponse
            ));

            errorNumber.bind(Bindings.createIntegerBinding(
                () -> {
                    String vResp = getValidationResponse();
                    if ("ok".equals(vResp)) {
                        return 0;
                    } else {
                        // should handle potential exceptions here...
                        if(vResp.contains(":")) {
                            int en = Integer.parseInt(vResp.split(":")[1]);
                            return en ;
                        } else {
                            return 0;
                        }
                    }
                }, validationResponse
            ));

            errorDescription.bind(Bindings.createStringBinding(
                () -> {
                    int en = getErrorNumber() ;
                    return GrblDictionary.getErrorDescription(en);
                }, errorNumber
            ));
        }

        public final String getGcode() {
            return gcode.get();
        }
        public final void setGcode(String value) {
            gcode.set(value);
        }
        public SimpleStringProperty gcodeProperty() {
            return this.gcode;
        }

        public final String getValidationResponse() {
            return validationResponse.get();
        }
        public final void setValidationResponse(String value) {
            validationResponse.set(value);
        }
        public SimpleStringProperty validationResponseProperty() {
            return this.validationResponse;
        }

        public Boolean isValidated() {
            return validatedProperty().get();
        }
        public ReadOnlyBooleanProperty validatedProperty() {
            return validated.getReadOnlyProperty();
        }

        // ugly method name to conform to method naming pattern:
        public final boolean isHasError() {
            return hasErrorProperty().get();
        }
        // better method name:
        public final boolean hasError() {
            return isHasError();
        }
        public ReadOnlyBooleanProperty hasErrorProperty() {
            return hasError.getReadOnlyProperty();
        }

        public final int getErrorNumber() {
            return errorNumberProperty().get();
        }
        public ReadOnlyIntegerProperty errorNumberProperty() {
            return errorNumber.getReadOnlyProperty() ;
        }

        public final String getErrorDescription() {
            return errorDescriptionProperty().get();
        }
        public ReadOnlyStringProperty errorDescriptionProperty() {
            return errorDescription.getReadOnlyProperty();
        }
    }

    public static class GrblDictionary {

        private static final Map<Integer, String> ERRORS = Map.ofEntries(
            Map.entry(1, "G-code words consist of a letter and a value. Letter was not found."),
            Map.entry(2, "Numeric value format is not valid or missing an expected value."),
            entry(17, "Laser mode requires PWM outentry."),
            entry(20, "Unsupported or invalid g-code command found in block."),
            entry(21, "More than one g-code command from same modal group found in block."),
            entry(22, "Feed rate has not yet been set or is undefined.")
        );

        public static String getErrorDescription(int errorNumber) {
            return ERRORS.containsKey(errorNumber) ? ERRORS.get(errorNumber) : "Unrecognized error number.";
        }
    }


}

