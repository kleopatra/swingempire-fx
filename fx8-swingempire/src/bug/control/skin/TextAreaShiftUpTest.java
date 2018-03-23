/*
 * Created on 23.03.2018
 *
 */
package control.skin;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * up/down in first/last line should move cursor to
 * start/end of line, same for shift
 * 
 * https://bugs.openjdk.java.net/browse/JDK-8200108
 * 
 * seems to be variable behaviour
 * med, notepad: as fx
 * scribbles: as expected for shift+up, no move for up alone
 * editor in bug system, twitter, eclipse: as expected in bug report
 */
public class TextAreaShiftUpTest extends Application {

    public static class SpecialMoveTextAreaSkin extends TextAreaSkin {

        /**
         * @param arg0
         */
        public SpecialMoveTextAreaSkin(TextArea area) {
            super(area);
        }

        /**
         * Implemented to trigger a move to line start/end if
         * super move didn't move the caret.
         * 
         * Note: this is slightly different from those that
         * fully support the to-start/end-on-up in that the 
         * move into the opposite direction doesn't end at the
         * position of the previous (before the additional) move. 
         */
        @Override
        public void moveCaret(TextUnit unit, Direction direction,
                boolean select) {
            int oldCaret = getSkinnable().getCaretPosition();
            super.moveCaret(unit, direction, select);
            int currentCaret = getSkinnable().getCaretPosition();
            // TODO: add some property to enable/disable this behaviour
            if (oldCaret == currentCaret) {
                if (oldCaret == 0 || oldCaret == getSkinnable().getLength()) {
                    // nothing to do, at start/end of document
                } else if (TextUnit.LINE == unit) {
                    if (Direction.UP == direction) {
                        super.moveCaret(unit, Direction.BEGINNING, select);
                    } else if (Direction.DOWN == direction) {
                        super.moveCaret(unit, Direction.END, select);
                    }
                }
            }
        }
        
        
    }
    @Override
    public void start(Stage stage) throws Exception {
        TextArea textArea = new TextArea(
                "shift+arrow up\n****************\nshift+arrow down") {

                    @Override
                    protected Skin<?> createDefaultSkin() {
                        return new SpecialMoveTextAreaSkin(this);
                    }
            
            
        };

//         installFix(textArea);

        Label caretPos = new Label();
        textArea.caretPositionProperty()
                .addListener((ob, oldBalue, newValue) -> {
                    int pos = newValue.intValue();
                    String subtext = textArea.getText().substring(0, pos);
                    long line = subtext.codePoints().filter((c) -> c == '\n')
                            .count() + 1;
                    caretPos.setText("line:" + line);

                });

        VBox root = new VBox(8, textArea, caretPos, new TextField("123456789"));

        Scene scene = new Scene(root, 400, 400);

        stage.setScene(scene);

        stage.show();
    }

    private static void installFix(final TextArea textArea) {
        textArea.addEventFilter(KeyEvent.ANY, (e) -> {
            if (e.getEventType() != KeyEvent.KEY_PRESSED) {
                return;
            }

            final boolean isCtrlDown = IS_MAC_OS ? e.isMetaDown()
                    : e.isControlDown();
            // FIX: UP
            if (!e.isShiftDown() && !isCtrlDown && e.getCode() == KeyCode.UP) {
                final int line = possitionToLine(textArea,
                        textArea.getCaretPosition());
                if (line == 1) {
                    textArea.home();
                    e.consume();
                }
            }
            // FIX: DOWN
            if (!e.isShiftDown() && !isCtrlDown
                    && e.getCode() == KeyCode.DOWN) {
                final int line = possitionToLine(textArea,
                        textArea.getCaretPosition());
                final int lastLine = countLines(textArea);
                if (lastLine == line) {
                    textArea.end();
                    e.consume();
                }
            }
            // FIX: SHift+UP
            if (e.isShiftDown() && !isCtrlDown && e.getCode() == KeyCode.UP) {
                final int line = possitionToLine(textArea,
                        textArea.getCaretPosition());
                if (line == 1) {
                    e.consume();
                    Platform.runLater(() -> {
                        final KeyEvent event = new KeyEvent(
                                KeyEvent.KEY_PRESSED, null, null, KeyCode.HOME,
                                true, false, false, false);
                        Event.fireEvent(e.getTarget(), event);

                        final KeyEvent event2 = new KeyEvent(
                                KeyEvent.KEY_RELEASED, null, null, KeyCode.HOME,
                                true, false, false, false);
                        Event.fireEvent(e.getTarget(), event2);

                    });
                }
            }
            // FIX: SHift+DOWN
            if (e.isShiftDown() && !isCtrlDown && e.getCode() == KeyCode.DOWN) {
                final int line = possitionToLine(textArea,
                        textArea.getCaretPosition());
                final int lastLine = countLines(textArea);
                if (lastLine == line) {
                    e.consume();
                    Platform.runLater(() -> {
                        final KeyEvent event = new KeyEvent(
                                KeyEvent.KEY_PRESSED, null, null, KeyCode.END,
                                true, false, false, false);
                        Event.fireEvent(e.getTarget(), event);

                        final KeyEvent event2 = new KeyEvent(
                                KeyEvent.KEY_RELEASED, null, null, KeyCode.END,
                                true, false, false, false);
                        Event.fireEvent(e.getTarget(), event2);
                    });
                }
            }
        });
    }

    private static int possitionToLine(final TextArea textArea, final int pos) {
        final String subtext = textArea.getText().substring(0, pos);
        final long line = subtext.codePoints().filter((c) -> c == '\n').count()
                + 1;
        return (int) line;
    }

    private static int countLines(final TextArea textArea) {
        return (int) textArea.getText().codePoints().filter((c) -> c == '\n')
                .count() + 1;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TextAreaShiftUpTest.class.getName());
    private static final boolean IS_MAC_OS = System.getProperty("os.name")
            .contains("Mac OS");
}