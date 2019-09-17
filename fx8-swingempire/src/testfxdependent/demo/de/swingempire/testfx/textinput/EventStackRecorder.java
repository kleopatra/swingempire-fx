/*
 * Created on 22.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.StackWalker.Option.*;

import javafx.event.Event;
import javafx.scene.input.KeyEvent;
//import javafx.scene.input.KeyEvent;

/**
 * Keeps state of received event and stacktrace.
 * <p>
 * PENDING: move into testsupport 
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class EventStackRecorder {

    
    private List<Event> events;
    private List<List<StackFrame>> stackFrames;
    private List<Object> handlers;
    private List<String> handlerTypes;
    
    private int depth;

    public EventStackRecorder() {
        this(1);
    }

    public EventStackRecorder(int depth) {
        events = new ArrayList<>();
        stackFrames = new ArrayList<>();
        handlers = new ArrayList<>();
        handlerTypes = new ArrayList<>();
        this.depth = depth;
    }

    /**
     * Returns the recorded size.
     * 
     * @return the count of recorded events
     * @throws IllegalStateException if # of events differs from # of
     *         stackFrames
     */
    public int getRecordSize() {
        checkRecordSize();
        return events.size();
    }

    private void checkRecordSize() {
        if (events.size() != stackFrames.size())
            throw new IllegalStateException("recorded events " + events.size()
                    + " must must be same size as recorded stackFrames "
                    + stackFrames.size());

    }

    /**
     * Collects the event and stackFrames with the depth of this record.
     * 
     * @param event the event passed into the caller
     */
    public void record(Event event) {
        events.add(event);
        List<StackFrame> stack = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)
                .walk(s -> s.skip(1 /* this */ + 1 /* caller */).limit(depth)
                        .collect(Collectors.toList()));
        stackFrames.add(stack);
    }

    public void recordHandler(Event event, Object handler) {
        record(event, handler, "handler");
    }
    
    public void recordFilter(Event event, Object handler) {
        record(event, handler, "filter");
    }
    
    private void record(Event event, Object handler, String handlerType) {
        record(event);
        handlers.add(handler);
        handlerTypes.add(handlerType);
    }
    /**
     * Applies the given consumer to each recorded list of StackFrames.
     * 
     * @param action the consumer to apply
     */
    public void forEach(Consumer<List<? super StackFrame>> action) {
        stackFrames.forEach(action::accept);
    }

    /**
     * Re-inventing asserts?
     * 
     * @param index
     * @param predicate
     */
    public void testEventSource(int index, Predicate<Object> predicate) {
        predicate.test(getEventSource(index));
    }

    public void forEvent(int index, Consumer<Event> eventConsumer) {
        eventConsumer.accept(events.get(index));
    }

    public void forFirstStackFrame(int index,
            Consumer<StackFrame> stackFrameConsumer) {
        stackFrameConsumer.accept(getFirstStackFrame(index));
    }

    public void logFirst(int index) {
        String log = "for index: " + index 
                + "\n event: " + events.get(index)
                + "\n stack: " + getFirstStackFrame(index);
        System.out.println(log);
    }
    
    
    public void log(int index) {
        Event event = events.get(index);
        String eventText = "source: " + event.getSource().getClass()
                + "\n     target: " + event.getTarget().getClass()
                + "\n     type: " + event.getEventType()
                + " consumed: " + event.isConsumed()
        ;        
        if (event instanceof KeyEvent) {
            eventText += " code: " + ((KeyEvent) event).getCode();
        }
        String handlerText = "";
        if (handlers.size() == getRecordSize() && handlers.get(index) != null) {
            handlerText = "\n     handler: " + handlers.get(index).getClass() + " type: " + handlerTypes.get(index);
        }
        String log = "\n------ Event for index: " + index + " @" + event.hashCode()
                + "\n " + eventText + handlerText + "\n Frames: ";
        List<StackFrame> frames = stackFrames.get(index);
        for(int i = 0; i < frames.size(); i++) {
            log += "\n " + frames.get(i);
            
        }
        System.out.println(log);
    }
    /**
     * Returns the eventSource of event at index
     * 
     * @param index
     * @return the eventSource of the recorded event at index
     * 
     */
    public Object getEventSource(int index) {
        return events.get(index).getSource();
    }

    /**
     * Returns the first stackFrame of the List of stackFrames at index.
     * 
     * @param index
     * @return
     */
    public StackFrame getFirstStackFrame(int index) {
        return stackFrames.get(index).get(0);
    }
}
