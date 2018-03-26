/*
 * Created on 28.08.2015
 *
 */
package de.swingempire.fx.property;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.swingempire.fx.property.OldBeanTableView.OldBean;
import javafx.application.Application;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Property value factory for old-style beans
 * http://stackoverflow.com/q/32270811/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class OldBeanTableView extends Application {
    public class OldBean {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        public static final String PROPERTY_NAME_FOO = "foo";
        private int foo = 99;

        public int getFoo() {
            return foo;
        }

        public void setFoo(int foo) {
            int oldValue = this.foo;
            pcs.firePropertyChange(PROPERTY_NAME_FOO, oldValue, foo);
            this.foo = foo;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ObservableList<OldBean> beans = FXCollections.observableArrayList();
        beans.add(new OldBean());

        TableView<OldBean> tableView = new TableView<>();
        TableColumn<OldBean, Integer> column = new TableColumn<OldBeanTableView.OldBean, Integer>();
        tableView.getColumns().add(column);
//        column.setCellValueFactory(new PropertyValueFactory<>("foo"));
        
        Callback<CellDataFeatures<OldBean, Integer>, ObservableValue<Integer>> valueFactory = cdf -> {
            OldBean bean = cdf.getValue();
            JavaBeanObjectProperty<Integer> wrappee;
            try {
                wrappee = JavaBeanObjectPropertyBuilder.create()
                        .name("foo").bean(bean).build();
                
                return wrappee;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        };
        column.setCellValueFactory(valueFactory);

        tableView.setItems(beans);
        primaryStage.setScene(new Scene(tableView));
        primaryStage.show();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> beans.get(0).setFoo(beans.get(0).getFoo() + 1), 0,
                1, TimeUnit.SECONDS);
    }

}