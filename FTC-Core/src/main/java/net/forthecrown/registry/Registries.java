package net.forthecrown.registry;

import net.forthecrown.core.Keys;
import net.forthecrown.core.animation.BlockAnimation;
import net.forthecrown.core.battlepass.BattlePassChallenge;
import net.forthecrown.core.battlepass.Reward;
import net.forthecrown.core.npc.InteractableNPC;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.emotes.CosmeticEmote;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.dungeons.boss.SpawnRequirement;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.economy.guilds.topics.VoteTopic;
import net.forthecrown.economy.houses.House;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.tree.StructureNodeType;
import net.forthecrown.structure.tree.StructureType;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.vars.types.VarType;
import org.bukkit.NamespacedKey;

/**
 * A class which stores registries and provides static methods to make them
 */
public interface Registries {
    /**
     * A registry of registries, master registry
     */
    Registry<Registry<?>>                   MASTER_REGISTRY     = new RegistryImpl<>(Keys.forthecrown("master_registry"));

    Registry<InteractableNPC>               NPCS                = create("npcs");
    Registry<BlockAnimation>                ANIMATIONS          = create("animations");

    Registry<BlockStructure>                STRUCTURES          = create("structures");
    Registry<StructureType>                 STRUCTURE_TYPES     = create("structure_types");
    Registry<StructureNodeType>             STRUCTURE_NODE_TYPES= create("structure_node_types");

    CloseableRegistry<VoteTopic>            VOTE_TOPICS         = createCloseable("vote_topics");
    CloseableRegistry<House>                HOUSES              = createCloseable("houses");

    CloseableRegistry<WeaponGoal>           WEAPON_GOALS        = createCloseable("weapon_goals");
    CloseableRegistry<WeaponAbility.Type>   WEAPON_ABILITIES    = createCloseable("weapon_abilities");

    //Cosmetic things registries
    CloseableRegistry<DeathEffect>          DEATH_EFFECTS       = createCloseable("death_effects");
    CloseableRegistry<TravelEffect>         TRAVEL_EFFECTS      = createCloseable("travel_effects");
    CloseableRegistry<CosmeticEmote>        EMOTES              = createCloseable("emotes");
    CloseableRegistry<ArrowEffect>          ARROW_EFFECTS       = createCloseable("arrow_effects");

    //Usables
    CloseableRegistry<UsageCheck>           USAGE_CHECKS        = createCloseable("usage_checks");
    CloseableRegistry<UsageAction>          USAGE_ACTIONS       = createCloseable("usage_actions");

    //Vars
    CloseableRegistry<VarType<?>>           VAR_TYPES           = createCloseable("var_types");

    //Dungeons
    CloseableRegistry<KeyedBoss>            DUNGEON_BOSSES      = createCloseable("dungeon_bosses");
    CloseableRegistry<SpawnRequirement.Type>SPAWN_REQUIREMENTS  = createCloseable("boss_spawn_tests");
    Registry<DungeonLevel>                  DUNGEON_LEVELS      = create("dungeon_levels");

    //Battle pass
    CloseableRegistry<BattlePassChallenge>  GOAL_BOOK           = createCloseable("battle_pass_challenges");
    CloseableRegistry<Reward>               REWARDS             = createCloseable("battle_pass_rewards");

    /**
     * Creates a closeable registry with the given key and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type which the registry holds
     * @return The registry
     */
    static <T> CloseableRegistry<T> createCloseable(String strKey){
        NamespacedKey key = Keys.parse(strKey);
        return (CloseableRegistry<T>) MASTER_REGISTRY.register(key, new CloseableRegistryImpl<>(key));
    }

    /**
     * Creates a default registry and registers it into the master registry
     * @param strKey The key of the registry
     * @param <T> The type the registry holds
     * @return The created registry
     */
    static <T> Registry<T> create(String strKey){
        NamespacedKey key = Keys.parse(strKey);
        return (Registry<T>) MASTER_REGISTRY.register(key, new RegistryImpl<>(key));
    }
}
