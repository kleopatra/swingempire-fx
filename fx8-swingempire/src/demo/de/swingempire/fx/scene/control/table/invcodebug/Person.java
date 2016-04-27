/*
 * Created on 18.04.2016
 *
 */
package de.swingempire.fx.scene.control.table.invcodebug;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Person {
    private final SimpleBooleanProperty  invited;
    private final SimpleStringProperty   invCode;
    private final SimpleStringProperty   name;

    public Person() {
        this(true,null,null);
    }

    public Person(boolean invited, String invCode, String name) {
        this.invited = new SimpleBooleanProperty(invited);
        this.invCode = new SimpleStringProperty(invCode);
        this.name = new SimpleStringProperty(name);
    }


    public boolean isInvited() {
        return invited.get();
    }
    public void setInvited(boolean invited) {
        this.invited.set(invited);
    }
    public SimpleBooleanProperty invitedProperty() {
        return invited;
    }


    public String getInvCode(){
        return invCode.get();
    }
    public void setInvCode(String invCode) {
        this.invCode.set(invCode);
    }
    public SimpleStringProperty invCodeProperty() {
        return invCode;
    }


    public String getName(){
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }
    public SimpleStringProperty nameProperty() {
        return name;
    }
}