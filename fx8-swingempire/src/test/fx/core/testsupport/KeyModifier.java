/*
 * Created on 05.09.2018
 *
 */
package fx.core.testsupport;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.tk.Toolkit;

import fx.core.testsupport.unused.StubToolkit;
import javafx.scene.input.KeyCode;
//import test.com.sun.javafx.pgstub.StubToolkit;

/**
 * c&p of test.com.sun.javafx.scene.control.infrastructure.KeyModifier
 */
public enum KeyModifier {
    SHIFT,
    CTRL,
    ALT,
    META;

    public static KeyModifier getShortcutKey() {
        // The StubToolkit doesn't know what the platform shortcut key is, so
        // we have to tell it here (and lets not be cute about optimising this
        // code as we need the platform shortcut key to be known elsewhere in the
        // code base for keyboard navigation tests to work accurately).
        if (Toolkit.getToolkit() instanceof StubToolkit) {
            ((StubToolkit)Toolkit.getToolkit()).setPlatformShortcutKey(
                    PlatformUtil.isMac() ? KeyCode.META : KeyCode.CONTROL);
        }

        switch (Toolkit.getToolkit().getPlatformShortcutKey()) {
            case SHIFT:
                return SHIFT;

            case CONTROL:
                return CTRL;

            case ALT:
                return ALT;

            case META:
                return META;

            default:
                return null;
        }
    }
}
