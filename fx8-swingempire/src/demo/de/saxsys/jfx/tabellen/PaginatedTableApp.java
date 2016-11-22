/*
 * Created on 06.09.2016
 *
 */
package de.saxsys.jfx.tabellen;


import de.swingempire.fx.util.FXUtils;
//import com.jyloo.syntheticafx.RootPane;
//import com.jyloo.syntheticafx.SyntheticaFXModena;
//import com.jyloo.syntheticafx.Utils;
//
//import de.swingempire.fx.utils.FXUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Jeanette Winzenburg, Berlin
 */
public class PaginatedTableApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // installs license
//        Utils.init();
//        new SyntheticaFXModena().init();
//        primaryStage.setScene(new Scene(new RootPane(primaryStage, getContent()), 1200, 400));
        primaryStage.setScene(new Scene(getContent(), 800, 400));
        primaryStage.setTitle(FXUtils.version());
        primaryStage.show();

    }

    /**
     * @return
     */
    private Parent getContent() {
        TownService ts = new TownService();
        PersonService ps = new PersonService(ts);
        PersonTable table = new PersonTable(ps, ts);
        BorderPane pane = new BorderPane(table.getPagination());
        return pane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
