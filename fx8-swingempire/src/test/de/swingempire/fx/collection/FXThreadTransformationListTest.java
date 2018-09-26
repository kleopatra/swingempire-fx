/*
 * Created on 31.01.2018
 *
 */
package de.swingempire.fx.collection;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ListChangeReport;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Not working - can't get the threading correct ...
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
//@SuppressWarnings({ "rawtypes", "unchecked" })
public class FXThreadTransformationListTest {
    
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    private ObservableList<String> source;

    
//    @Test
//    public void testSimpleAddNotification() throws InterruptedException {
//        TransformationList<String, String> fx = new FXThreadTransformationList<>(source);
//        ListChangeReport report = new ListChangeReport(fx);
//        doOnThread(c -> source.add("added"));
////        LOG.info("" + fx);
////        LOG.info("count: " + report.getEventCount());
////        assertEquals(1, report.getEventCount());
//    }
    
    /**
     * Problem: plain listener is notified as expected, report is not .. why?
     * looks like the sequence is indeterminate - the plain listener is notified
     * or not erratically (during the lifetime of the testing method)
     * @throws InterruptedException
     */
    @Test
    public void testAddOnFXThread() throws InterruptedException {
        TransformationList<String, String> fx = new FXThreadTransformationList<>(source);
        ListChangeReport report = new ListChangeReport(fx);
        fx.addListener((ListChangeListener<String>)  c -> {
            LOG.info("in plain listener: ");
              assertTrue("notification must be on fx thread", Platform.isFxApplicationThread());
              int added = 0;
              while(c.next()) {
                  if (c.wasAdded()) {
                      added += c.getAddedSize();
                  }
              }
              assertEquals("must have added", 1, added);
         });
        doOnThread(input -> source.add("added"));
        Platform.runLater(() -> {
            LOG.info("testing in runlater: " + report);
            assertEquals(1, report.getEventCount());
        }) ;
//        LOG.info("at end report: " + report);
    }

    protected void doOnThread(Consumer<String> consumer) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Thread thread = new Thread(() ->  {
            consumer.accept(null);
            latch.countDown();
        });
        thread.start();
        latch.await();
        
    }
    @Before
    public void setup() {
        source = FXCollections.observableArrayList("1", "2", "3");
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(FXThreadTransformationListTest.class.getName());
}
