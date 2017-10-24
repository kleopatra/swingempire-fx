    /*
     * Created on 11.08.2017
     *
     */
    package de.swingempire.fx.scene.control.cell;
    
    //import java.lang.System.Logger;
    import java.util.Locale;
    import java.util.logging.Logger;
    
    import de.swingempire.fx.util.FXUtils;
    import javafx.application.Application;
    import javafx.beans.Observable;
    import javafx.collections.FXCollections;
    import javafx.event.EventTarget;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.TableColumn;
    import javafx.scene.control.TableColumnBase;
    import javafx.scene.control.TableView;
    import javafx.scene.control.TextField;
    import javafx.scene.control.cell.PropertyValueFactory;
    import javafx.scene.control.skin.TableColumnHeader;
    import javafx.scene.control.skin.TableHeaderRow;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.BorderPane;
    import javafx.stage.Stage;
    
    /**
     * Format/style parts of a TableCell
     * https://stackoverflow.com/q/46880705/203657
     */
    public class TableHeaderWithInput extends Application {
        TableView<Locale> table;
        
        protected void installHeaderHandler(Observable s) {
            table.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (e.isPrimaryButtonDown() &&  e.getClickCount() > 1) {
                    EventTarget target = e.getTarget();
                    TableColumnBase<?, ?> column = null;
                    while (target instanceof Node) {
                        target = ((Node) target).getParent();
                        // beware: package of TableColumnHeader is version specific
                        if (target instanceof TableColumnHeader) {
                            column = ((TableColumnHeader) target).getTableColumn();
                            if (column != null) break;
                        }
                    }
                    if (column != null) {
                        TableColumnBase<?,?> tableColumn = column;
                        TextField textField = new TextField(column.getText());
                        textField.setMaxWidth(column.getWidth());
                        textField.setOnAction(a -> {
                            tableColumn.setText(textField.getText());
                            tableColumn.setGraphic(null);
                        });
                        textField.focusedProperty().addListener((src, ov, nv) -> {
                            if (!nv) tableColumn.setGraphic(null);
                        });
                        column.setGraphic(textField);
                        textField.requestFocus();
                    }
                    e.consume();
                }
            });
        }
        
        private Parent getContent() {
            table = new TableView<>(FXCollections.observableArrayList(
                    Locale.getAvailableLocales()));
            table.setTableMenuButtonVisible(true);
            // quick hack: don't let sorting interfere ...
            table.setSortPolicy(e -> {return false;});
            TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
            countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
            TableColumn<Locale, String> language = new TableColumn<>("Language");
            language.setCellValueFactory(new PropertyValueFactory<>("language"));
            table.getColumns().addAll(countryCode, language);
            table.skinProperty().addListener(this::installHeaderHandler);
            BorderPane pane = new BorderPane(table);
            return pane;
        }
    
        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.setScene(new Scene(getContent(), 800, 400));
            primaryStage.setTitle(FXUtils.version());
            primaryStage.show();
        }
        
        public static void main(String[] args) {
            launch(args);
        }
    
        @SuppressWarnings("unused")
        private static final Logger LOG = Logger
                .getLogger(TableHeaderWithInput.class.getName());
    }
