/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fx.collection;

import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.swingempire.fx.junit.JavaFXThreadingRule;

/**
 *
 * @author kleopatra
 */
@RunWith(JUnit4.class)
public class SimpleTest { //extends TestCase{
    @ClassRule public static JavaFXThreadingRule classfxRule = new JavaFXThreadingRule();
//    @Rule public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    protected ObservableList items;
    protected SelectionModel selectionModel;

    @Test
    public void testMe() {
        
        LOG.info("testMe " + Platform.isFxApplicationThread());
    }
   
    @BeforeClass
    public static void init() {
//        JFXPanel panel = new JFXPanel();
        
    }
    
    @Before
//    @Override
    public void setUp() {
       items = FXCollections.observableArrayList(
                "5-item", "4-item", "3-item", "2-item", "1-item");

        LOG.info("setup ...");
        ListView view = new ListView(items);
        selectionModel = view.getSelectionModel();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(SimpleTest.class
        .getName());
}
