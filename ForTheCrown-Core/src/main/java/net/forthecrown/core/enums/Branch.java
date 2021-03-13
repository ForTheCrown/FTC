package net.forthecrown.core.enums;

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
        this.singularName = name;
    }

    public String getName() {
        return name;
    }

    public String getSingularName() {
        return singularName;
    }
}
