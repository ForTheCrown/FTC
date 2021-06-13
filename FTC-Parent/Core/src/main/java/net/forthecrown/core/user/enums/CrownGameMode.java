package net.forthecrown.core.user.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.GameMode;

public enum CrownGameMode {
    SURVIVAL ("gameMode.survival", GameMode.SURVIVAL, false),
    CREATIVE ("gameMode.creative", GameMode.CREATIVE, true),
    SPECTATOR ("gameMode.spectator", GameMode.SPECTATOR, true),
    ADVENTURE ("gameMode.adventure", GameMode.ADVENTURE, false);

    public final String translationKey;
    public final GameMode bukkit;
    public final boolean canFly;

    CrownGameMode(String translationKey, GameMode handle, boolean canFly){
        this.translationKey = translationKey;
        this.bukkit = handle;
        this.canFly = canFly;
    }

    public static CrownGameMode wrap(GameMode bukkit){
        switch (bukkit){
            case CREATIVE: return CREATIVE;
            case SURVIVAL: return SURVIVAL;
            case ADVENTURE: return ADVENTURE;
            case SPECTATOR: return SPECTATOR;

            default: throw new IllegalStateException("Unexpected value: " + bukkit);
        }
    }

    public TranslatableComponent title(){
        return Component.translatable(translationKey);
    }
}
