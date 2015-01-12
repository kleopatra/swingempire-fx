/*
 * Created on 08.01.2015
 *
 */
package de.swingempire.fx.scene.control;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;


/**
 * @author Jeanette Winzenburg, Berlin
 */
public class TreeIgnores {

    public static class IgnoreLog implements IgnoreCondition {

        @Override
        public boolean isSatisfied() {
            return true;
        }
        
    }
    
    private TreeIgnores() {};
}
