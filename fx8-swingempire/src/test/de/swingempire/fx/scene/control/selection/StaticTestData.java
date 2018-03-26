/*
 * Created on 13.03.2015
 *
 */
package de.swingempire.fx.scene.control.selection;

import java.util.logging.Logger;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.codeaffine.test.ConditionalIgnoreRule;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import de.swingempire.fx.util.FXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

/**
 * Trying to understand the weirdness introduced by static test data, not
 * conclusive, though, there might be an issue with listeners (or not)
 * 
 * Simply DONT!
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@RunWith(JUnit4.class)
//@RunWith(Parameterized.class)
public class StaticTestData {
    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();

    @Rule
    public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private static ObservableList<String> defaultData = FXCollections
            .<String> observableArrayList();

    private static ObservableList<String> classData = FXCollections
            .<String> observableArrayList();
    
    private ObservableList<String> instanceData = FXCollections
            .<String> observableArrayList();

    ListView listView;
    MultipleSelectionModel sm;

    // flag to use static test data
    private boolean useStatic = true;
    static int runCount;
    
    @Before
    public void setUp() {
        runCount++;
        // ListView init
      defaultData.setAll("Row 4", "Row 6",
              "Row 7", "Row 8", "Row 9", "Row 10", "Row 11", "Row 12", "Row 13",
              "Row 14", "Row 15", "Row 16", "Row 17", "Row 18", "Row 19");
      if (useStatic) {
          classData.setAll(defaultData);
          listView = new ListView(classData);
      } else {
          instanceData = FXCollections.observableArrayList();
          instanceData.setAll(defaultData);
          listView = new ListView(instanceData);
      }
//      listView.setSelectionModel(new SimpleListSelectionModel<>(listView));
      sm = listView.getSelectionModel();
      sm.setSelectionMode(SelectionMode.MULTIPLE);
    }
    @Test
    public void testSomethingElse() {
        int[] indices = new int[]{1, 4, 2};
        sm.selectIndices(indices[0], indices);
        assertEquals(indices[indices.length - 1], sm.getSelectedIndex());
        for (int i : indices) {
            assertTrue(sm.isSelected(i));
        }
        assertEquals(indices.length, sm.getSelectedIndices().size());
    }
    
    @Test
    public void testListenerOnItems() {
        int[] indices = new int[]{2, 5, 7};
        ListChangeListener l = c -> {
            LOG.info("hey, got a change, useStatic? " + useStatic + runCount + c.getList());
            FXUtils.prettyPrint(c);
        };
        sm.getSelectedIndices().addListener(l);
        sm.selectIndices(indices[0], indices);
        
    }
    @Test
    public void testSomething() {
        int index = 3;
        sm.select(index);
        assertEquals(getDataItem(index), sm.getSelectedItem());
    }


    protected String getDataItem(int index) {
        return useStatic ? classData.get(index) : instanceData.get(index);
    }
    
//    public StaticTestData(boolean useStatic) {
//        this.useStatic = useStatic;
//    }
    
//    @Parameterized.Parameters
//    public static Collection selectionModes() {
//        return Arrays.asList(new Object[][] { { false }, { true } });
//    }

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(StaticTestData.class
            .getName());
}
