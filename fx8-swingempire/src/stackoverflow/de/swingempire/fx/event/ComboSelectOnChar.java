/*
 * Created on 05.08.2019
 *
 */
package de.swingempire.fx.event;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Selecting doesn't scroll the selected item into the visible area of the 
 * dropdown
 * https://stackoverflow.com/q/57338431/203657
 * 
 * Here the question is: selecting by key. A possible solution is to grab
 * the list in the popup and invoke its scrollTo(selected). Drawback: as always
 * there is no fine-grained control of target position, it's always scrolled
 * to the top.
 * 
 * Not even on opening, Bug? not-a-regression, same in fx8/9/11 
 * 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ComboSelectOnChar extends Application {

    ComboBox<String> cb;
    //Entered random options
    private final ObservableList<String> options = FXCollections.observableArrayList(
            "Aab",
            "Aer",
            "Aeq",
            "Arx",
            "Byad",
            "Csca",
            "Csee",
            "Cfefe",
            "Cead",
            "Defea",
            "Dqeqe",
            "Fefaf",
            "Gert",
            "Wqad is a longish name or what?",
            "Xsad",
            "Zzz"
            
            );
    
    private Parent createContent() {
        
        ChoiceBox<String> choice = new ChoiceBox<>(options);
        // Bug?: selected item not scrolled to visible on showing popup
        for (int i = 0; i < 20; i++) {
            choice.getItems().add(0, "item " + i);
        }
        
        cb = new ComboBox<>(options);
//        selectOptionOnKey();
        // Bug?: selected item not scrolled to visible on showing popup
        choice.getSelectionModel().select("Gert");
        cb.getSelectionModel().select("Gert");
        BorderPane content = new BorderPane(cb);
        content.setTop(choice);
        return content;
    }

    public void selectOptionOnKey() {
        cb.setOnKeyPressed(e -> {
            KeyCode keyCode = e.getCode();

            if (keyCode.isLetterKey()) {
                char key = keyCode.getName().charAt(0);

                SingleSelectionModel<String> cbSelectionModel = cb.getSelectionModel();

                cbSelectionModel.select(0);

                for (int i = 0; i < options.size(); i++) {
                    if(cbSelectionModel.getSelectedItem().charAt(0) == key) {
                        // option which starts with the input letter found -> select it
                        cbSelectionModel.select(i);
                        /* Before exiting the function it would be nice if after the selection,
                           the combo box would auto slide/jump to the option which is selected.
                           I don't know how to do that. */
                        
                        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
                        ListView<?> list = (ListView<?>) skin.getPopupContent();
                        list.scrollTo(i);
                        break;
                    }
                    else {
                        cbSelectionModel.selectNext();
                        ComboBoxListViewSkin<?> skin = (ComboBoxListViewSkin<?>) cb.getSkin();
                        ListView<?> list = (ListView<?>) skin.getPopupContent();
                        list.scrollTo(i);
                    }     
                }   
            }
        });
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
            .getLogger(ComboSelectOnChar.class.getName());

}
