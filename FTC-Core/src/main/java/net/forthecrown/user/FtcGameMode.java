package net.forthecrown.user;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.Translatable;
import net.minecraft.world.level.GameType;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a game mode, but with more stuff, like the translation component and whether the gamemode can fly
 */
public enum FtcGameMode implements Translatable {
    SURVIVAL (GameMode.SURVIVAL, false, 0),
    CREATIVE (GameMode.CREATIVE, true, 1),
    SPECTATOR (GameMode.SPECTATOR, true, 2),
    ADVENTURE (GameMode.ADVENTURE, false, 3);

    public final String translationKey;
    public final GameMode bukkit;
    public final boolean canFly;
    public final int id;

    FtcGameMode(GameMode handle, boolean canFly, int id){
        this.translationKey = "gameMode." + name().toLowerCase();
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

    public static FtcGameMode wrap(GameType type) {
        return switch (type) {
            case CREATIVE -> CREATIVE;
            case SURVIVAL -> SURVIVAL;
            case ADVENTURE -> ADVENTURE;
            case SPECTATOR -> SPECTATOR;
        };
    }

    public TranslatableComponent title(){
        return Component.translatable(translationKey);
    }

    @Override
    public @NotNull String translationKey() {
        return translationKey;
    }
}
