/*
 * Created on 23.06.2014
 *
 */
package de.swingempire.fx.junit;

import java.util.logging.Logger;

import javafx.application.Platform;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.junit.FXThreadingRule;
import de.swingempire.fx.junit.JavaFXThreadingRule;
import static junit.framework.TestCase.*;

/**
 * Example of enforcing fx-app thread:
 * 
 * use arbitrary runner combined with classRule for thread 
 * enforcing.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class DummyTestWithRules { //extends TestCase {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    
    @Rule
    public TestRule mRule = new FXThreadingRule();

    @Test
    public void passingTest() {
        LOG.info("nothing done, trivial passt ...");
    }
    
    @Test
    public void failingTest() {
        fail("just saying ....");
    }
    
    @Before
    public void init() {
        LOG.info("fx-at? " + Platform.isFxApplicationThread());
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(DummyTestWithPlatformRunner.class
            .getName());
}
