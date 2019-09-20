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
import static java.util.stream.Collectors.*;

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
    private int callerDepth;
    
    public EventStackRecorder() {
        this(1);
    }

    /**
     * Instantiates a recorder with callerDepth 1 and depth to record.
     * 
     * @param depth the depth to record
     */
    public EventStackRecorder(int depth) {
        this(1, depth);
    }

    /**
     * Instantiates a recorder with callerDepth and depth to record.
     * 
     * @param callerDepth the stack depth of the caller
     * @param depth the depth to record
     */
    public EventStackRecorder(int callerDepth, int depth) {
        events = new ArrayList<>();
        stackFrames = new ArrayList<>();
        handlers = new ArrayList<>();
        handlerTypes = new ArrayList<>();
        this.depth = depth;
        this.callerDepth = callerDepth;
    }
    
    /**
     * Clears all recorded state.
     */
    public void clear() {
        events.clear();
        stackFrames.clear();
        handlers.clear();
        handlerTypes.clear();
    }
    
    /**
     * Returns the recorded size.
     * 
     * @return the count of recorded events
     * @throws IllegalStateException if # of events differs from # of
     *         stackFrames
     */
    public int recordedSize() {
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
        int selfDepth = 1;
        doRecord(event, selfDepth);
    }

    /**
     * @param event
     * @param selfDepth
     */
    protected void doRecord(Event event, int selfDepth) {
        events.add(event);
        List<StackFrame> stack = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)
                .walk(s -> s.skip(selfDepth /* this */ + callerDepth /* caller */).limit(depth)
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
        doRecord(event, 2);
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
    
    public void logAll() {
        for (int i = 0; i < recordedSize(); i++) {
            log(i);
        }
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
        if (handlers.size() == recordedSize() && handlers.get(index) != null) {
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
    
    public List<Object> getEventSources() {
        return events.stream()
                .map(e -> e.getSource())
                .collect(toList());      
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
    
    public List<StackFrame> getStackFrames(int index) {
        return stackFrames.get(index);
    }
    
    public String compareSources(List<Object> expected) {
        return compareSources(expected, getEventSources());
    }
    
    public static String compareSources(List<Object> expected, List<Object> sources) {
        StringBuilder out = new StringBuilder("expected/actual: \n ");
        expected.stream().forEach(o -> out.append(" " + o.getClass().getSimpleName()));
        out.append("\n ");
        sources.stream().forEach(o -> out.append(" " + o.getClass().getSimpleName()));
        out.append("\n ");
        return out.toString();
    }
}
