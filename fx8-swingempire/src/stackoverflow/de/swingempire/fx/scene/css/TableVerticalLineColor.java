/*
 * Created on 26.07.2019
 *
 */
package de.swingempire.fx.scene.css;

import de.swingempire.fx.scene.css.TableVerticalLineColor.PrimitiveModel;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TableVerticalLineColor extends Application{

    @Override
    public void start(Stage primaryStage) {
        try {

            ObservableList<PrimitiveModel> variableList = FXCollections.<PrimitiveModel>observableArrayList();
            for (int i = 0; i < 5; i++) {
                variableList.add(new PrimitiveModel(i, i*2.5, Integer.toString(i*3)));
            }

            TableView<PrimitiveModel> myTable = new TableView<PrimitiveModel>(variableList);
            myTable.getColumns().addAll(getIntegerColumn(), getDoubleColumn(), getStringColumn());
            myTable.setPrefSize(400.0, 300.0);


            PseudoClass redRowPseudoClass = PseudoClass.getPseudoClass("redRow");
            PseudoClass aquaRowPseudoClass = PseudoClass.getPseudoClass("aquaRow");

            myTable.setRowFactory( table ->{  
                TableRow<PrimitiveModel> row = new TableRow<>();

                row.itemProperty().addListener(new ChangeListener<PrimitiveModel>() {
                    @Override
                    public void changed(ObservableValue<? extends PrimitiveModel> observable, PrimitiveModel oldValue,
                            PrimitiveModel newValue) {
                        if(oldValue!=null) {
                            if(oldValue.getInteger()%2==0) {
                                row.pseudoClassStateChanged(redRowPseudoClass, false);
                            }else {
                                row.pseudoClassStateChanged(aquaRowPseudoClass, false);
                            }
                        }

                        if(newValue!=null) {
                            if(newValue.getInteger()%2==0) {
                                row.pseudoClassStateChanged(redRowPseudoClass, true);
                            }else {
                                row.pseudoClassStateChanged(aquaRowPseudoClass, true);
                            }
                        }
                    }
                });

                row.selectedProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if(row.getItem()!=null) {
                            if (newValue) {
                                if(row.getItem().getInteger()%2==0) {
                                    row.pseudoClassStateChanged(redRowPseudoClass, false);
                                }else {
                                    row.pseudoClassStateChanged(aquaRowPseudoClass, false);
                                }
                            } else {
                                if(row.getItem().getInteger()%2==0) {
                                    row.pseudoClassStateChanged(redRowPseudoClass, true);
                                }else {
                                    row.pseudoClassStateChanged(aquaRowPseudoClass, true);
                                }
                            }
                        }

                    }
                });
                return row;
            });

            VBox root = new VBox(myTable);
            Scene scene = new Scene(root,400,400);
//            scene.getStylesheets().add(getClass().getResource("TableStyle.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static TableColumn<PrimitiveModel, Integer> getIntegerColumn() {
        TableColumn<PrimitiveModel, Integer> integerCol = new TableColumn<PrimitiveModel, Integer>("Integer");
        integerCol.setCellValueFactory(new PropertyValueFactory<PrimitiveModel, Integer>("integer")); 
        return integerCol;
    }

    public static TableColumn<PrimitiveModel, Double> getDoubleColumn() {
        TableColumn<PrimitiveModel, Double> doubleCol = new TableColumn<>("Double");
        doubleCol.setCellValueFactory(new PropertyValueFactory<PrimitiveModel, Double>("double"));
        return doubleCol;
    }

    public static TableColumn<PrimitiveModel, String> getStringColumn() {
        TableColumn<PrimitiveModel, String> stringCol = new TableColumn<PrimitiveModel, String>("String");
        stringCol.setCellValueFactory(new PropertyValueFactory<PrimitiveModel, String>("string")); 
        return stringCol;
    }


    public static void main(String[] args) {
        launch(args);
    }

    public class PrimitiveModel {

        public PrimitiveModel(int i, double d, String s) {
            this.i = i;
            this.d = d;
            this.s = s;
        }
        public int getInteger() {
            return i;
        }
        public double getDouble() {
            return d;
        }
        public String getString() {
            return s;
        }

        private int i;
        private double d;
        private String s;
    }


}

