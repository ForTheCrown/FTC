package net.forthecrown.core.enums;

import org.jetbrains.annotations.NotNull;

/**
 * A class representing a rank branch
 */
public enum Branch {
    DEFAULT ("Branch-less"),
    ROYALS ("Royals", "Royal"),
    VIKINGS ("Vikings", "Viking"),
    PIRATES ("Pirates", "Pirate");

    private final String name;
    private final String singularName;
    Branch(String name, String singularName){
        this.name = name;
        this.singularName = singularName;
    }
    Branch(String name){
        this.name = name;
        this.singularName = null;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getSingularName() {
        return singularName == null ? name : singularName;
    }
}
