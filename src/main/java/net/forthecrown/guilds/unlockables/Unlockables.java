package net.forthecrown.guilds.unlockables;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.module.OnEnable;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableBrackets;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableColorType;
import net.forthecrown.guilds.unlockables.nameformat.UnlockableStyle;

public @UtilityClass class Unlockables {
    public final Registry<Unlockable> REGISTRY = Registries.newFreezable();

    @OnEnable
    private static void init() {
        UnlockableColor.registerAll(REGISTRY);

        registerAll(UnlockableChunkUpgrade.values());
        registerAll(UnlockableRankSlot.values());
        registerAll(Upgradable.values());
        registerAll(UnlockableSetting.values());
        registerAll(UnlockableBrackets.values());
        registerAll(UnlockableColorType.values());
        registerAll(UnlockableStyle.values());
    }

    private static void registerAll(Unlockable[] unlockables) {
        for (var u: unlockables) {
            String key;

            if (REGISTRY.contains(u.getKey())) {
                key = u.getClass().getSimpleName() + "/" + u.getKey();
            } else {
                key = u.getKey();
            }

            REGISTRY.register(key, u);
        }
    }
}