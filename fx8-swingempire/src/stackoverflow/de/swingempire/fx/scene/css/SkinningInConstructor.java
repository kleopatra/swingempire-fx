/*
 * Created on 29.04.2018
 *
 */
package de.swingempire.fx.scene.css;

import java.util.logging.Logger;

import de.swingempire.fx.util.FXUtils;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class SkinningInConstructor extends Application {

    public static class MyCombo<T> extends ComboBox<T> {
        
        public MyCombo() {
            super();
            setSkin(new ComboBoxListViewSkin<>(this));
        }
    }
    private Parent createContent() {
        ObservableList<String> normalItems = FXCollections.observableArrayList("normal", "one", "item");
        ComboBox<String> normal = new ComboBox<String>(normalItems);
        
        ComboBox<String> withDefaultSkin = new ComboBox<String>() {

            @Override
            protected Skin<?> createDefaultSkin() {
                return new CustomComboSkin<>(this);
            }
            
        };
        withDefaultSkin.getItems().add(0, "default");
        
        ComboBox<String> withConstrSkin = new ComboBox<String> (){
            {
//                setSkin(new CustomComboSkin<>(this));
                // this is overridden by skin def in css
                setSkin(new ComboBoxListViewSkin<>(this));
            }
        };
        withConstrSkin.getItems().add(0, "constr");
        
        ComboBox<String> customInConstr = new MyCombo<>();
        customInConstr.getItems().add(0, "MyCombo");
        
        Button log = new Button("log skins");
        log.setOnAction(e -> {
            LOG.info("skins: normal/default/constr - \n" + normal.getSkin().getClass()
                    + "\n / " + withDefaultSkin.getSkin().getClass()
                    + "\n / " + withConstrSkin.getSkin().getClass()
                    + " \n/ " + customInConstr.getSkin().getClass()
                    );
        });
        VBox pane = new VBox(10, normal, withDefaultSkin, withConstrSkin, customInConstr, log);
        return pane;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContent());
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("comboskin.css").toExternalForm());

        stage.setTitle(FXUtils.version());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(SkinningInConstructor.class.getName());

}
