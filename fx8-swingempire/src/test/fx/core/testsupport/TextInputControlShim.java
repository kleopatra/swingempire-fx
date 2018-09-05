/*
 * Created on 05.09.2018
 *
 */
package fx.core.testsupport;

import de.swingempire.fx.util.FXUtils;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.TextInputControl;

/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
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
//package javafx.scene.control;

// this is a protected interface in TextInputControl
// is-a ObservableStringValue
//import javafx.scene.control.TextInputControl.Content;

/**
 * c&p from core: replaced direct access to internal class by reflection
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class TextInputControlShim {

    public static ObservableStringValue getContentObject(TextInputControl tic) {
        return (ObservableStringValue) FXUtils.invokeGetFieldValue(TextInputControl.class, tic, "content");
    }
    
//    public static Content getContent(TextInputControl tic) {
//        return tic.getContent();
//    }

    public static String getContent_get(TextInputControl tic,
            int start, int end) {
        Object content = getContentObject(tic);
        return (String) FXUtils.invokeGetMethodValue(content.getClass(), content, "get", 
                new Class[] {Integer.TYPE, Integer.TYPE},
                new Object[] {start, end});
    }
    
//    public static String getContent_get(TextInputControl tic,
//            int start, int end) {
//        return tic.getContent().get(start, end);
//    }

    
    public static void getContent_insert(TextInputControl tic,
            int index, String text,
            boolean notifyListeners) {
        Object content = getContentObject(tic);
        FXUtils.invokeGetMethodValue(content.getClass(), content, "insert", 
                new Class[] {Integer.TYPE, String.class, Boolean.TYPE},
                new Object[] {index, text, notifyListeners});
    }
//    public static void getContent_insert(TextInputControl tic,
//            int index, String text,
//            boolean notifyListeners) {
//        tic.getContent().insert(index, text, notifyListeners);
//    }
//
    
    
    public static void getContent_delete(TextInputControl tic,
            int start, int end,
            boolean notifyListeners) {
        Object content = getContentObject(tic);
        FXUtils.invokeGetMethodValue(content.getClass(), content, "delete", 
                new Class[] {Integer.TYPE, Integer.TYPE, Boolean.TYPE},
                new Object[] {start, end, notifyListeners});
    }
    
//    public static void getContent_delete(TextInputControl tic,
//            int start, int end,
//            boolean notifyListeners) {
//        tic.getContent().delete(start, end, notifyListeners);
//    }

}
