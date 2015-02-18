/*
 * Created on 12.02.2015
 *
 */
package de.swingempire.fx.collection;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class Filters {

    public static class EqualsPredicate<T> implements Predicate<T> {

        private T matchValue;
        
        public EqualsPredicate(T matchValue) {
            this.matchValue = Objects.requireNonNull(matchValue);
        }
        @Override
        public boolean test(T t) {
            return matchValue.equals(t);
        }
        
        protected boolean areEquals() {
            return false;
        }
    }
    
    private Filters() {};
}
