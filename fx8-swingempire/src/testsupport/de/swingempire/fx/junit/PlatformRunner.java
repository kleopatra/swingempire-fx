/*
 * Created on 23.06.2014
 *
 */
package de.swingempire.fx.junit;

import com.sun.javafx.application.PlatformImpl;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
* A test runner for JUnit that ensures that all tests are run on the 
* FX-Application thread.<p>
* 
* Initializes the fx environment during instantiation and guarantees to
* run on the fx thread.
* 
* Inspired by
* http://andrewtill.blogspot.de/2012/10/junit-rule-for-javafx-controller-testing.html
* @author Andy Till
*
* PENDING: still experimenting with several options to kick off the fx-at. 
*/
public class PlatformRunner extends BlockJUnit4ClassRunner {

   /**
    * Creates a test runner for the specified test class. Implemented
    * to initialize the fx environment.
    *
    * @param klass the class to test
    * @throws InitializationError if a problem occurs during object
    * construction
    */
   public PlatformRunner(Class<?> klass) throws InitializationError {
       super(klass);
       try {
           setupJavaFX();
       } catch (InterruptedException e) {
           throw new InitializationError("setup failed");
       }
   }

   protected void setupJavaFX() throws InterruptedException {
       System.out.println("init runner");
       final CountDownLatch latch = new CountDownLatch(1);
       SwingUtilities.invokeLater(new Runnable() {
           @Override
           public void run() {
//               System.out.println("init runner: in invoke" );
               // initializes JavaFX environment
               new JFXPanel();
               latch.countDown();
           }
       });
       System.out.println("init runner: after invoke, waiting ..." );
       latch.await();
        System.out.println("init runner: after waiting ..." );
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void run(final RunNotifier notifier) {
       if (Platform.isFxApplicationThread()) {
           superRun(notifier, "on fx-at");
       } else {
//           invokeAndWaitWithLatch(notifier);
           invokeAndWaitWithImpl(notifier);
       }
   }

   /**
    * Invokes super on FX-AT and waits via PlatformImpl.runAndWait. 
    * Dirty due to usage 
    * of sun class
    * 
    * @param notifier 
    */
   protected void invokeAndWaitWithImpl(final RunNotifier notifier) {
      PlatformImpl.runAndWait(new Runnable() {
      
           @Override
           public void run() {
              superRun(notifier, "wait in impl");
           }
       });
   }   

   /**
    * Invokes super on the FX-AT and waits with the help of a 
    * CountDownLatch.
    * 
    * @param notifier 
    */
   protected void invokeAndWaitWithLatch(final RunNotifier notifier)  {
       final CountDownLatch latch = new CountDownLatch(1);
       Platform.runLater(new Runnable() {
           @Override
           public void run() {
               superRun(notifier, "waitWithLatch");
               latch.countDown();
           }
       });
       try {
           latch.await();
       } catch (InterruptedException ex) {
           // what to do here?
           throw new RuntimeException("got interrupted ", ex);
       }
   }
   
   protected void superRun(final RunNotifier notifier, String source) {
       // debug ... 
       // LOG.info("run by " + source);
       super.run(notifier);
   }
   
   @SuppressWarnings("unused")
   private static final Logger LOG = Logger.getLogger(PlatformRunner.class
           .getName());

}
