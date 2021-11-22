/*
 * Created 02.11.2021
 */

package de.swingempire.fx.scene.control.table;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * https://stackoverflow.com/q/69784955/203657
 * setting pref of row to values < somevalue (24?) looses rows at bottom
 */
public class TableRowHeightDependOnItem extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static class MyTableRowSkin extends TableRowSkin<Integer> {

        /**
         * @param control
         */
        public MyTableRowSkin(TableRow<Integer> control) {
            super(control);
            control.setMaxHeight(Region.USE_PREF_SIZE);
//            control.setMinHeight(Region.USE_PREF_SIZE);
        }

        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset,
                double bottomInset, double leftInset) {
            double pref = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
            double computed = computePrefHeight(getSkinnable().getIndex());
            return computed > -1 ? computed : pref;
        }

        private double computePrefHeight(int i) {
            double pref = -1;
            TableView<Integer> table = getSkinnable().getTableView();
            if (table != null) {
                int size = table.getItems().size();
                if (i >= 0 && i < size) {
                    Integer item = table.getItems().get(i);
                    if (item < 5) {
                        pref = 16;
                    }
                }
            }
            return pref;
        }




    }
    @Override public void start(Stage stage) throws Exception {
        int numCols = 20;
        int numRows = 10;

        ObservableList<Integer> rows = FXCollections.observableList(IntStream
                .rangeClosed(1, numRows).boxed().collect(Collectors.toList()));
        TableView<Integer> tv = new TableView<>(rows);
        for (int i = 0; i < numCols; i++) {
            TableColumn<Integer, String> col = new TableColumn<>();
            int ii = i;
            col.setCellValueFactory(cdf -> new SimpleStringProperty(String.valueOf(ii)));
            tv.getColumns().add(col);
        }
        tv.setRowFactory(tview -> new TableRow<Integer>() {





            @Override
            protected Skin<?> createDefaultSkin() {
                return new MyTableRowSkin(this);
            }

            @Override
            public void updateIndex(int i) {
//                updatePrefHeight(i);
                super.updateIndex(i);

            }

            private void updatePrefHeight(int i) {
                double pref = -1;
                if (getTableView() != null) {
                    int size = getTableView().getItems().size();
                    if (i >= 0 && i < size) {
                        Integer item = getTableView().getItems().get(i);
                        if (item < 5) {
                            pref = 16;
                        }
                    }
                }
                setPrefHeight(pref);
            }

            @Override
            protected void layoutChildren() {
//                updatePrefHeight(getIndex());
                super.layoutChildren();
            }



//            @Override public void updateItem(Integer item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item != null && item < 10) {
//                    setPrefHeight(15);
//                } else {
//                    setPrefHeight(-1);
//                }
//            }
        });

        ListView<Integer> list = new ListView<>(tv.getItems());
        list.setCellFactory(l -> {
            ListCell<Integer> cell = new ListCell<>() {

                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    double pref = -1;
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        if (item < 5) {
                            pref = 10;
                        }
                        setText("" + item);
                    }
                    setPrefHeight(pref);
                }


            };
            return cell;
        });
        Scene scene = new Scene(tv, 900, 600);
        stage.setScene(scene);
        stage.show();
    }
}

