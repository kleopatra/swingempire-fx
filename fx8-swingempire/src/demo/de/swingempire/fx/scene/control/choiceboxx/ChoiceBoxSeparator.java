/*
 * Created on 31.08.2014
 *
 */
package de.swingempire.fx.scene.control.choiceboxx;

import java.util.logging.Logger;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.selection.AbstractChoiceInterfaceSelectionIssues.ChoiceControl;
import de.swingempire.fx.scene.control.selection.ChoiceSelectionIssues.ChoiceCoreControl;
import de.swingempire.fx.scene.control.selection.ChoiceXSelectionIssues.ChoiceXControl;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Type-safe separator.
 * http://stackoverflow.com/q/25914924/203657
 * 
 * Supported by ChoiceBoxX 
 * - SeparatorMarker: but not much nicer as the answer at SO.
 * - separatorsList property
 * 
 * PENDING JW: remove marker support, isn't worth any trouble
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class ChoiceBoxSeparator extends Application {

    ObservableList<String> items = FXCollections.observableArrayList(
            "5-item", "4-item", "3-item", "2-item", "1-item");

    private final ObservableList persons =
            FXCollections.observableArrayList(
                    new Person("Jacob", "Smith", "jacob.smith@example.com"),
                    // core support is ugly: prevents type-safe data
                    new Separator(),
                    new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                    new Person("Ethan", "Williams", "ethan.williams@example.com"),
                    // first try of extended support isn't much better, requires dummy extension
                    // and support in selectionModel (easy) and skin (re-write needed)
//                    new SeparatorPerson(),
                    new Person("Emma", "Jones", "emma.jones@example.com"),
                    new Person("Michael", "Brown", "michael.brown@example.com"));
    /**
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Parent getContent() {
        StringConverter converter = new StringConverter() {

            @Override
            public String toString(Object object) {
                if (object instanceof Person) {
                    return ((Person) object).getEmail();
                }
                return object != null ? object.toString() : "";
            }

            @Override
            public Object fromString(String string) {
                throw new UnsupportedOperationException("invers conversion not supported");
            }
            
        };
        
        ChoiceCoreControl core = new ChoiceCoreControl(persons);
        core.setConverter(converter);
        Parent coreButtons = createButtonPane(core);
        BorderPane coreChoice = new BorderPane(core);
        coreChoice.setBottom(coreButtons);
        
        ChoiceXControl box = new ChoiceXControl(persons);
        box.setConverter(converter);
        box.addSeparator(3);
        Parent buttons = createButtonPane(box);
        
        BorderPane xChoice = new BorderPane(box);
        xChoice.setBottom(buttons);
        
        Pane pane = new HBox(coreChoice, xChoice);
        return pane;
    }

    
    private static class SeparatorPerson extends Person implements SeparatorMarker {

        /**
         * @param fName
         * @param lName
         * @param email
         */
        public SeparatorPerson() {
            super("empty", "empty", "empty");
        }
        
    }
    protected Parent createButtonPane(ChoiceControl box) {
        // variant: initial uncontained value 
        //box.setValue("initial uncontained");
        // variant: initial selection
        //box.setValue(items.get(0));
        Button setSelectedItemUncontained = new Button("Set selectedItem to uncontained");
        setSelectedItemUncontained.setOnAction(e -> {
            SelectionModel model = getSelectionModel(box);
            model.select("myDummySelectedItem");
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Button setValue = new Button("Set value to uncontained");
        setValue.setOnAction(e -> {
            SelectionModel model = getSelectionModel(box);
            box.setValue("myDummyValue");
            LOG.info("selected/item/value" + model.getSelectedIndex() 
                    + "/" + model.getSelectedItem() + "/" + box.getValue());
        });
        Parent buttons = new HBox(setSelectedItemUncontained, setValue);
        return buttons;
    }

    // PENDING JW: hacking around typing issues of test interface
    protected SelectionModel getSelectionModel(ChoiceControl box) {
        return (box instanceof ChoiceBox) ? ((ChoiceBox) box).getSelectionModel() 
                : ((ChoiceBoxX) box).getSelectionModel();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene(getContent());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
    
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(ChoiceBoxSeparator.class
            .getName());
}
