package net.forthecrown.july.rewards;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;

public enum RaceRanks {
    SLOWPOKE("&8[&7Slowpoke&8] &r"),
    FAST_CUTIE ("&7[&6Roadrunner&7] &r");

    private final String prefix;

    RaceRanks(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public Component prefix(){
        return ChatUtils.convertString(prefix, true);
    }
}
