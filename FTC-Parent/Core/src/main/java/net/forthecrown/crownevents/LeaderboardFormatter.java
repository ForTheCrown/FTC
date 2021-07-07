package net.forthecrown.crownevents;

import net.kyori.adventure.text.Component;

public interface LeaderboardFormatter {
    Component formatName(int pos, String name, String score);

    static LeaderboardFormatter defaultFormat(){
        return (pos, name, score) -> Component.text(pos + ". " + name + ": " + score);
    }
}
