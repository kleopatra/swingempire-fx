/*
 * Created on 01.08.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.FXUtils.*;
import static javafx.collections.FXCollections.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.ListChangeReport;
import de.swingempire.fx.util.StageLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class TabPaneTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    private TabPane tabPane;
    private ListChangeReport report;
    private static int TAB_COUNT = 6;
    
    
    @Test
    public void testTabHeaderReversed() {
        new StageLoader(tabPane);
        reverse(tabPane.getTabs());
        assertTabLabel(0);
        assertTabHeader(0);
    }
    
    @Test
    public void testTabHeader() {
        new StageLoader(tabPane);
        assertTabHeader(0);
        assertTabLabel(0);
    }
    
    protected void assertTabLabel(int index) {
        Tab tab = getTab(index);
        Node header = getTabHeader(index);
        Label label = (Label) header.lookup(".tab-label");
        assertEquals("text on header: " + index, tab.getText(), label.getText());
        assertEquals("graphic on header: " + index, tab.getGraphic(), label.getGraphic());
    }
    
    protected void assertTabHeader(int index) {
        Tab tab = getTab(index);
        Node header = getTabHeader(index);
        assertEquals("header for tab at " + index, tab, header.getProperties().get(Tab.class));
    }
    
    protected Node getFirstHeader() {
        return getTabHeader(0);
    }
    
    protected Node getLastHeader() {
        return getTabHeader(TAB_COUNT);
    }
    
    protected Node getTabHeader(int index) {
        Parent headersRegion = (Parent) tabPane.lookup(".headers-region");
        assertEquals(TAB_COUNT, headersRegion.getChildrenUnmodifiable().size());
        Node firstNode = headersRegion.getChildrenUnmodifiable().get(index);
        return firstNode;
    }
    
    
    /**
     * sanity: reverse actually does reverse the tabs and fires a single replaced
     */
    @Test
    public void testReverseTabNotification() {
        Tab first = getFirstTab();
        Tab last = getLastTab();
        reverse(tabPane.getTabs());
        assertTrue("reverse produces single replaced notification", wasSingleReplaced(report.getLastChange()));
        assertSame("first is old last", last, getFirstTab());
        assertSame("last is old first", first, getLastTab());
    }


    protected Tab getLastTab() {
        return getTab(TAB_COUNT - 1);
    }

    protected Tab getFirstTab() {
        return getTab(0);
    }

    protected Tab getTab(int index) {
        return tabPane.getTabs().get(index);
    }
    
    
    
    @Test
    public void testBasicSetup() {
        assertEquals("sanity tabCount", TAB_COUNT, tabPane.getTabs().size());
    }
    
    @Before
    public void setup() {
        tabPane = new TabPane();
        tabPane.getTabs().setAll(createTabs(TAB_COUNT));
        report = new ListChangeReport(tabPane.getTabs());
    }
    
    private Tab createTab(int i) {
        Tab tab = new Tab("" + i);
        return tab;
    }
    
    private List<Tab> createTabs(int size) {
        List<Tab> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(createTab(i));
        }
        return list;
    }


}
