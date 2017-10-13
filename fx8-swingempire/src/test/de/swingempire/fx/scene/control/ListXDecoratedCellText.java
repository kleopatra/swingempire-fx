/*
 * Created on 11.10.2017
 *
 */
package de.swingempire.fx.scene.control;

import de.swingempire.fx.demobean.Person;
import de.swingempire.fx.scene.control.cell.DebugListCellTest;
import de.swingempire.fx.util.EditableControl;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

/**
 * Test ListXDecoratedCell
 * 
 * Experimenting whether/how far we can re-use the normal test.
 * Failing tests for all cell.commit
 * 
 * @author Jeanette Winzenburg, Berlin
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ListXDecoratedCellText extends DebugListCellTest {

    
    /**
     * failing because commitEdit(T) is unsupported.
     * Think: how to handle?
     */
    @Override
    public void testEditCommitNotEditing() {
        super.testEditCommitNotEditing();
    }

    /**
     * failing because commitEdit(T) is unsupported.
     * Think: how to handle?
     */
    @Override
    public void testNullControlOnCommitEdit() {
        super.testNullControlOnCommitEdit();
    }

    /**
     * failing because commitEdit(T) is unsupported.
     * Think: how to handle?
     */
    @Override
    public void testNoCommitNotEditing() {
        super.testNoCommitNotEditing();
    }

    /**
     * failing because commitEdit(T) is unsupported.
     * Think: how to handle?
     */
    @Override
    public void testEditCommitOnCell() {
        super.testEditCommitOnCell();
    }

    /**
     * failing because commitEdit(T) is unsupported.
     * Think: how to handle?
     */
    @Override
    public void testEditCommitOnCellResetEditingIndex() {
        super.testEditCommitOnCellResetEditingIndex();
    }

    /**
     * Overridden to create and return an EditableControl wrapping a
     * ListView of type ListXView.
     */
    @Override
    protected EditableControl createEditableControl() {
        EListXView control = new EListXView(Person.persons());
        control.setCellValueFactory(p -> ((Person) p).lastNameProperty());

        control.setEditable(true);
        control.setCellFactory(createTextFieldCellFactory());
        return control;
    }
    
    public static class EListXView extends ListXView
            implements EditableControl<ListView, ListCell> {

        @Override
        public ListView getControl() {
            return this;
        }

        public EListXView() {
            super();
        }

        public EListXView(ObservableList arg0) {
            super(arg0);
        }

        @Override
        public EventType editAny() {
            return editAnyEvent();
        }

        @Override
        public EventType editCommit() {
            return editCommitEvent();
        }

        @Override
        public EventType editCancel() {
            return editCancelEvent();
        }

        @Override
        public EventType editStart() {
            return editStartEvent();
        }

        @Override
        public <T extends Event> void addEditEventHandler(EventType<T> type,
                EventHandler<? super T> handler) {
            addEventHandler(type, handler);
        }

    }


    @SuppressWarnings("rawtypes")
    @Override
    protected Callback<ListView, ListCell> createTextFieldCellFactory() {
        return e -> new TextFieldListXDecoratedCell<>();
//                (Callback<ListView, ListCell>)DebugTextFieldListCell.forListView();
    }

}
