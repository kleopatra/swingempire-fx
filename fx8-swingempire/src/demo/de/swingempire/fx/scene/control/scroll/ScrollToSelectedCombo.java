package de.swingempire.fx.scene.control.scroll;

import java.util.logging.Logger;

import de.swingempire.testfx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Digging into
 * https://bugs.openjdk.java.net/browse/JDK-8129123
 * 
 * unrelated: flicker? here (fx11) as well as in fx14
 */
public class ScrollToSelectedCombo extends Application {
    
    private Parent createContent() {
        ComboBox<String> combo = new ComboBox<>(getItems());
        combo.getSelectionModel().select(50);
     
        // do it on showing
//        combo.setOnShowing(e -> {
//            int selected = combo.getSelectionModel().getSelectedIndex();
//            if (selected >= 0 && combo.getSkin() instanceof ComboBoxListViewSkin) {
//                ListView<?> list = (ListView<?>) ((ComboBoxListViewSkin<?>) combo.getSkin()).getPopupContent();
//                // keep selected in middle
//                list.scrollTo(Math.max(selected - 5, 0));
//            }
//        });
//        combo.setOnShown(e -> {
//            int selected = combo.getSelectionModel().getSelectedIndex();
//            if (selected >= 0 && combo.getSkin() instanceof ComboBoxListViewSkin) {
//                ListView<?> list = (ListView<?>) ((ComboBoxListViewSkin<?>) combo.getSkin()).getPopupContent();
//                // keep selected in middle
//                list.scrollTo(Math.max(selected - 5, 0));
//            }
//        });
        Button select = new Button("select 50");
        select.setOnAction(e -> {
            combo.getSelectionModel().select(50);
        });
        VBox content = new VBox(10, combo, select);
        return content;
    }

    protected ObservableList<String> getItems() {
        String[] values = new String[100];
        for (int i = 0; i < values.length; i++) {
            values[i] = String.valueOf(i);
        }

        ObservableList<String> items = FXCollections.observableArrayList(values);
        return items;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(createContent()));
        stage.setTitle(FXUtils.version());
        stage.show();
//        stage.setX(stage.getX() - 200);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollToSelectedCombo.class.getName());

}
