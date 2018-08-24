/*
 * Created on 24.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 
 * 
 * https://stackoverflow.com/q/51993138/203657
 * add/remove items on commit
 * 
 * aside:
 * action on editor never fired, action on combo fired on any value change
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboWithHistory extends Application {
    
    
    public class HistoryField<String> extends ComboBox<String> {
        public final static int DEFAULT_MAX_ENTRIES = 256;

        //Data members
        private int maxSize;
        
        // alias to itmes
        private final ObservableList<String> history;

       //Default constructor
        public HistoryField() {
            this(DEFAULT_MAX_ENTRIES, (String[]) null);
        }

        public HistoryField(int maxSize, String ... entries) {
//            super(FXCollections.observableList(List.of(entries))); //new LinkedList<>()));
            this.setEditable(true);

            this.maxSize = maxSize;
            this.history = this.getItems();

            getItems().setAll(entries);

//            //Populate list with entries (if any)
//            if (entries != null) {
//                for (int i = 0; ((i < entries.length) && (i < this.maxSize)); i++) {
//                    this.history.add(entries[i]);
//                }
//             }

//            setOnAction(e -> {
//                LOG.info("combo action? has editor action?" + getEditor().getOnAction());
//            });
//            
            TextField editor = getEditor();
//            TextFieldSkin s;
            
            TextFormatter formatter = new TextFormatter(TextFormatter.IDENTITY_STRING_CONVERTER);
            editor.setTextFormatter(formatter);
            editor.setOnAction(e -> LOG.info("action in text"));
            formatter.valueProperty().addListener((src, ov, nv) -> {
                LOG.info("formatter " + ov + nv);
            });
            getEditor().addEventFilter(KeyEvent.KEY_RELEASED,e -> {
                if (KeyCode.ENTER == e.getCode()) {
                    LOG.info("ENTER: " + e);
                }
            });
//            getEditor().setOnAction(e -> {
//                LOG.info("textField action?");
//            });
            
//            skinProperty().addListener((src, ov, nv) -> {
//                if (nv != null)
//                    LOG.info("installed?" +  nv + getEditor().getOnAction());
//                ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) nv;
//            });
//            

            // from Zephyr: working as expected
            valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {

                    // Check if value already exists in list
                    if (!this.history.contains(newValue)) {
                        this.history.add(0, newValue);

                        // If the max_size has been reached, remove the oldest item from the list
                        if (this.history.size() > maxSize) {
                            this.history.remove(history.size() - 1);
                        }

                        System.out.println(history);

                        // Clear the selection when new item is added
                        this.getSelectionModel().clearSelection();
                    }
                }
            });
     
            // weird listener implementation, usage error?
//            valueProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//                if ((oldValue == null) && (newValue != null)) {     
//                    LOG.info("adding: " + newValue);
//                    if (this.getSelectionModel().getSelectedIndex() < 0) {
//                        this.getItems().add(0, newValue);
//                        this.getSelectionModel().clearSelection();
//                    }
//                } else {
//                    LOG.info("selecting?" + newValue);
//                    //This throws IndexOutOfBoundsException
//                    this.getSelectionModel().clearSelection();
//                }
//            });
        }

        
    }


    private HistoryField<String> historyField;

    @Override
    public void start(Stage primaryStage) {        
        this.historyField = new HistoryField<>(5, "one", "two", "three");
        TextField field = new TextField("dummy");
        field.setOnAction(e -> {
            LOG.info("action from dummy");
        });
        
        BorderPane root = new BorderPane(historyField);
        root.setBottom(field);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setTitle("History Field Test");
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ComboWithHistory.class.getName());
}

