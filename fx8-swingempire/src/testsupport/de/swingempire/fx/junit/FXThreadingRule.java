/*
 * Created on 23.06.2014
 *
 */
package de.swingempire.fx.junit;


import javafx.application.Platform;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Method rule as precondition for all fx-tests.
 * @author kleopatra
 */
public class FXThreadingRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        System.out.println("threading rule: on fx-at " + Platform.isFxApplicationThread());
        if (!Platform.isFxApplicationThread())
              throw new IllegalStateException("test must be run on fx-at " + description ); 
        return base;
    }    
    
}
