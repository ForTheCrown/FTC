package net.forthecrown.crownevents;

import net.kyori.adventure.text.Component;

public interface TimerMessageFormatter {
    Component format(String timer, long millis);

    static TimerMessageFormatter defaultTimer(){
        return (timer, millis) -> Component.text(timer);
    }
}
