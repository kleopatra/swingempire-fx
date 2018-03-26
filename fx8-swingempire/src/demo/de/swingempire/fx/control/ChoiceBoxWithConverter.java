/*
 * Created on 14.03.2018
 *
 */
package de.swingempire.fx.control;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * beginners question (wrong path, OP asked for two mapped lists when
 * a converter is the solution)
 * https://stackoverflow.com/q/49271999/203657
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxWithConverter extends Application {

    
    private Parent createContent() {
        ChoiceBox<User> choiceBox =  new ChoiceBox<>(getUsers()); 
            // new ChoiceBox<>(sql.getAllUsers());
        StringConverter<User> converter = new StringConverter<>() {

            @Override
            public String toString(User user) {
                return user != null ? user.getUserLogin() : "";
            }
            
            @Override
            public User fromString(String userLogin) {
                // should never happen, choicebox is not editable
                throw new UnsupportedOperationException("back conversion not supported from " + userLogin);
            }
            
            
        };
        choiceBox.setConverter(converter);
        
        // experimenting: any way to map a converter as functional interface?
        StringConverter<User> base = 
                new BaseConverter<>(User::getUserLogin);
//                BaseConverter::new;
        ToString<User> conv = c -> c.getUserLogin();
        ToString<User> con = User::getUserLogin;
        StringConverter<User> conx = ((ToString<User>) (User::getUserLogin)).as();
        StringConverter<User> co = conv.as();
        StringConverter<User> x = ((ToString<User>) c -> c.getUserLogin()).as();
        choiceBox.setConverter(new BaseConverter<>(User::getUserLogin));
        choiceBox.setConverter(createConverter());
        choiceBox.setConverter(asStringConverter(User::getUserLogin));
        BorderPane pane =  new BorderPane(choiceBox);
        return pane;
    }
    
    public StringConverter<User> createConverter() {
        return new BaseConverter<>(User::getUserLogin);
    }
    
    public <T> StringConverter<T> asStringConverter(Function<T, String> f) {
        return new BaseConverter<>(f);
    }
    
    public static class BaseConverter<T> extends StringConverter<T> {
        Function<T, String> conv;
        public BaseConverter(Function<T, String> conv) {
            this.conv = conv;
        }
        @Override
        public String toString(T arg0) {
            return conv.apply(arg0);
        }
        @Override
        public T fromString(String arg0) {
            // should never happen, must not be used if editable
            throw new UnsupportedOperationException(
                    "back conversion not supported from " + arg0);
        }
    }
    
    @FunctionalInterface
    public static interface ToString<T> {
        String toString(T item);
        default StringConverter<T> as() {
            return new StringConverter<T>() {

                @Override
                public T fromString(String arg0) {
                    // should never happen, must not be used if editable
                    throw new UnsupportedOperationException(
                            "back conversion not supported from " + arg0);
                }

                @Override
                public String toString(T arg0) {
                    return ToString.this.toString(arg0);
                }
                
            };
        }
    }
    /**
     * Convenience implementation for not-editable contexts.
     */
    public abstract static class ToStringConverter<T> extends StringConverter<T> {
        @Override
        public T fromString(String text) {
            // should never happen, must not be used if editable
            throw new UnsupportedOperationException(
                    "back conversion not supported from " + text);
        }
        
    }
//    public class ConverterAdapter<T> extends StringConverter<T> 
//        implements ToStringConverter<T> {
//
//        @Override
//        public String toString(T arg0) {
//            return null;
//        }
//    }
//
//    public interface ToStringConverter<T> {
//        
//        String toString(T item);
//        
//        // the default method doesn't count as implementation
//        default T fromString(String text) {
//            // should never happen, must not be used if editable
//            throw new UnsupportedOperationException("back conversion not supported from " + text);
//        }
//    }
    /**
     * @return
     */
    protected ObservableList<User> getUsers() {
        return User.users();
    }

    private static class User extends Person {

        public User(String fName, String lName) {
            super(fName, lName);
        }
        
        public String getUserLogin() {
            return getLastName();
        }
        public static ObservableList<User> users() {
            ObservableList<Person> persons = Person.persons();
            List<User> users = persons.stream()
                    .map(p -> new User(p.getFirstName(), p.getLastName()))
                    .collect(Collectors.toList());
            return FXCollections.observableArrayList(users);
        }
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
            .getLogger(ChoiceBoxWithConverter.class.getName());

}
