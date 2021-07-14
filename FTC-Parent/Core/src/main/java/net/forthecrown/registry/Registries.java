package net.forthecrown.registry;

import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.core.CrownCore;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.AbstractDeathEffect;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.preconditions.UsageCheck;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;

/**
 * A class which stores registries and provides static methods to make them
 */
public interface Registries {
    /**
     * A registry of registries, master registry
     */
    Registry<Registry<?>> MASTER_REGISTRY = new BaseRegistry<>(CrownCore.coreKey("master_registry"));

    CloseableRegistry<CosmeticEmote> EMOTES = createCloseable("emotes");
    CloseableRegistry<AbstractDeathEffect> DEATH_EFFECTS = createCloseable("death_effects");
    CloseableRegistry<ArrowEffect> ARROW_EFFECTS = createCloseable("arrow_effects");

    CloseableRegistry<UsageCheck> USAGE_CHECKS = createCloseable("usage_checks");
    CloseableRegistry<UsageAction> USAGE_ACTIONS = createCloseable("usage_actions");

    CloseableRegistry<ComVarType<?>> COMVAR_TYPES = createCloseable("comvar_types");

    CloseableRegistry<DungeonBoss<?>> DUNGEON_BOSSES = createCloseable("dungeon_bosses");

    /**
     * Creates a closeable registry with the given key and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type which the registry holds
     * @return The registry
     */
    static <T> CloseableRegistry<T> createCloseable(String strKey){
        Key key = FtcUtils.parseKey(strKey);
        return (CloseableRegistry<T>) MASTER_REGISTRY.register(key, new CloseableRegistryBase<>(key));
    }

    /**
     * Creates a default registry and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type the registry holds
     * @return The created registry
     */
    static <T> Registry<T> create(String strKey){
        Key key = FtcUtils.parseKey(strKey);
        return (Registry<T>) MASTER_REGISTRY.register(key, new BaseRegistry<>(key));
    }
}
