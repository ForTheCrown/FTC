package net.forthecrown.registry;

import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.preconditions.UsageCheck;
import net.kyori.adventure.key.Key;

public interface Registries {
    Registry<Registry<?>> MASTER_REGISTRY = new BaseRegistry<>(CrownCore.coreKey("master_registry"));

    CloseableRegistry<CosmeticEmote> EMOTES = createCloseable("emotes");
    CloseableRegistry<AbstractDeathEffect> DEATH_EFFECTS = createCloseable("death_effects");
    CloseableRegistry<ArrowEffect> ARROW_EFFECTS = createCloseable("arrow_effects");

    CloseableRegistry<UsageCheck> USAGE_CHECKS = createCloseable("usage_checks");
    CloseableRegistry<UsageAction> USAGE_ACTIONS = createCloseable("usage_actions");

    CloseableRegistry<ComVarType<?>> COMVAR_TYPES = createCloseable("comvar_types");

    CloseableRegistry<DungeonBoss<?>> DUNGEON_BOSSES = createCloseable("dungeon_bosses");

    static <T> CloseableRegistry<T> createCloseable(String strKey){
        Key key = CrownCore.coreKey(strKey);
        return (CloseableRegistry<T>) MASTER_REGISTRY.register(key, new CloseableRegistryBase<>(key));
    }

    static <T> Registry<T> create(String strKey){
        Key key = CrownCore.coreKey(strKey);
        return (Registry<T>) MASTER_REGISTRY.register(key, new BaseRegistry<>(key));
    }
}
