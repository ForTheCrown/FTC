package net.forthecrown.crown;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface LeaderboardFormatter {
    Component formatName(int pos, String name, Component score);

    static LeaderboardFormatter defaultFormat(){
        return (pos, name, score) -> Component.text(pos + ". " + name + ": ").append(score);
    }
}
