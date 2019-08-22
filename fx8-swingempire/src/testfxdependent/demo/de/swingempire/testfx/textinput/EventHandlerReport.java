/*
 * Created on 22.08.2019
 *
 */
package de.swingempire.testfx.textinput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * Utility class to keep track of eventHandlers that are messaged during
 * eventDispatch.
 * <p>
 * PENDING: move into testsupport 
 * @author Jeanette Winzenburg, Berlin
 */
public class EventHandlerReport {
    private List<EventHandler<?>> handlers;

    private List<Event> events;

    public EventHandlerReport() {
        handlers = new ArrayList<>();
        events = new ArrayList<>();
    }

    /**
     * Creates and returns an EventHandler that adds itself to the list of
     * notified handlers and adds the received event to the list of events.
     * <p>
     * Both lists are fifo (in contrast to most other xxReport classes) because
     * the testing code is interested in the actual sequence (not the "last").
     * 
     * @param <T> the type of Event.
     * @return an eventHandler that adds itself and the event to the list of
     *         handlers/events, respectively.
     */
    public <T extends Event> EventHandler<T> createEventHandler() {
        EventHandler<T> handler = new EventHandler<>() {

            @Override
            public void handle(Event event) {
                handlers.add(this);
                events.add(event);
            }
        };
        return handler;
    }

    public <T extends Event> EventHandler<T> addEventHandler(Scene node, EventType<T> type) {
        EventHandler<T> handler = createEventHandler();
        node.addEventHandler(type, handler);
        return handler;
    }
    
    public <T extends Event> EventHandler<T> addEventFilter(Scene node, EventType<T> type) {
        EventHandler<T> handler = createEventHandler();
        node.addEventFilter(type, handler);
        return handler;
    }
    public <T extends Event> EventHandler<T> addEventHandler(Node node, EventType<T> type) {
        EventHandler<T> handler = createEventHandler();
        node.addEventHandler(type, handler);
        return handler;
    }
    
    public <T extends Event> EventHandler<T> addEventFilter(Node node, EventType<T> type) {
        EventHandler<T> handler = createEventHandler();
        node.addEventFilter(type, handler);
        return handler;
    }
    /**
     * Clears all internal state. Here: clears list of events/handlers.
     */
    public void clear() {
        handlers.clear();
        events.clear();
    }

    /**
     * Returns the size of received events/handler notified. 
     * @return
     */
    public int getEventCount() {
        return events.size();
    }

    /**
     * Returns an unmodifiable list of the messaged eventhandlers.
     * 
     * @return list of eventHandlers that were invoked.
     */
    public List<EventHandler<?>> getEventHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
