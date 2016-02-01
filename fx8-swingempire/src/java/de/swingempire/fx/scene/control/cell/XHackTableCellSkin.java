/*
 * Created on 29.01.2016
 *
 */
package de.swingempire.fx.scene.control.cell;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.InputMap.Mapping;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.skin.TableCellSkin;
import javafx.scene.input.MouseEvent;

/**
 * Not really useful for replacing Behavior:
 * - super behavior is final, can't hack a replace
 * - super behavior will interfere (it installed all the listeners)
 * - PENDING: hack super and call dispose? doesn't work, old input binding
 *   still called before the new one available.
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class XHackTableCellSkin<S,T> extends TableCellSkin<S,T>  {

    private BehaviorBase<TableCell<S,T>> behaviorBase;
    
    /**
     * @param control
     */
    public XHackTableCellSkin(TableCell<S,T> control) {
        super(control);
        replaceBehavior(null);
    }
    
    

    /**
     * Overridden to dispose the replaced behaviour.
     */
    @Override
    public void dispose() {
        if (behaviorBase != null) {
            behaviorBase.dispose();
        }
        super.dispose();
    }



    /**
     * Just a marker - can't because super behaviour is final!
     * Trying to dispose (aka: remove all input bindings) from old - doesn't work:
     * Old mousePressed binding still called before the new.
     * 
     * @param xTableCellBehavior
     */
    private void replaceBehavior(XTableCellBehavior<S, T> xTableCellBehavior) {
        BehaviorBase<?> old = (BehaviorBase<?>) invokeGetField("behavior");
        if (old != null) {
            InputMap map = old.getInputMap();
            Optional oldEntry = map.lookupMapping(MouseEvent.MOUSE_PRESSED);
            // this removes all mappings, nothing left
            // old.getInputMap().dispose();
            // this removes the defaults (mappings are empty)
            // but still something is active: default mouse handlers
            // in cellBehaviour are invoked before the replaced
            // even though the mappings are removed
            old.dispose();
            Optional entry = map.lookupMapping(MouseEvent.MOUSE_PRESSED);
            LOG.info("old/new: " + oldEntry + "/" + entry);
        }
        behaviorBase = new XTableCellBehavior<>(getSkinnable());
//        LOG.info("new: " + behaviorBase.getInputMap().lookupMapping(MouseEvent.MOUSE_PRESSED).get());
        LOG.info("new: " + lookupMappingKey(behaviorBase.getInputMap(), MouseEvent.MOUSE_PRESSED));
    }
    
    private List<Mapping<?>> lookupMappingKey(InputMap map, Object mappingKey) {
        return (List<Mapping<?>>) map.getMappings().stream()
                .filter(mapping -> !((InputMap.MouseMapping) mapping).isDisabled())
                .filter(mapping -> mappingKey.equals(((Mapping<?>) mapping).getMappingKey()))
                .collect(Collectors.toList());
    }


    private Object invokeGetField(String name) {
        Class<?> target = TableCellSkin.class;
        try {
            Field field = target.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(XHackTableCellSkin.class.getName());
}
