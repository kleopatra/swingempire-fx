/*
 * Created on 20.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.DatePickerContent;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;

/**
 * Per picker locale.
 * https://stackoverflow.com/q/27383203/203657
 * 
 * DatePickerSkin creates and accesses DatePickerContent (private, not final)
 *    - datePickerContent is created in getPopupContent (public api)
 *    - datePickerContent is accessed directly
 * DatePickerContent is responsible for formatting content, internal public class in com.sun.*
 *    - delegates all locale-aware formatting to getLocale() public
 *    
 * basically, we could 
 * - subclass DatePickerContent (internal access)
 * - override its getLocale to query for custom locale in the picker's properties
 * - inject custom DatePickerContent into skin (reflective access/modification)
 * - works for content, not for editor
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DatePickerLocale extends Application {

    public static class XDatePickerContent extends DatePickerContent {
       
        public XDatePickerContent(DatePicker datePicker) {
            super(datePicker);
        }

        @Override
        protected Locale getLocale() {
            if (datePicker != null) {
                Object locale = datePicker.getProperties().get("CONTROL_LOCALE");
                if (locale instanceof Locale) {
                    return (Locale) locale;
                }
            }
            return super.getLocale();
        }

    }
    
    public static class XDatePickerSkin extends DatePickerSkin {

        public XDatePickerSkin(DatePicker control) {
            super(control);
            LOG.info("converter: " + getConverter());
        }

        @Override
        public Node getPopupContent() {
            DatePickerContent content = (XDatePickerContent) getDatePickerContent();
            if (!(content instanceof XDatePickerContent)) {
                content = new XDatePickerContent((DatePicker) getSkinnable());
                replaceDatePickerContent(content);
            }
            return content;
        }
        
        //------------- going dirty: reflective access to super
        
        protected DatePickerContent getDatePickerContent() {
            return (DatePickerContent) FXUtils.invokeGetFieldValue(DatePickerSkin.class, this, "datePickerContent");
        }
        
        protected void replaceDatePickerContent(DatePickerContent content) {
            FXUtils.invokeSetFieldValue(DatePickerSkin.class, this, "datePickerContent", content);
        }
    }
    
    private Parent createContent() {
        LocalDate now = LocalDate.now();
        DatePicker picker = new DatePicker(now) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new XDatePickerSkin(this);
            }
            
        };
        
        Locale customLocale = Locale.CHINA;
        // config locale for content
        picker.getProperties().put("CONTROL_LOCALE", customLocale);
        // config locale for chronology/converter
        picker.setChronology(Chronology.ofLocale(customLocale));
        picker.setConverter(new LocalDateStringConverter(FormatStyle.SHORT, 
                customLocale, picker.getChronology()));
        // just to see some formats with default locale
        List<String> patterns = List.of("e", "ee", "eee", "eeee", "eeeee");
        HBox box = new HBox(10);
        patterns.forEach(p -> {
            DateTimeFormatter ccc = DateTimeFormatter.ofPattern(p);
            String name = ccc.withLocale(Locale.getDefault(Locale.Category.FORMAT)).format(now);
            box.getChildren().add(new Label(name));
        });
        
        BorderPane content = new BorderPane(picker);
        content.setBottom(box);
        return content;
    }
 
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent(), 400, 200));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(DatePickerLocale.class.getName());

}
