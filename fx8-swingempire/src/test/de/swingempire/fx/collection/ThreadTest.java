/*
 * Created on 30.09.2018
 *
 */
package de.swingempire.fx.collection;

import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.sun.javafx.application.PlatformImpl;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

/**
 * Trying to test threading ... wait until something is done..
 * not working, giving up ..
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ThreadTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    
//    @ClassRule
//    public static TestRule classRule = new JavaFXThreadingRule();

    @Test
    public void testThread() throws InterruptedException {
        long timeMillis = System.currentTimeMillis();
        final CountDownLatch latch = new CountDownLatch(1);
        PlatformImpl.startup(() -> {
//            installUncaughtExceptionHandler();
            latch.countDown();
        });
        latch.await();
        IntegerProperty counter = new SimpleIntegerProperty();
        int value = 10;
        LOG.info("fx thread in plain " + Platform.isFxApplicationThread());
        // doesn't work if threading fx rule is on nor off
        ObjectProperty<CountDownLatch> cd = new SimpleObjectProperty<>();
//        final CountDownLatch countDownLatch = new CountDownLatch(1);
        LOG.info("we are here");
        Duration duration = Duration.millis(2000);
            Runnable r = () -> {
                Timeline timeline = new Timeline(new KeyFrame(duration, e -> {
                    counter.set(value);
                    //          countDownLatch.countDown();
                }));
                timeline.currentTimeProperty().addListener((src, ov, nv) -> {
                    LOG.info("nv: " + nv);
                });
                timeline.play();
                cd.set(new CountDownLatch(1));
                int i = 0;
                long longValue = Double.valueOf(duration.toMillis()).longValue();
                LocalTime now = LocalTime.now();
                while (i++ < 1) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LocalTime later = LocalTime.now();
//                    LOG.info("in thread: " + i + " / " + longValue + " - " + now
//                            + "/" + later);
              LOG.info("" + timeline.getCurrentTime());
//              Platform.runLater(() -> {
//                  
//              });
                }
//                countDownLatch.countDown();
                
            };
//            PlatformImpl.runAndWait(r);
//      Platform.runLater(r);
            Thread th = new Thread(r);
            th.setDaemon(true);
            th.start();
        LOG.info("at end ..");
        cd.get().await();

    }
    /**
     * just trying to hook into output ... threading prob
     * doesn't work, something basic wrong ..
     * @throws InterruptedException
     */
    @Test
    public void testTimeline() throws InterruptedException {
//        IntegerProperty counter = new SimpleIntegerProperty();
//        int value = 10;
//        Duration duration = Duration.millis(2000);
//        Timeline timeline = new Timeline(new KeyFrame(duration, e -> {
//            counter.set(value);
////            countDownLatch.countDown();
//        }));
//        timeline.currentTimeProperty().addListener((src, ov, nv) -> {
//            LOG.info("nv: " + nv);
//        });
//        final CountDownLatch countDownLatch = new CountDownLatch(1);
//        Runnable r = () -> {
//            timeline.playFromStart();
//            LOG.info("fx thread " + Platform.isFxApplicationThread() + timeline.getStatus());
//            int i = 0;
//            long longValue = Double.valueOf(duration.toMillis()).longValue();
//            LocalTime now = LocalTime.now();
//            while (i++ < 20) {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                LocalTime later = LocalTime.now();
////                LOG.info("in thread: " + i + " / " + longValue + " - " + now
////                        + "/" + later);
//                LOG.info("" + timeline.getCurrentTime());
////                Platform.runLater(() -> {
////                    
////                });
//            }
//            countDownLatch.countDown();
//
//        };
////        Platform.runLater(r);
//        Thread th = new Thread(r);
//        th.setDaemon(true);
//        th.start();
////        countDownLatch.await();
//
//        // wait until ready, then test
//        assertEquals(value, counter.get());
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ThreadTest.class.getName());
}
