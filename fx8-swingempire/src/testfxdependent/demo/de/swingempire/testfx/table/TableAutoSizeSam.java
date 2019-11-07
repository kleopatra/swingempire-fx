/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
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

package de.swingempire.testfx.table;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Digging into test failure on decrease: here both test and app
 * are misbehaving.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TableAutoSizeSam extends Application {
    public static class MyTableColumnHeader extends TableColumnHeader {

        public MyTableColumnHeader(final TableColumnBase tc) {
            super(tc);
        }

        public void resizeCol() {
            /*
             * Note: this compiles only with a tweaked TableColumnHeader in
             * experiment-tableheader-sam-test!
             */
//            doColumnAutoSize(getTableColumn(), -1);
            resizeColumnToFitContent(-1);
        }
        
        private void resizeColumnToFitContent(int rows) {
           FXUtils.invokeGetMethodValue(TableColumnHeader.class, 
                    this, "doColumnAutoSize", 
                    new Class[] {TableColumnBase.class, Integer.TYPE} , 
                    new Object[] {getTableColumn(), rows});
        }
    }
    
    MyTableColumnHeader tableColumnHeader;
    
    private Parent createContent() {
//        model = Person.persons();
        model = FXCollections.observableArrayList(
                new Person("Humphrey McPhee", null),
                new Person("Justice Caldwell", null),
                new Person("Orrin Davies", null),
                new Person("Emma Wilson", null)
        );

        initialFirst = model.get(0).getFirstName();
        bigValue = "This is a big text inside that column";
        
        column = new TableColumn<>("Col ");
        column.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));
        tableView = new TableView<>(model) {
            @Override
            protected Skin<?> createDefaultSkin() {
                return new TableViewSkin(this) {
                    @Override
                    protected TableHeaderRow createTableHeaderRow() {
                        return new TableHeaderRow(this) {
                            @Override
                            protected NestedTableColumnHeader createRootHeader() {
                                return new NestedTableColumnHeader(null) {
                                    @Override
                                    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
                                        tableColumnHeader = new MyTableColumnHeader(column);
                                        return col == null || col.getColumns().isEmpty() || col == getTableColumn() ?
                                                tableColumnHeader :
                                                new NestedTableColumnHeader(col);

                                    }
                                };
                            }
                        };
                    }
                };
            }
        };

        tableView.getColumns().add(column);

        Button initial = new Button("initial measure");
        initial.setOnAction(e -> {
            double width = column.getWidth();
            tableColumnHeader.resizeCol();
            LOG.info("width/resized same: " + width + "/" + column.getWidth());
        });
        
        Button biggerEdit = new Button("bigger with edit");
        biggerEdit.setOnAction(e -> {
            double width = column.getWidth();
            EventType<TableColumn.CellEditEvent<Person, String>> eventType = TableColumn.editCommitEvent();
            column.getOnEditCommit().handle(new TableColumn.CellEditEvent<Person, String>(
                    tableView, new TablePosition<Person, String>(tableView, 0, column), (EventType) eventType, bigValue));
            tableColumnHeader.resizeCol();
            LOG.info("width/resized: " + width + "/" + column.getWidth());

        });
        
        Button bigger = new Button("bigger with data");
        bigger.setOnAction(e -> {
            largeContent();
        });
        
        Button smaller = new Button("smaller with data");
        smaller.setOnAction(e -> {
            smallContent();
            
            
        });
        
        Button reset =  new Button("reset initial");
        reset.setOnAction(e -> {
            reset();

        });
//        BorderPane content = new BorderPane(tableView);
//        content.setBottom(new HBox(10, initial, biggerEdit, bigger, smaller, reset));
        return tableView;
    }


    /**
     * 
     */
    protected void largeContent() {
        double width = column.getWidth();
        tableView.getItems().get(0).setFirstName(bigValue);
        tableColumnHeader.resizeCol();
        LOG.info("width/resized: " + width + "/" + column.getWidth() + " pref: " + column.getPrefWidth());
    }


    /**
     * 
     */
    protected void smallContent() {
        double width = column.getWidth();
        tableView.getItems().stream().forEach(p -> p.setFirstName("small"));
        tableColumnHeader.resizeCol();
        LOG.info("width/resized: " + width + "/" + column.getWidth() + " pref: " + column.getPrefWidth());
    }


    /**
     * 
     */
    protected void reset() {
        double width = column.getWidth();
        tableView.getItems().get(0).setFirstName(initialFirst);
        tableColumnHeader.resizeCol();
        LOG.info("width/resized: " + width + "/" + column.getWidth() + " pref: " + column.getPrefWidth());
    }

    
    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent(), 400, 500);
        scene.getAccelerators().put(KeyCombination.keyCombination("F1"), () -> smallContent());
        scene.getAccelerators().put(KeyCombination.keyCombination("F3"), () -> reset());
        stage.setScene(scene);
        stage.show();
        stage.setX(50);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(TableAutoSizeSam.class.getName());
    private TableView<Person> tableView;
    private TableColumn<Person, String> column;
    private ObservableList<Person> model;
    private String initialFirst;
    private String bigValue;

}
