package net.forthecrown.user.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.GameMode;

/**
 * Represents a game mode, but with more stuff, like the translation component and whether the gamemode can fly
 */
public enum FtcGameMode {
    SURVIVAL ("gameMode.survival", GameMode.SURVIVAL, false, 0),
    CREATIVE ("gameMode.creative", GameMode.CREATIVE, true, 1),
    SPECTATOR ("gameMode.spectator", GameMode.SPECTATOR, true, 2),
    ADVENTURE ("gameMode.adventure", GameMode.ADVENTURE, false, 3);

    public final String translationKey;
    public final GameMode bukkit;
    public final boolean canFly;
    public final int id;

    FtcGameMode(String translationKey, GameMode handle, boolean canFly, int id){
        this.translationKey = translationKey;
        this.bukkit = handle;
        this.canFly = canFly;
        this.id = id;
    }

    /**
     * Gets the FTC gamemode equivalent of the given bukkit type
     * @param bukkit The bukkit gamemode
     * @return The FTC equivalent
     */
    public static FtcGameMode wrap(GameMode bukkit){
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
