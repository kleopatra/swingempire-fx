/*
 * Created on 20.03.2019
 *
 */
package de.swingempire.fx.concurrency;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.javafx.binding.BindingHelperObserver;
import com.sun.javafx.binding.ExpressionHelper;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.Binding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Safely propagate changes from the model realm onto the fx app thread.
 * https://stackoverflow.com/a/55258445/203657
 * 
 * Simple: listen to model property and manually wrap setting the ui property
 * into Platform.runLater
 * 
 * Add infrastructure to always move over to fx thread: both custom
 * binding and custom property need to re-create the whole stack of XXbinding/XXProperty
 * For binding the boilerplate is shorter.
 * 
 * PENDING JW: Andres always points to jdeferred .. looks like overkill?
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class SeparateModelUpdateOnUI extends Application {

    /**
     * InvalidationListener that invalidates the binding on the fx app thread. 
     * C&P of BindingHelperObserver which moves invalidation call to fx if needed
     * @author Jeanette Winzenburg, Berlin
     */
    public class FXBindingHelperObserver implements InvalidationListener, WeakListener {

        private final WeakReference<Binding<?>> ref;

        public FXBindingHelperObserver(Binding<?> binding) {
            if (binding == null) {
                throw new NullPointerException("Binding has to be specified.");
            }
            ref = new WeakReference<Binding<?>>(binding);
        }

        /**
         * Guarantees to invalidate on fx thread.
         */
        @Override
        public void invalidated(Observable observable) {
           if (Platform.isFxApplicationThread()) {
               invalidatedOnFXThread(observable); 
           } else {
               Platform.runLater(() -> invalidatedOnFXThread(observable));
           }
        }

        /**
         * Actual handling of invalidation, must be called on fx thread.
         * @param observable
         */
        private void invalidatedOnFXThread(Observable observable) {
            final Binding<?> binding = ref.get();
            if (binding == null) {
                observable.removeListener(this);
            } else {
                binding.invalidate();
            }
            
        }
        @Override
        public boolean wasGarbageCollected() {
            return ref.get() == null;
        }
    }

    public abstract class FXStringBinding extends StringExpression
            implements Binding<String> {

        private String value;

        private boolean valid = false;

        private FXBindingHelperObserver observer;

        private ExpressionHelper<String> helper = null;

        @Override
        public void addListener(InvalidationListener listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        @Override
        public void addListener(ChangeListener<? super String> listener) {
            helper = ExpressionHelper.addListener(helper, this, listener);
        }

        @Override
        public void removeListener(ChangeListener<? super String> listener) {
            helper = ExpressionHelper.removeListener(helper, listener);
        }

        /**
         * Start observing the dependencies for changes. If the value of one of
         * the dependencies changes, the binding is marked as invalid.
         *
         * @param dependencies the dependencies to observe
         */
        protected final void bind(Observable... dependencies) {
            if ((dependencies != null) && (dependencies.length > 0)) {
                if (observer == null) {
                    observer = new FXBindingHelperObserver(this);
                }
                for (final Observable dep : dependencies) {
                    dep.addListener(observer);
                }
            }
        }

        /**
         * Stop observing the dependencies for changes.
         *
         * @param dependencies the dependencies to stop observing
         */
        protected final void unbind(Observable... dependencies) {
            if (observer != null) {
                for (final Observable dep : dependencies) {
                    dep.removeListener(observer);
                }
                observer = null;
            }
        }

        /**
         * A default implementation of {@code dispose()} that is empty.
         */
        @Override
        public void dispose() {
        }

        /**
         * A default implementation of {@code getDependencies()} that returns an
         * empty {@link javafx.collections.ObservableList}.
         *
         * @return an empty {@code ObservableList}
         */
        @Override
        public ObservableList<?> getDependencies() {
            return FXCollections.emptyObservableList();
        }

        /**
         * Returns the result of {@link #computeValue()}. The method
         * {@code computeValue()} is only called if the binding is invalid. The
         * result is cached and returned if the binding did not become invalid
         * since the last call of {@code get()}.
         *
         * @return the current value
         */
        @Override
        public final String get() {
            if (!valid) {
                value = computeValue();
                valid = true;
            }
            return value;
        }

        /**
         * The method onInvalidating() can be overridden by extending classes to
         * react, if this binding becomes invalid. The default implementation is
         * empty.
         */
        protected void onInvalidating() {
        }

        @Override
        public final void invalidate() {
            if (valid) {
                valid = false;
                onInvalidating();
                ExpressionHelper.fireValueChangedEvent(helper);
            }
        }

        @Override
        public final boolean isValid() {
            return valid;
        }

        /**
         * Calculates the current value of this binding.
         * <p>
         * Classes extending {@code StringBinding} have to provide an
         * implementation of {@code computeValue}.
         *
         * @return the current value
         */
        protected abstract String computeValue();

        /**
         * Returns a string representation of this {@code StringBinding} object.
         * 
         * @return a string representation of this {@code StringBinding} object.
         */
        @Override
        public String toString() {
            return valid ? "StringBinding [value: " + get() + "]"
                    : "StringBinding [invalid]";
        }

    }

    // Model
    public class Temp {
        private StringProperty temp = new SimpleStringProperty("a");

        private final ExecutorService service = Executors.newCachedThreadPool();

        public Temp() {
//            task.startConnection();
            service.submit(new Task<Integer>() {

                @Override
                public Integer call() {
                    while(true) {
                        try {
                            Thread.sleep(1000);
                        }catch(Exception e) {
                            e.printStackTrace();
                        }

                        setTemp(getTemp() + "b");
                        System.out.println(getTemp());

                    }
                }
            });
        }

        public StringProperty tempProperty() {
            return this.temp;
        }

        public String getTemp() {
            return this.temp.get();
        }

        public void setTemp(String value) {
            this.temp.set(value);
        }
    }
    
    private Label target;
    private Label boundTarget;
    private Temp model;
    
    private StringProperty uiTemp;
    private FXStringBinding uiBinding;
    
    private Parent createContent() {
        target = new Label();
        boundTarget = new Label("doing nothing ..");
        createAndConfigureModel();
        VBox content = new VBox(10, target, boundTarget);
        return content;
    }

    private void createAndConfigureModel() {
        model = new Temp();
        model.tempProperty().addListener(this::updateLabel);
        // experiment: use custom binding?
        // no: same as with custom property, need to go really high up the chain
        // and re-implement all (ExpressionHelper still is internal api!
        uiBinding = new FXStringBinding() {
            {
                bind(model.tempProperty());
            }
            @Override
            protected String computeValue() {
                return model.getTemp();
            }
            
        };
        boundTarget.textProperty().bind(uiBinding);
        
        // experiment: implement custom property that notifies on fx thread
        // no, need to go higher up: from XXPropertyBase?
        // even higher: need access markInvalid which is private ...
//        uiTemp = new SimpleStringProperty() {
//
//            @Override
//            protected void invalidated() {
//                Platform.runLater(() -> set(get()));
//            }
//            
//        };
//        uiTemp.bind(model.tempProperty());
//        boundTarget.textProperty().bind(uiTemp);
        // ------ end experiment
    }

    private void updateLabel(Observable ov) {
        Platform.runLater(this::doUpdateLabel); //() -> target.setText(model.getTemp()));
    }
    
    private void doUpdateLabel() {
        target.setText(model.getTemp());
    }
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 200, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SeparateModelUpdateOnUI.class.getName());

}
