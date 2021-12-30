package net.forthecrown.crown;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface ScoreFormatter {
    Component format(int score);

    static ScoreFormatter defaultFormat(){
        return Component::text;
    }

    static ScoreFormatter timerFormat(){
        return score -> Component.text(EventTimer.getTimerCounter(score).toString());
    }
}
