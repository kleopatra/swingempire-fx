/*
 * Created on 21.11.2019
 *
 */
package de.swingempire.fx.event;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.sun.javafx.event.BasicEventDispatcher;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/58973367/203657
 * debugging: some key pressed don't reach the textField
 * 
 * reason: all keys are redirected to the popup scene's focusOwner - here
 * the table - which consumes some in its inputMap.
 * 
 * Tentative solution: replace the table's eventDispatcher to intercept
 * events needed in the textField.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ViewPopupApplication extends Application {

    public static class InterceptingEventDispatcher implements EventDispatcher {
        private BasicEventDispatcher original;
        private Predicate<Event> interceptor;
        
        public InterceptingEventDispatcher(BasicEventDispatcher original, Predicate<Event> interceptor) {
            this.original = original;
            this.interceptor = interceptor;
        }

        @Override
        public Event dispatchEvent(Event event, EventDispatchChain tail) {
            if (!interceptor.test(event)) {
                event = original.dispatchCapturingEvent(event);
                if (event.isConsumed()) {
                    return null;
                }
            }
            event = tail.dispatchEvent(event);
            if (event != null && !interceptor.test(event)) {
                event = original.dispatchBubblingEvent(event);
                if (event.isConsumed()) {
                    return null;
                }
            }
            return event;
        }
       
    }
    
    private Parent createContent() {
        TableView<Locale> table = new TableView<>(FXCollections.observableArrayList(Locale.getAvailableLocales()));
        // just to see that right/left are intercepted while up/down are handled
        table.getSelectionModel().setCellSelectionEnabled(true);
        
        TableColumn<Locale, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(new PropertyValueFactory<>("displayCountry"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("displayLanguage"));
        table.getColumns().addAll(country, language);
        // disables default focus traversal
        //  table.setFocusTraversable(false);
        
        // decide which keys to intercept
        List<KeyCode> toIntercept = List.of(KeyCode.LEFT, KeyCode.RIGHT);
        Predicate<Event> interceptor = e -> {
            if (e instanceof KeyEvent) {
                return toIntercept.contains(((KeyEvent) e).getCode());
            }
            return false;
        };
        table.setEventDispatcher(new InterceptingEventDispatcher(
                (BasicEventDispatcher) table.getEventDispatcher(), interceptor));
        
        TextField textField = new TextField("something to show");
        textField.setPrefColumnCount(20);
        textField.setText("something to see");

        table.prefWidthProperty().bind(textField.widthProperty());
        Popup popUp = new Popup();
        popUp.getContent().add(table);
        
        textField.setOnKeyTyped(event -> {
            if(!popUp.isShowing()){
                popUp.show(
                        textField.getScene().getWindow(),
                        textField.getScene().getWindow().getX()
                                + textField.localToScene(0, 0).getX()
                                + textField.getScene().getX(),
                        textField.getScene().getWindow().getY()
                                + textField.localToScene(0, 0).getY()
                                + textField.getScene().getY()
                                + textField.getHeight() - 1);
            }
        });

        BorderPane content = new BorderPane(textField);
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
            .getLogger(ViewPopupApplication.class.getName());

}
