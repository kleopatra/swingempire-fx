/*
 * Created on 27.08.2018
 *
 */
package de.swingempire.fx.scene.control.tree.dnd;

import java.io.Serializable;
import java.util.Objects;

/**
 * TreeDrag: 
 */
public class TaskNode implements Serializable {
    private String name;

    public TaskNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final TaskNode other = (TaskNode) obj;
        return Objects.equals(this.name, other.name);
    }

}
