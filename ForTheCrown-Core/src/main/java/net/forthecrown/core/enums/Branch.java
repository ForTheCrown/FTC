package net.forthecrown.core.enums;

public enum Branch {
    DEFAULT ("Branch-less"),
    ROYALS ("Royals"),
    VIKINGS ("Vikings"),
    PIRATES ("Pirates");

    private final String name;
    Branch(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
