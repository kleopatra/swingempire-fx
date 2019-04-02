/*
 * Created on 02.04.2019
 *
 */
package de.swingempire.fx.scene.control.scroll;

import java.text.Format;
import java.time.LocalDate;
import java.time.Month;
import java.util.Set;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * https://stackoverflow.com/q/55461094/203657
 * hbar not sync'ed initially
 * 
 * to reproduce (without the trick)
 * - run
 * - scroll lower table
 * - expected: upper table should scroll in sync
 * - actual: no scrolling
 * - add data to upper by clicking the button, remove data by clicking button
 * - scroll lower table
 * - expected and actual: upper table scrolled in sync
 * 
 * dirty trick: 
 * after wiring scrollBars, add/remove data in upper - works if wrapping
 * the remove into runLater
 * drawback: short but perceptible flicker - doing the add before showing works but
 * also flickers
 *  
 * digging: 
 * - VirtualFlow is not visible if table has no data initially, but isVisible
 * if data is added/removed 
 */
public class SummaryTableDemo extends Application
{

    // random hacking ...
    public static class FakeNotEmptyTableSkin<T> extends TableViewSkin<T> {

        boolean fakeNotEmpty = false;
        boolean initial = true;
        /**
         * @param control
         */
        public FakeNotEmptyTableSkin(TableView<T> control) {
            super(control);
        }

        @Override
        protected int getItemCount() {
            int itemCount = super.getItemCount();
//            fakeNotEmpty = false;
//            if (itemCount == 0) {
//                itemCount = 1;
//                fakeNotEmpty = true;
//                
//            }
//            if (fakeNotEmpty) {
//                Node placeHolder = getSkinnable().lookup("placeholder");
//                if (placeHolder != null) {
//                    placeHolder.setVisible(true);
//                    
//                }
//            }
////            if (initial) {
////                initial = false;
////            }
            return itemCount;
        }

        @Override
        protected void updateItemCount() {
            super.updateItemCount();
            if (initial) {
                int count = getItemCount();
                if (count == 0) {
                    fakeNotEmpty = true;
                    getVirtualFlow().setCellCount(1);
                    getVirtualFlow().setVisible(true);
                  Node placeHolder = getSkinnable().lookup("placeholder");
                  if (placeHolder != null) {
                      placeHolder.setVisible(false);
                      
                  }
                }
            }
        }
        
        
        
    }

    private TableView<Data> mainTable = new TableView<>() {

        @Override
        protected Skin<?> createDefaultSkin() {
            // TODO Auto-generated method stub
            return new FakeNotEmptyTableSkin(this);
        }
        
    };
    private TableView<SumData> sumTable = new TableView<>();

    private final ObservableList<Data> data
            = FXCollections.observableArrayList();

    // TODO: calculate values
    private final ObservableList<SumData> sumData
            = FXCollections.observableArrayList(
                    new SumData("Sum", 0.0, 0.0, 0.0),
                    new SumData("Min", 0.0, 0.0, 0.0),
                    new SumData("Max", 0.0, 0.0, 0.0)
            );

    final HBox hb = new HBox();

    public static void main(String[] args)
    {
        launch(args);
    }

    
    @Override
    public void start(Stage stage)
    {

//        Group root = new Group();
        VBox root = new VBox();
        Scene scene = new Scene(root);

        // load css
        //  scene.getStylesheets().addAll(getClass().getResource("application.css").toExternalForm());
        stage.setTitle("Table View Sample");
        stage.setWidth(250);
        stage.setHeight(550);

        // setup table columns
        setupMainTableColumns();
        setupSumTableColumns();

        // fill tables with data
        mainTable.setItems(data);
        sumTable.setItems(sumData);

        // set dimensions
        sumTable.setPrefHeight(90);

        // bind/sync tables
        for (int i = 0; i < mainTable.getColumns().size(); i++)
        {

            TableColumn<Data, ?> mainColumn = mainTable.getColumns().get(i);
            TableColumn<SumData, ?> sumColumn = sumTable.getColumns().get(i);

            // sync column widths
            sumColumn.prefWidthProperty().bind(mainColumn.widthProperty());

            // sync visibility
            sumColumn.visibleProperty().bindBidirectional(mainColumn.visibleProperty());

        }

        // allow changing of column visibility
        //mainTable.setTableMenuButtonVisible(true);
        // hide header (variation of jewelsea's solution: http://stackoverflow.com/questions/12324464/how-to-javafx-hide-background-header-of-a-tableview)
        sumTable.getStyleClass().add("tableview-header-hidden");

        // hide horizontal scrollbar via styles
        //   sumTable.getStyleClass().add("sumtable");
        // create container
        BorderPane bp = new BorderPane();

        Button addButton = new Button("+");
        Button clearButton = new Button("X");

        addButton.setOnAction((ActionEvent c) ->
        {
            data.add(new Data(LocalDate.of(2015, Month.JANUARY, 11), 40.0, 50.0, 60.0));
        });
        clearButton.setOnAction((ActionEvent c) ->
        {
            data.clear();
        });

        HBox buttonBar = new HBox(clearButton, addButton);
        bp.setTop(buttonBar);
        bp.setCenter(mainTable);
        bp.setBottom(sumTable);

        // fit content
        bp.prefWidthProperty().bind(scene.widthProperty());
        bp.prefHeightProperty().bind(scene.heightProperty());

        root.getChildren().addAll(bp);

//        mainTable.refresh();
        stage.setScene(scene);
        stage.setX(0);
        stage.show();
//        root.setVisible(false);

        VirtualFlow flow = (VirtualFlow) mainTable.lookup(".virtual-flow");
        LOG.info("main size: " + mainTable.getWidth() + "/" + mainTable.getHeight());
        // doesn't help
//        flow.setVisible(true);
//        flow.requestLayout();
        // synchronize scrollbars (must happen after table was made visible)
        ScrollBar mainTableHorizontalScrollBar = findScrollBar(mainTable, Orientation.HORIZONTAL);
        // virtualFlow has no notion of scrollBarPolicy, so can't hide
        ScrollBar sumTableHorizontalScrollBar = findScrollBar(sumTable, Orientation.HORIZONTAL);
        mainTableHorizontalScrollBar.valueProperty().bindBidirectional(sumTableHorizontalScrollBar.valueProperty());
        // forcing max doesn't help
        // mainTableHorizontalScrollBar.setMax(sumTableHorizontalScrollBar.getMax());
        // setting visible doesn't help, plus it's not at the right place (no layout happened)
        // mainTableHorizontalScrollBar.setVisible(true);
        BooleanProperty bound = new SimpleBooleanProperty();
        
        mainTableHorizontalScrollBar.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("main hbar: " + nv );
        });
        
        sumTableHorizontalScrollBar.valueProperty().addListener((src, ov, nv) -> {
            LOG.info("sum hbar: " + nv + " max " + sumTableHorizontalScrollBar.getMax()
                  +  " main: " + mainTableHorizontalScrollBar.getValue() + " / " + mainTableHorizontalScrollBar.getMax());
        });
        
        TableViewSkin skin;
//        addButton.fire();
//        Platform.runLater(( ) -> {
//            clearButton.fire();
//            root.setVisible(true);
//        });
//
    }

    /**
     * Primary table column mapping.
     */
    private void setupMainTableColumns()
    {

        TableColumn<Data, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(120);
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Data, Double> value1Col = new TableColumn<>("Value 1");
        value1Col.setPrefWidth(90);
        value1Col.setCellValueFactory(new PropertyValueFactory<>("value1"));
        value1Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        TableColumn<Data, Double> value2Col = new TableColumn<>("Value 2");
        value2Col.setPrefWidth(90);
        value2Col.setCellValueFactory(new PropertyValueFactory<>("value2"));
        value2Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        TableColumn<Data, Double> value3Col = new TableColumn<>("Value 3");
        value3Col.setPrefWidth(90);
        value3Col.setCellValueFactory(new PropertyValueFactory<>("value3"));
        value3Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        mainTable.getColumns().addAll(dateCol, value1Col, value2Col, value3Col);

    }

    /**
     * Summary table column mapping.
     */
    private void setupSumTableColumns()
    {

        TableColumn<SumData, String> textCol = new TableColumn<>("Text");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));

        TableColumn<SumData, Double> value1Col = new TableColumn<>("Value 1");
        value1Col.setCellValueFactory(new PropertyValueFactory<>("value1"));
        value1Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        TableColumn<SumData, Double> value2Col = new TableColumn<>("Value 2");
        value2Col.setCellValueFactory(new PropertyValueFactory<>("value2"));
        value2Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        TableColumn<SumData, Double> value3Col = new TableColumn<>("Value 3");
        value3Col.setCellValueFactory(new PropertyValueFactory<>("value3"));
        value3Col.setCellFactory(new FormattedTableCellFactory<>(TextAlignment.RIGHT));

        sumTable.getColumns().addAll(textCol, value1Col, value2Col, value3Col);

    }

    /**
     * Find the horizontal scrollbar of the given table.
     *
     * @param table
     * @return
     */
    private ScrollBar findScrollBar(TableView<?> table, Orientation orientation)
    {

        // this would be the preferred solution, but it doesn't work. it always gives back the vertical scrollbar
        //      return (ScrollBar) table.lookup(".scroll-bar:horizontal");
        //      
        // => we have to search all scrollbars and return the one with the proper orientation
        Set<Node> set = table.lookupAll(".scroll-bar");
        for (Node node : set)
        {
            ScrollBar bar = (ScrollBar) node;
            if (bar.getOrientation() == orientation)
            {
                return bar;
            }
        }

        return null;

    }

    /**
     * Data for primary table rows.
     */
    public static class Data
    {

        private final ObjectProperty<LocalDate> date;
        private final SimpleDoubleProperty value1;
        private final SimpleDoubleProperty value2;
        private final SimpleDoubleProperty value3;

        public Data(LocalDate date, double value1, double value2, double value3)
        {

            this.date = new SimpleObjectProperty<LocalDate>(date);

            this.value1 = new SimpleDoubleProperty(value1);
            this.value2 = new SimpleDoubleProperty(value2);
            this.value3 = new SimpleDoubleProperty(value3);
        }

        public final ObjectProperty<LocalDate> dateProperty()
        {
            return this.date;
        }

        public final LocalDate getDate()
        {
            return this.dateProperty().get();
        }

        public final void setDate(final LocalDate date)
        {
            this.dateProperty().set(date);
        }

        public final SimpleDoubleProperty value1Property()
        {
            return this.value1;
        }

        public final double getValue1()
        {
            return this.value1Property().get();
        }

        public final void setValue1(final double value1)
        {
            this.value1Property().set(value1);
        }

        public final SimpleDoubleProperty value2Property()
        {
            return this.value2;
        }

        public final double getValue2()
        {
            return this.value2Property().get();
        }

        public final void setValue2(final double value2)
        {
            this.value2Property().set(value2);
        }

        public final SimpleDoubleProperty value3Property()
        {
            return this.value3;
        }

        public final double getValue3()
        {
            return this.value3Property().get();
        }

        public final void setValue3(final double value3)
        {
            this.value3Property().set(value3);
        }

    }

    /**
     * Data for summary table rows.
     */
    public static class SumData
    {

        private final SimpleStringProperty text;
        private final SimpleDoubleProperty value1;
        private final SimpleDoubleProperty value2;
        private final SimpleDoubleProperty value3;

        public SumData(String text, double value1, double value2, double value3)
        {

            this.text = new SimpleStringProperty(text);

            this.value1 = new SimpleDoubleProperty(value1);
            this.value2 = new SimpleDoubleProperty(value2);
            this.value3 = new SimpleDoubleProperty(value3);
        }

        public final SimpleStringProperty textProperty()
        {
            return this.text;
        }

        public final java.lang.String getText()
        {
            return this.textProperty().get();
        }

        public final void setText(final java.lang.String text)
        {
            this.textProperty().set(text);
        }

        public final SimpleDoubleProperty value1Property()
        {
            return this.value1;
        }

        public final double getValue1()
        {
            return this.value1Property().get();
        }

        public final void setValue1(final double value1)
        {
            this.value1Property().set(value1);
        }

        public final SimpleDoubleProperty value2Property()
        {
            return this.value2;
        }

        public final double getValue2()
        {
            return this.value2Property().get();
        }

        public final void setValue2(final double value2)
        {
            this.value2Property().set(value2);
        }

        public final SimpleDoubleProperty value3Property()
        {
            return this.value3;
        }

        public final double getValue3()
        {
            return this.value3Property().get();
        }

        public final void setValue3(final double value3)
        {
            this.value3Property().set(value3);
        }

    }

    /**
     * Formatter for table cells: allows you to align table cell values
     * left/right/center
     *
     * Example for alignment form
     * http://docs.oracle.com/javafx/2/fxml_get_started/fxml_tutorial_intermediate.htm
     *
     * @param <S>
     * @param <T>
     */
    public static class FormattedTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>>
    {

        private TextAlignment alignment = TextAlignment.LEFT;
        private Format format;

        public FormattedTableCellFactory()
        {
        }

        public FormattedTableCellFactory(TextAlignment alignment)
        {
            this.alignment = alignment;
        }

        public TextAlignment getAlignment()
        {
            return alignment;
        }

        public void setAlignment(TextAlignment alignment)
        {
            this.alignment = alignment;
        }

        public Format getFormat()
        {
            return format;
        }

        public void setFormat(Format format)
        {
            this.format = format;
        }

        @Override
        @SuppressWarnings("unchecked")
        public TableCell<S, T> call(TableColumn<S, T> p)
        {
            TableCell<S, T> cell = new TableCell<S, T>()
            {
                @Override
                public void updateItem(Object item, boolean empty)
                {
                    if (item == getItem())
                    {
                        return;
                    }
                    super.updateItem((T) item, empty);
                    if (item == null)
                    {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if (format != null)
                    {
                        super.setText(format.format(item));
                    } else if (item instanceof Node)
                    {
                        super.setText(null);
                        super.setGraphic((Node) item);
                    } else
                    {
                        super.setText(item.toString());
                        super.setGraphic(null);
                    }
                }
            };
            cell.setTextAlignment(alignment);
            switch (alignment)
            {
                case CENTER:
                    cell.setAlignment(Pos.CENTER);
                    break;
                case RIGHT:
                    cell.setAlignment(Pos.CENTER_RIGHT);
                    break;
                default:
                    cell.setAlignment(Pos.CENTER_LEFT);
                    break;
            }
            return cell;
        }
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SummaryTableDemo.class.getName());
}

