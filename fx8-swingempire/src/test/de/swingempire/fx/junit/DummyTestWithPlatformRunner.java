/*
 * Created on 23.06.2014
 *
 */
package de.swingempire.fx.junit;


import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.*;

import javafx.application.Platform;

/**
 * Example of how-to enforce fx-app thread in testing:
 * 
 * use the Platformrunner w/out additional rule (double safety net).
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(PlatformRunner.class)
public class DummyTestWithPlatformRunner { //extends TestCase {

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
