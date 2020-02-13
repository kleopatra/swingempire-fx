/*
 * Created on 09.02.2020
 *
 */
package de.swingempire.testfx.textinput;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8176270 OOBE when accessing
 * selectedText in listener
 */
public class TextFieldIssueTest extends ApplicationTest {

    private final static Logger LOGGER = Logger
            .getLogger(TextFieldIssueTest.class.getName());
    static {
        Thread.setDefaultUncaughtExceptionHandler(
                TextFieldIssueTest::handleError);
    }

    private static Throwable unchaughtException;

    private TextInputControl textField;

    private static void handleError(Thread t, Throwable e) {
        unchaughtException = e;
        LOGGER.log(Level.SEVERE, "Unchaught exception observed!", e);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // reset exception
        unchaughtException = null;

        VBox vBox = new VBox();
        textField = new TextField();
        textField.setText("1234 5678");
        vBox.getChildren().add(textField);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void test_fail_case01() {
        LOGGER.info("Running test 'fail_case01'!");
        textField.selectedTextProperty()
                .addListener(this::handleSelectionChanged);

        doSelectAndReplace();
    }

    @Test
    public void test_fail_case02() {
        LOGGER.info("Running test 'fail_case02'!");
        textField.selectedTextProperty()
                .addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        // accessing the selectedTextProperty causes a
                        // StringOutOfBoundsException
                        observable.toString();
                    }
                });
        doSelectAndReplace();
    }

    @Test
    public void test_fail_workaround() {
        LOGGER.info("Running test 'fail_case02'!");
        textField.selectedTextProperty()
                .addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        // workaround: ensure that state of selected text
                        // property is
                        // accessed later
                        Platform.runLater(() -> observable.toString());
                    }
                });

        doSelectAndReplace();
    }

    private void handleSelectionChanged(
            ObservableValue<? extends String> observable, String oldValue,
            String newValue) {
        // internally the selectedTextProperty is accessed to get the current
        // value
        // from which
        // causes the StringOutOfBoundsException
    }

    private void doSelectAndReplace() {
        textField.positionCaret(5);
        WaitForAsyncUtils.waitForFxEvents();

        // select 2nd word
        textField.selectNextWord();
        WaitForAsyncUtils.waitForFxEvents();

        // replace selection
        type(KeyCode.DIGIT0);
        WaitForAsyncUtils.waitForFxEvents();

        assert (unchaughtException == null);
    }
}
