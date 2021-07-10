package net.forthecrown.user.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.GameMode;

public enum CrownGameMode {
    SURVIVAL ("gameMode.survival", GameMode.SURVIVAL, false, 0),
    CREATIVE ("gameMode.creative", GameMode.CREATIVE, true, 1),
    SPECTATOR ("gameMode.spectator", GameMode.SPECTATOR, true, 2),
    ADVENTURE ("gameMode.adventure", GameMode.ADVENTURE, false, 3);

    public final String translationKey;
    public final GameMode bukkit;
    public final boolean canFly;
    public final int id;

    CrownGameMode(String translationKey, GameMode handle, boolean canFly, int id){
        this.translationKey = translationKey;
        this.bukkit = handle;
        this.canFly = canFly;
        this.id = id;
    }

    public static CrownGameMode wrap(GameMode bukkit){
        return switch (bukkit) {
            case CREATIVE -> CREATIVE;
            case SURVIVAL -> SURVIVAL;
            case ADVENTURE -> ADVENTURE;
            case SPECTATOR -> SPECTATOR;
        };
    }

    public TranslatableComponent title(){
        return Component.translatable(translationKey);
    }
}
