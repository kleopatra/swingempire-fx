/*
 * Created on 23.04.2015
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.util.logging.Logger;

import de.swingempire.fx.property.BidirectionalBinding;
import javafx.application.Application;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * bidi-binding of expression - doesn't work, not supported?
 * http://stackoverflow.com/q/29807105/203657
 * 
 * It's similar to bidi-converter - couldn't make it.
 */
public class SpinnerCellBindingIntegerProperty extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        final TableView<MyBean> tableView = new TableView<>();
        final TableColumn<MyBean, Integer> colA = new TableColumn<>("Col A");
        final TableColumn<MyBean, Integer> colB = new TableColumn<>("Col B");

        colA.setCellFactory(col -> new SpinnerCell<MyBean, Integer>());
        colA.setCellValueFactory(new PropertyValueFactory<MyBean, Integer>("valA"));

        colB.setCellFactory(col -> new SpinnerCell<MyBean, Integer>(2));
        colB.setCellValueFactory(new PropertyValueFactory<MyBean, Integer>("valB"));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(FXCollections.observableArrayList(new MyBean(1)));
        tableView.getColumns().addAll(colA, colB);

        Spinner<Number> spinnerA = new Spinner<>(0, 100, 1);
        Spinner<Number> spinnerB = new Spinner<>(0, 100, 2);
        MyBean bean = new MyBean(2);
        spinnerA.getValueFactory().valueProperty().bindBidirectional(bean.valAProperty());
        
        spinnerA.getValueFactory().valueProperty().addListener((source, old, value) -> {
            LOG.info("got valueChange from spinnerA: " + value);
        }) ;
        
        bean.valAProperty().addListener((source, old, value) -> {
            LOG.info("got valueChange from valA: " + value);
            
        });
        spinnerB.getValueFactory().valueProperty().bindBidirectional(bean.valBProperty());
        
        Label labelB = new Label();
        StringBinding tB = new StringBinding() {
            {
                bind(bean.valBProperty());
            }
            
            @Override
            protected String computeValue() {
                LOG.info("in be binding?");
                return String.valueOf(bean.valBProperty().getValue());
            }
        };
        labelB.textProperty().bind(tB);
        Label labelA = new Label();
        StringBinding tA = new StringBinding() {
            {
                bind(bean.valAProperty());
            }

            @Override
            protected String computeValue() {
                LOG.info("in aa binding?");
                return String.valueOf(bean.valAProperty().getValue());
            }
        };
        labelA.textProperty().bind(tA);
        stage.setScene(new Scene(new VBox(tableView, spinnerA, spinnerB, labelA, labelB), 500, 300));
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch();
    }

    public static class SpinnerCell<S, T extends Number> extends TableCell<S, T> {

        private Spinner<T> spinner;
        private ObservableValue<T> ov;

        public SpinnerCell() {
            this(1);
        }    
        
        public SpinnerCell(int step) {
            this.spinner = new Spinner<>(0, 100, step);
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(null);
                setGraphic(this.spinner);

                if(this.ov instanceof Property) {
                    this.spinner.getValueFactory().valueProperty().unbindBidirectional(((Property) this.ov));
                }

                this.ov = getTableColumn().getCellObservableValue(getIndex());

                if(this.ov instanceof Property) {
                    this.spinner.getValueFactory().valueProperty().bindBidirectional(((Property) this.ov));
                }
            }
        }
    }


    public static class MyBean {

        private IntegerProperty valA; 
        private IntegerProperty valB;

        public MyBean(int valA) {
            this.valA = new SimpleIntegerProperty(this, "valA", valA);
            this.valB = new SimpleIntegerProperty(this, "valB", 0);
            updateB(this.valA, null, this.valA.get());
            BidirectionalBinding.<Number, Number>bindBidirectional(
                    this.valA, this.valB, this::updateB, this::updateA);
        }

        protected void updateB(ObservableValue<? extends Number> source,  Number old, Number value) {
            setValB(value.intValue() * 2);
        }
        
        protected void updateA(ObservableValue<? extends Number> source, Number old, Number value) {
            setValA(value.intValue() / 2);
        }
        
        public IntegerProperty valAProperty() {
            return this.valA;
        }

        public void setValA(int valA) {
            this.valA.setValue(valA);
        }

        public int getValA() {
            return valA.getValue();
        }

        public IntegerProperty valBProperty() {
            return this.valB;
        }

        public void setValB(int valB) {
            this.valB.setValue(valB);
        }

        public int getValB() {
            return valB.getValue();
        }
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SpinnerCellBindingIntegerProperty.class
            .getName());
}
