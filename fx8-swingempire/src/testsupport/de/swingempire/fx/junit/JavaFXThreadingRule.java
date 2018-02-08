/*
 * Created on 23.06.2014
 *
 */
package de.swingempire.fx.junit;


import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javax.swing.SwingUtilities;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.sun.javafx.application.PlatformImpl;

/**
* A JUnit {@link Rule} for running tests on the JavaFX thread and performing
* JavaFX initialisation. To include in your test case, add the following code:
*
* <pre>
* {@literal @}Rule
* public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
* </pre>
*
* http://andrewtill.blogspot.de/2012/10/junit-rule-for-javafx-controller-testing.html
* @author Andy Till
*
* Original is the one with instantiating a JFXPanel.
* 
* <p>
* PENDING JW:
* JDK9 - problem with running Swing on main
* http://stackoverflow.com/q/35726049/203657
* <p>
* changed the rule to start up via PlatformUtils.start (instead of JFXPanel)
* think about reverting
* <p>
* trying to integrate a custom uncaughtExceptionHelper didn't work out ... why not?
* Installing inside the runnable that's passed over to Platform.runLater seems to
* work. Leaving inside for now, need to look out for side-effects. Currrently done
* once on startup of the fx-app thread .. good enough?
*/
public class JavaFXThreadingRule implements TestRule {

    /**
     * Flag for setting up the JavaFX, we only need to do this once for all
     * tests.
     */
    private static boolean jfxIsSetup;

    @Override
    public Statement apply(Statement statement, Description description) {
       return new OnJFXThreadStatement(statement);
    }

    private static class OnJFXThreadStatement extends Statement {

        private final Statement statement;

        public OnJFXThreadStatement(Statement aStatement) {
            statement = aStatement;
        }

        private Throwable rethrownException = null;

        @Override
        public void evaluate() throws Throwable {
            if (!jfxIsSetup) {
                setupJavaFX();
                jfxIsSetup = true;
            }

            final CountDownLatch countDownLatch = new CountDownLatch(1);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        statement.evaluate();
                    } catch (Throwable e) {
                        rethrownException = e;
                    }
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
// if an exception was thrown by the statement during evaluation,
// then re-throw it to fail the test
            if (rethrownException != null) {
                throw rethrownException;
            }

        }

        /**
         * PENDING JW: access of internal fx class PlatformImp.
         * 
         * @throws InterruptedException
         */
        protected void setupJavaFX() throws InterruptedException {
            long timeMillis = System.currentTimeMillis();
            final CountDownLatch latch = new CountDownLatch(1);
            PlatformImpl.startup(() -> {
                installUncaughtExceptionHandler();
                latch.countDown();
            });
            // --- commented original
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    // initializes JavaFX environment
//                    new JFXPanel();
//                    latch.countDown();
//                }
//            });
//            System.out.println("javafx initialising...");
            latch.await();
//            System.out.println("javafx is initialised in " + (System.currentTimeMillis() - timeMillis) + "ms");
        }

        /**
         * Installs a custom uncaughtExceptionHandler. This implementation
         * installs a handler that re-throws runtimeExceptions and passes
         * other types to the threadGroup.
         */
        protected void installUncaughtExceptionHandler() {
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException)throwable;
                } else {
                    Thread.currentThread().getThreadGroup().uncaughtException(thread, throwable);

//                            throw new RuntimeException(throwable);
                }
                });
        }
    }
}
