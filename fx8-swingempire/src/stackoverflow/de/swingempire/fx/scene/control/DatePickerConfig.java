/*
 * Created on 20.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.sun.javafx.scene.control.DatePickerContent;

import static java.time.temporal.ChronoUnit.*;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Replace icon in button (was answered)
 * Localize to Chinese with narrow week of day formatter.
 * https://stackoverflow.com/q/46403564/203657
 * 
 * DatePickerSkin creates and accesses DatePickerContent (private, not final)
 *    - datePickerContent is created in getPopupContent (public api)
 *    - datePickerContent is accessed directly
 * DatePickerContent is responsible, internal public class in com.sun.*
 * has DateCells in List dayNameCells (private), style day-name-cell
 * formatted with weekDayNameFormatter (private), has pattern "ccc"
 * content updated in public updateDayNameCells, via plain setText of formatted day
 * 
 * basically, we could 
 * - subclass DatePickerContent (internal access)
 * - override updateDayNameCells to find dayNamecells and do our own formatting
 * - inject custom DatePickerContent into skin (reflective access/modification)
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class DatePickerConfig extends Application {

    /**
     * Custom picker content which allows to tweak the formatter for weekDay cells.
     */
    public static class XDatePickerContent extends DatePickerContent {
       
        private DateTimeFormatter weekDayNameFormatter;
        
        public XDatePickerContent(DatePicker datePicker) {
            super(datePicker);
        }

        /**
         * c&p from super to format with narrow formatter
         */
        @Override
        public void updateDayNameCells() {
            // first day of week, 1 = monday, 7 = sunday
            Locale locale = getLocale();
            int firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek().getValue();

            List<DateCell> dayNameCells = getDayNameCells();
            int daysPerWeek = getDaysPerWeek();
            // july 13th 2009 is a Monday, so a firstDayOfWeek=1 must come out of the 13th
            LocalDate date = LocalDate.of(2009, 7, 12 + firstDayOfWeek);
            DateTimeFormatter weekDayNameFormatter = getWeekDayNameFormatter();
            for (int i = 0; i < daysPerWeek; i++) {
                String name = weekDayNameFormatter.withLocale(locale).format(date.plus(i, DAYS));
                dayNameCells.get(i).setText(getTitleCaseWord(name));
            }
        }

        /**
         * Lazily creates and returns the formatter for week days.
         * Note: this is called from the constructor which implies that
         * the field is not available.
         */
        private DateTimeFormatter getWeekDayNameFormatter() {
            if (weekDayNameFormatter == null) {
                weekDayNameFormatter =
                        createWeekDayNameFormatter(); // 
            }
            return weekDayNameFormatter;
        }

        /**
         * Factory method for weekDayNameFormatter, here: narrow standalone day name
         */
        protected DateTimeFormatter createWeekDayNameFormatter() {
            return DateTimeFormatter.ofPattern("ccccc");
        }
       
        // ------------- going dirty: reflective access to super
        
        protected String getTitleCaseWord(String str) {
            return (String) FXUtils.invokeGetMethodValue(DatePickerContent.class, this, "titleCaseWord", String.class, str);
        }
        
        protected int getDaysPerWeek() {
            return (int) FXUtils.invokeGetFieldValue(DatePickerContent.class, this, "daysPerWeek");
        }

        protected List<DateCell> getDayNameCells() {
            return (List<DateCell>) FXUtils.invokeGetFieldValue(DatePickerContent.class, this, "dayNameCells");
        }
    }
    
    /**
     * Custom picker skin that reflectively injects the custom content.
     */
    public static class XDatePickerSkin extends DatePickerSkin {

        public XDatePickerSkin(DatePicker control) {
            super(control);
        }

        /**
         * Overridden to reflectively inject the custom picker content.
         */
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
        Locale.setDefault(Locale.CHINA);
        LocalDate now = LocalDate.now();
        DatePicker picker = new DatePicker(now) {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new XDatePickerSkin(this);
            }
            
        };
        // just to see some options
        List<String> patterns = List.of("c", "ccc", "cccc", "ccccc", "e", "ee", "eee", "eeee", "eeeee");
        HBox box = new HBox(10);
        patterns.forEach(p -> {
            DateTimeFormatter ccc = DateTimeFormatter.ofPattern(p);
            String name = ccc.withLocale(getLocale()).format(now);
            box.getChildren().add(new Label(name));
        });
        
        BorderPane content = new BorderPane(picker);
        content.setBottom(box);
        return content;
    }
    
    protected Locale getLocale() {
        return Locale.getDefault(Locale.Category.FORMAT);
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
            .getLogger(DatePickerConfig.class.getName());

}
