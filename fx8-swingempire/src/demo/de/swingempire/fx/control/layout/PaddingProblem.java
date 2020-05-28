/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
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

package de.swingempire.fx.control.layout;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * https://bugs.openjdk.java.net/browse/JDK-8245919
 * 
 * padding on one button affects padding on other
 * to reproduce: run, focus second -> second looses padding
 */
public class PaddingProblem extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        Button button1 = new Button("111");
        Button button2 = new Button("222");
        /*
         * Only the padding of button1 is changed,
         * but it affects the padding of button2.
         */
//        button1.setPadding(new Insets(10,20,10,20));
        button1.setPadding(new Insets(4,8,4,8));
        
        HBox hBox = new HBox(10);
        hBox.getChildren().addAll(button1, button2);
        Scene scene = new Scene(hBox, 300, 300);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        System.out.println(button2.getPadding());
    } 
    
    public static void main(String[] args) {
        launch(args);
    }

}
