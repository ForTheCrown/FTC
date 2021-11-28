package net.forthecrown.crown;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface TimerMessageFormatter {
    Component format(String timer, long millis);

    static TimerMessageFormatter defaultTimer(){
        return (timer, millis) -> Component.text(timer);
    }
}
