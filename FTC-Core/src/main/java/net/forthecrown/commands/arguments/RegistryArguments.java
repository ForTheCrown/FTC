package net.forthecrown.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.cosmetics.arrows.ArrowEffect;
import net.forthecrown.cosmetics.deaths.DeathEffect;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.dungeons.bosses.DungeonBoss;
import net.forthecrown.economy.houses.House;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.inventory.weapon.abilities.WeaponAbility;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.useables.checks.UsageCheck;
import net.forthecrown.core.animation.BlockAnimation;
import org.bukkit.NamespacedKey;

import java.util.concurrent.CompletableFuture;

public class RegistryArguments<T> implements ArgumentType<T> {
    private static final RegistryArguments<BlockAnimation> ANIMATION =     new RegistryArguments<>(Registries.ANIMATIONS, "Animation");
    private static final RegistryArguments<BlockStructure> STRUCTURE = new RegistryArguments<>(Registries.STRUCTURES, "Structure");

    private static final RegistryArguments<ArrowEffect> ARROW_EFFECT =  new RegistryArguments<>(Registries.ARROW_EFFECTS, "Arrow Effect");
    private static final RegistryArguments<DeathEffect> DEATH_EFFECT =  new RegistryArguments<>(Registries.DEATH_EFFECTS, "Death Effect");
    private static final RegistryArguments<TravelEffect> TRAVEL_EFFECT = new RegistryArguments<>(Registries.TRAVEL_EFFECTS, "Travel Effect");

    private static final RegistryArguments<DungeonBoss<?>> DUNGEON_BOSS =  new RegistryArguments<>(Registries.DUNGEON_BOSSES, "Dungeon boss");

    private static final RegistryArguments<UsageAction> USAGE_ACTION =  new RegistryArguments<>(Registries.USAGE_ACTIONS, "Usage action");
    private static final RegistryArguments<UsageCheck> USAGE_CHECK =   new RegistryArguments<>(Registries.USAGE_CHECKS, "Usage check");

    private static final RegistryArguments<House> HOUSE = new RegistryArguments<>(Registries.HOUSES, "Dynasty");

    private static final RegistryArguments<WeaponGoal> WEAPON_GOAL = new RegistryArguments<>(Registries.WEAPON_GOALS, "Weapon goal");
    private static final RegistryArguments<WeaponAbility> WEAPON_ABILITY = new RegistryArguments<>(Registries.WEAPON_ABILITIES, "Weapon ability");

    public static RegistryArguments<BlockAnimation> animation() {
        return ANIMATION;
    }

    public static RegistryArguments<ArrowEffect> arrowEffect() {
        return ARROW_EFFECT;
    }

    public static RegistryArguments<DeathEffect> deathEffect() {
        return DEATH_EFFECT;
    }

    public static RegistryArguments<TravelEffect> travelEffect() {
        return TRAVEL_EFFECT;
    }

    public static RegistryArguments<DungeonBoss<?>> dungeonBoss() {
        return DUNGEON_BOSS;
    }

    public static RegistryArguments<UsageAction> usageAction() {
        return USAGE_ACTION;
    }

    public static UsageAction getAction(CommandContext<CommandSource> c, String arg) {
        return c.getArgument(arg, UsageAction.class);
    }

    public static RegistryArguments<UsageCheck> usageCheck() {
        return USAGE_CHECK;
    }

    public static UsageCheck getCheck(CommandContext<CommandSource> c, String arg) {
        return c.getArgument(arg, UsageCheck.class);
    }

    public static RegistryArguments<House> house() {
        return HOUSE;
    }

    public static RegistryArguments<WeaponAbility> weaponAbility() {
        return WEAPON_ABILITY;
    }

    public static RegistryArguments<WeaponGoal> weaponGoal() {
        return WEAPON_GOAL;
    }

    public static RegistryArguments<BlockStructure> structure() {
        return STRUCTURE;
    }

    private final Registry<T> registry;
    private final DynamicCommandExceptionType unknown;

    public RegistryArguments(Registry<T> registry, String unknownMessage) {
        this.registry = registry;

        unknown = new DynamicCommandExceptionType(
                o -> new LiteralMessage("Unknown " + unknownMessage + ": '" + o + "'")
        );
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        NamespacedKey key = Keys.argumentType().parse(reader);

        T val = registry.get(key);
        if(val == null) throw unknown.createWithContext(GrenadierUtils.correctReader(reader, cursor), key);

        return val;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestRegistry(builder, registry);
    }
}
