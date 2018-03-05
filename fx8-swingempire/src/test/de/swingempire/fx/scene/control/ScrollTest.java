/*
 * Created on 05.03.2018
 *
 */
package de.swingempire.fx.scene.control;

import java.util.Locale;
import java.util.logging.Logger;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static de.swingempire.fx.util.VirtualFlowTestUtils.*;
import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.StageLoader;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.VirtualFlow;


@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
public class ScrollTest {

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    
    @Test
    public void testScrollToSetup() {
        TableView<Locale> table = new TableView<>();
        TableColumn<Locale, String> countryCode = new TableColumn<>("CountryCode");
        countryCode.setCellValueFactory(new PropertyValueFactory<>("country"));
        TableColumn<Locale, String> language = new TableColumn<>("Language");
        language.setCellValueFactory(new PropertyValueFactory<>("language"));
        TableColumn<Locale, String> variant = new TableColumn<>("Variant");
        variant.setCellValueFactory(new PropertyValueFactory<>("variant"));
        table.getColumns().addAll(countryCode, language, variant);
        
        new StageLoader(table);
        
        int index = 100;
        table.scrollTo(index);
        VirtualFlow flow = getVirtualFlow(table);
        IndexedCell cell = getCell(table, index);
        LOG.info("" + cell );
        table.scrollTo(index - 10);
        IndexedCell same = getCell(table, index);
        assertSame(cell, same);
        LOG.info("" +cell );
    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(ScrollTest.class.getName());
}
