/*
 * Created on 04.09.2017
 *
 */
package test.tableeditcore;

/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * c&p from webrev http://cr.openjdk.java.net/~jgiles/8177375.1/
 * 
 * used in fix of Improve Atomicity of SelectionModel properties
 * https://bugs.openjdk.java.net/browse/JDK-8177375
 * 
 */
public class AtomicProperties {

    private AtomicProperties() { }

    public static void atomicChange(Change... changes) {
        for (Change change : changes) {
            atomicProperty(change).ifPresent(AtomicProperty::lock);
        }

        for (Change change : changes) {
            change.apply();
        }

        for (Change change : changes) {
            atomicProperty(change).ifPresent(AtomicProperty::unlock);
        }
    }

    private static boolean isAtomicProperty(Change change) {
        return change instanceof PropertyChange && ((PropertyChange)change).property instanceof AtomicProperty;
    }

    private static Optional<AtomicProperty> atomicProperty(Change change) {
        return isAtomicProperty(change) ? Optional.of((AtomicProperty)((PropertyChange)change).property) : Optional.empty();
    }

    private interface AtomicProperty<T> {
        void lock();
        void unlock();
        void mute();
        void unmute();
    }

    private static class AtomicPropertyHelper implements AtomicProperty {
        private boolean locked = false;
        private boolean muted = false;
        private boolean fireAttempted = false;
        private final Runnable fireHandler;

        public AtomicPropertyHelper(Runnable fireHandler) {
            this.fireHandler = fireHandler;
        }

        @Override public void lock() {
            if (!locked) {
                fireAttempted = false;
            }

            locked = true;
        }

        @Override public void unlock() {
            locked = false;
            if (fireAttempted) {
                if (!muted) {
                    fireHandler.run();
                }
                fireAttempted = false;
            }
        }

        public final boolean isFireAllowed() {
            if (muted) return false;
            fireAttempted = true;
            return !locked;
        }

        @Override public void mute() {
            muted = true;
        }

        @Override public void unmute() {
            muted = false;
        }
    }

    public static class AtomicReadOnlyIntegerWrapper extends ReadOnlyIntegerWrapper implements AtomicProperty {
        private AtomicPropertyHelper helper = new AtomicPropertyHelper(this::fireValueChangedEvent);

        public AtomicReadOnlyIntegerWrapper(Object bean, String name, int initialValue) {
            super(bean, name, initialValue);
        }

        @Override protected void fireValueChangedEvent() {
            if (helper.isFireAllowed()) {
                super.fireValueChangedEvent();
            }
        }
        @Override public void lock() { helper.lock(); }
        @Override public void unlock() { helper.unlock(); }
        @Override public void mute() { helper.mute(); }
        @Override public void unmute() { helper.unmute(); }
    }

    public static class AtomicReadOnlyObjectWrapper<T> extends ReadOnlyObjectWrapper<T> implements AtomicProperty {
        private AtomicPropertyHelper helper = new AtomicPropertyHelper(this::fireValueChangedEvent);

        public AtomicReadOnlyObjectWrapper(T initialValue) {
            super(initialValue);
        }

        public AtomicReadOnlyObjectWrapper(Object bean, String name, T initialValue) {
            super(bean, name, initialValue);
        }

        @Override protected void fireValueChangedEvent() {
            if (helper.isFireAllowed()) {
                super.fireValueChangedEvent();
            }
        }
        @Override public void lock() { helper.lock(); }
        @Override public void unlock() { helper.unlock(); }
        @Override public void mute() { helper.mute(); }
        @Override public void unmute() { helper.unmute(); }
    }


    public interface Change {
        void apply();

        default AtomicMode getMode() {
            return AtomicMode.NORMAL;
        }
    }

    public static class PropertyChange<T> implements Change {
        private final Property<T> property;
        private final T newValue;
        private final AtomicMode mode;
        private final BooleanSupplier test;

        public PropertyChange(Property<T> property, T newValue) {
            this(property, newValue, null);
        }

        public PropertyChange(Property<T> property, T newValue, BooleanSupplier test) {
            this(property, newValue, test, AtomicMode.NORMAL);
        }

        public PropertyChange(Property<T> property, T newValue, BooleanSupplier test, AtomicMode mode) {
            this.property = property;
            this.newValue = newValue;
            this.test = test;
            this.mode = mode;
        }

        @Override public void apply() {
            if (property == null) return;
            if (test != null && !test.getAsBoolean()) return;

            atomicProperty(this).ifPresent(ap -> {
                if (mode == AtomicMode.SILENT) ap.mute();
            });
            property.setValue(newValue);
            atomicProperty(this).ifPresent(ap -> {
                if (mode == AtomicMode.SILENT) ap.unmute();
            });
        }

        @Override public AtomicMode getMode() {
            return mode;
        }
    }

    public static class RunnableChange implements Change {
        private Runnable r;

        public RunnableChange(Runnable r) {
            this.r = r;
        }

        @Override public void apply() {
            r.run();
        }
    }

    public enum AtomicMode {
        NORMAL, SILENT;
    }
}
