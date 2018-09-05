/*
 * Created on 05.09.2018
 *
 */
package fx.core.control.text;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import de.swingempire.fx.junit.JavaFXThreadingRule;
import fx.core.testsupport.TextInputControlShim;
import javafx.scene.control.TextField;

/**
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class ShimTest {

    @ClassRule
    public static TestRule classRule = new JavaFXThreadingRule();
    
    private TextField txtField;//Empty string
    private TextField dummyTxtField;//With string value

    @Test 
    public void checkContentNotNull() {
        // PENDING JW: replaced with reflective access
        assertNotNull(TextInputControlShim.getContentObject(txtField));
//        assertNotNull(TextInputControlShim.getContent(txtField));
    }

    @Test 
    public void checkCharsSameAsContent() {
        assertEquals(dummyTxtField.getCharacters().toString(), TextInputControlShim.getContent_get(dummyTxtField, 0, dummyTxtField.getLength()).toString());
    }

    @Test 
    public void checkTextSameAsContent() {
        assertEquals(dummyTxtField.getText(), TextInputControlShim.getContent_get(dummyTxtField, 0, dummyTxtField.getLength()));
    }


    @Before 
    public void setup() {
        txtField = new TextField();
        dummyTxtField = new TextField("dummy");
    }

}
