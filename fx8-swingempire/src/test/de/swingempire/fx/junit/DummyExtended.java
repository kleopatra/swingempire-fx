/*
 * Created on 30.09.2014
 *
 */
package de.swingempire.fx.junit;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class DummyExtended extends DummyTestConditionalIgnore {
    // repeating the role from super doesn't help
//    @Rule
//    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    /**
     * @return
     */
    @Override
    public boolean supportsSeparators() {
        return false;
    }

}
