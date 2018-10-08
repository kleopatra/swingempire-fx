/*
 * Created on 08.10.2018
 *
 */
package de.swingempire.fx.scene.control.text;

import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * SO: bind sentence to size of list, grammatically correct.
 * @author Jeanette Winzenburg, Berlin
 */
public class ListLocales extends Application {

    private ObservableList<Locale> data = FXCollections.observableArrayList(Locale.getAvailableLocales());
    
    private Parent createContent() {
        ListView<Locale> list = new ListView<>(data);
        list.setCellFactory(c -> {
            ListCell<Locale> cell = new ListCell<>() {

                @Override
                protected void updateItem(Locale item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("");
                    } else {
                        setText(item.getDisplayLanguage());
                    }
                }
                
            };
            return cell;
        });
        Label label = new Label("unbound");
        ComboBox<Locale> combo = new ComboBox<>(
                FXCollections.observableArrayList(Locale.ENGLISH, Locale.GERMAN));
        combo.valueProperty().addListener((src, ov, nv) -> updateBinding(label, nv));
        combo.getSelectionModel().selectFirst();
        
        Button reset = new Button("Reset");
        reset.setOnAction(e -> data.setAll(Locale.getAvailableLocales()));
        Button clear = new Button("Clear");
        clear.setOnAction(e -> data.clear());
        Button retain = new Button("Retain");
        retain.setOnAction(e -> data.retainAll(combo.getItems()));
        BorderPane content = new BorderPane(list);
        content.setTop(new HBox(10, label, combo));
        content.setBottom(new HBox(10, reset, clear, retain));
        return content;
    }

    protected void updateBinding(Label label, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        ResourceBundle messages =       
                ResourceBundle.getBundle(getClass().getPackageName() +".messagebundle", locale);
        
        MessageFormat form = new MessageFormat(messages.getString("choicetemplate"));
        double[] limits = {0, 1, 2};
        String[] parts = new String[]{messages.getString("noitems"), 
                messages.getString("oneitem"), messages.getString("moreitems")};
        ChoiceFormat listForm = new ChoiceFormat(limits, parts);
        form.setFormatByArgumentIndex(0, listForm);
        
        StringBinding binding = new StringBinding() {
            {
               super.bind(data) ;
            }
            @Override
            protected String computeValue() {
                return form.format(new Object[] {data.size()});
            }
            
        };
        label.textProperty().bind(binding);

    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ListLocales.class.getName());

}
