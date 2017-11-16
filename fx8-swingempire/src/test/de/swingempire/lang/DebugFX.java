/*
 * Created on 15.11.2017
 *
 */
package de.swingempire.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.application.Application;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class DebugFX extends Application {

    @Override
    public void start(Stage arg0) throws Exception {
        ArrayList<Locale> list = new ArrayList<>();
        Locale mine = Locale.getDefault();
        list.add(mine);
        TableView<Locale> table = new TableView<>();

    }

    public static void main(String[] args) {
        launch();
    }
}
