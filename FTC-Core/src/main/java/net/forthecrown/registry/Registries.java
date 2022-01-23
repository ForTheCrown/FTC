package net.forthecrown.registry;

import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.core.Keys;
import net.forthecrown.core.animation.BlockAnimation;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.economy.houses.House;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.checks.UsageCheck;
import org.bukkit.NamespacedKey;

/**
 * A class which stores registries and provides static methods to make them
 */
public interface Registries {
    /**
     * A registry of registries, master registry
     */
    Registry<Registry<?>> MASTER_REGISTRY               = new BaseRegistry<>(Keys.forthecrown("master_registry"));

    Registry<InteractableNPC> NPCS                      = create("npcs");
    Registry<BlockAnimation> ANIMATIONS                 = create("animations");
    Registry<BlockStructure> STRUCTURES                 = create("structures");

    CloseableRegistry<VoteTopic> VOTE_TOPICS            = createCloseable("vote_topics");
    CloseableRegistry<House> HOUSES                     = createCloseable("houses");

    CloseableRegistry<WeaponGoal> WEAPON_GOALS          = createCloseable("weapon_goals");
    CloseableRegistry<WeaponAbility> WEAPON_ABILITIES   = createCloseable("weapon_abilities");

    //Cosmetic things registries
    CloseableRegistry<DeathEffect> DEATH_EFFECTS        = createCloseable("death_effects");
    CloseableRegistry<TravelEffect> TRAVEL_EFFECTS      = createCloseable("travel_effects");
    CloseableRegistry<CosmeticEmote> EMOTES             = createCloseable("emotes");
    CloseableRegistry<ArrowEffect> ARROW_EFFECTS        = createCloseable("arrow_effects");

    //Usables
    CloseableRegistry<UsageCheck> USAGE_CHECKS          = createCloseable("usage_checks");
    CloseableRegistry<UsageAction> USAGE_ACTIONS        = createCloseable("usage_actions");

    //Comvars
    CloseableRegistry<ComVarType<?>> COMVAR_TYPES       = createCloseable("comvar_types");

    //Dungeons
    CloseableRegistry<DungeonBoss<?>> DUNGEON_BOSSES    = createCloseable("dungeon_bosses");

    /**
     * Creates a closeable registry with the given key and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type which the registry holds
     * @return The registry
     */
    static <T> CloseableRegistry<T> createCloseable(String strKey){
        NamespacedKey key = Keys.parse(strKey);
        return (CloseableRegistry<T>) MASTER_REGISTRY.register(key, new CloseableRegistryBase<>(key));
    }

    /**
     * Creates a default registry and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type the registry holds
     * @return The created registry
     */
    static <T> Registry<T> create(String strKey){
        NamespacedKey key = Keys.parse(strKey);
        return (Registry<T>) MASTER_REGISTRY.register(key, new BaseRegistry<>(key));
    }
}
