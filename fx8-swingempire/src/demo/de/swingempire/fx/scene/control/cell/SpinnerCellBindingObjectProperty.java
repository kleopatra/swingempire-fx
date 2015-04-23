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
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
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
public class SpinnerCellBindingObjectProperty extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        final TableView<MyBeanObjectProperty> tableView = new TableView<>();
        final TableColumn<MyBeanObjectProperty, Integer> colA = new TableColumn<>("Col A");
        final TableColumn<MyBeanObjectProperty, Integer> colB = new TableColumn<>("Col B");

        colA.setCellFactory(col -> new SpinnerCell<MyBeanObjectProperty, Integer>());
        colA.setCellValueFactory(new PropertyValueFactory<MyBeanObjectProperty, Integer>("valA"));

        colB.setCellFactory(col -> new SpinnerCell<MyBeanObjectProperty, Integer>());
        colB.setCellValueFactory(new PropertyValueFactory<MyBeanObjectProperty, Integer>("valB"));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setItems(FXCollections.observableArrayList(new MyBeanObjectProperty(1, 2)));
        tableView.getColumns().addAll(colA, colB);

        Spinner<Integer> spinnerA = new Spinner(0, 100, 1);
        Spinner<Integer> spinnerB = new Spinner(0, 100, 1);
        MyBeanObjectProperty bean = new MyBeanObjectProperty(2, 4);
        spinnerA.getValueFactory().valueProperty().bindBidirectional(bean.valAProperty());
        spinnerB.getValueFactory().valueProperty().bindBidirectional(bean.valBProperty());
        
        Label labelB = new Label();
        StringBinding tB = new StringBinding() {
            {
                bind(bean.valBProperty());
            }
            
            @Override
            protected String computeValue() {
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

    public static class SpinnerCell<S, T> extends TableCell<S, T> {

        private Spinner<Integer> spinner;
        private ObservableValue<T> ov;

        public SpinnerCell() {
            this.spinner = new Spinner<Integer>(0, 100, 1);
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

                if(this.ov instanceof Property<?>) {
                    this.spinner.getValueFactory().valueProperty().unbindBidirectional(((Property) this.ov));
                }

                this.ov = getTableColumn().getCellObservableValue(getIndex());

                if(this.ov instanceof Property) {
                    this.spinner.getValueFactory().valueProperty().bindBidirectional(((Property) this.ov));
                }
            }
        }
    }


    public static class MyBeanObjectProperty {

        private Property<Integer> valA; 
        private Property<Integer> valB;

        public MyBeanObjectProperty(int valA, int valB) {
//            this.valA = new SimpleIntegerProperty(this, "valA", valA) {
//
//                @Override
//                protected void invalidated() {
//                    validated(this);
//                    super.invalidated();
//                }
//
//                
//            };
//            this.valB = new SimpleIntegerProperty(this, "valB", valB) {
//
//                @Override
//                protected void invalidated() {
//                    validated(this);
//                    super.invalidated();
//                }
//                
//                
//            };
            this.valA = new SimpleObjectProperty<Integer>(this, "valA", valA);
            this.valB = new SimpleObjectProperty<Integer>(this, "valB", valB);
            BidirectionalBinding.<Integer, Integer>bindBidirectional(
                    this.valA, this.valB, this::updateB, this::updateA);
        }

        protected void updateB(ObservableValue<? extends Integer> source,  Integer old, Integer value) {
            LOG.info("getting/from: " + ((Property) source).getName() + ((Property)source).getValue() );
            setValB(value.intValue() * 2);
        }
        
        protected void updateA(ObservableValue<? extends Integer> source, Integer old, Integer value) {
            setValA(value.intValue() / 2);
        }
        
//        protected void validated(IntegerProperty property) {
//            LOG.info("getting/from: " + property.getName() + property.get() );
//            if (property == valA) {
//                setValB(property.get() * 2);
//            } else { 
//                setValA(property.get() / 2);
//            }
//        }
        
        public Property<Integer> valAProperty() {
            return this.valA;
        }

        public void setValA(int valA) {
            this.valA.setValue(valA);
        }

        public int getValA() {
            return valA.getValue();
        }

        public Property<Integer> valBProperty() {
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
    private static final Logger LOG = Logger.getLogger(SpinnerCellBindingObjectProperty.class
            .getName());
}
