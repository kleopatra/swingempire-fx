/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyEvent;

public class AgePickerCell<Inputs> extends TableCell<Inputs, LocalDate> {

        private final DatePicker datePicker = new DatePicker();;

        public AgePickerCell() {

                datePicker.addEventFilter(KeyEvent.KEY_PRESSED, t -> {
                        switch (t.getCode()) {
                        case ENTER:
                                cancelEdit();
                                break;
                        case ESCAPE:
                                cancelEdit();
                                break;
                        case TAB:
                                cancelEdit();
                                break;
                        default:
                                break;
                        }
                });

                datePicker.valueProperty().addListener((bean, oldVal, newVal) -> {
                        if (newVal != null) {
                                Platform.runLater(() -> commitEdit(newVal));
                        }
                });

                datePicker.focusedProperty().addListener((bean, oldVal, newVal) -> {
                        if (!newVal) {
                                cancelEdit();
                        }
                });

                setOnMouseClicked(e -> {
                        if (getContentDisplay() == ContentDisplay.TEXT_ONLY) {
                                startEdit();
                        }
                });

                setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void commitEdit(LocalDate newValue) {
                super.commitEdit(newValue);
        }

        @Override
        public void startEdit() {
                super.startEdit();
                if (!isEmpty()) {
                        setGraphic(datePicker);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        Platform.runLater(() -> datePicker.requestFocus());
                }
        }

        @Override
        public void cancelEdit() {
                super.cancelEdit();
                setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                // TODO UPDATE DATE
                if (item != null) {
                        this.setItem(item);
                        datePicker.setValue(item);
                        int between = (int) ChronoUnit.YEARS.between(item, LocalDate.now());
                        setText(String.valueOf(between));
                }
        }
}
