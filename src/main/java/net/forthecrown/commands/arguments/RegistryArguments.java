package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.admin.JailCell;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.challenge.Challenge;
import net.forthecrown.core.challenge.ChallengeManager;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.core.npc.SimpleNpc;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.KeyedBoss;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.sell.SellShopMenu;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.Structures;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.test.UsageTests;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserRank;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.user.data.UserTimeTracker;
import net.forthecrown.waypoint.WaypointProperties;
import net.forthecrown.waypoint.WaypointProperty;
import net.minecraft.commands.CommandBuildContext;

@Getter
@SuppressWarnings({"unchecked", "rawtypes"})
public class RegistryArguments<T> implements ArgumentType<Holder<T>>, VanillaMappedArgument {

  public static final RegistryArguments<BlockStructure> STRUCTURE
      = new RegistryArguments<>(Structures.get().getRegistry(), "Structure");

  public static final RegistryArguments<KeyedBoss> DUNGEON_BOSS
      = new RegistryArguments<>(Bosses.REGISTRY, "Dungeon boss");

  public static final RegistryArguments<JailCell> JAIL_CELL
      = new RegistryArguments<>(Punishments.get().getCells(), "Jail Cell");

  public static final RegistryArguments<SimpleNpc> NPC
      = new RegistryArguments<>(Npcs.REGISTRY, "NPC");

  public static final RegistryArguments<SellShopMenu> SELLS_SHOP
      = new RegistryArguments<>(Economy.get().getSellShop().getMenus(), "Menu");

  public static final RegistryArguments<TimeField> TIME_FIELDS
      = new RegistryArguments<>(UserTimeTracker.TIME_FIELDS, "TimeStamp");

  public static final RegistryArguments<WaypointProperty> WAYPOINT_PROPERTY
      = new RegistryArguments<>(WaypointProperties.REGISTRY, "Waypoint property");

  public static final RegistryArguments<Challenge> CHALLENGE = new RegistryArguments<>(
      ChallengeManager.getInstance().getChallengeRegistry(),
      "Challenge"
  );

  public static final RegistryArguments<UserRank> RANKS
      = new RegistryArguments<>(UserRanks.REGISTRY, "Rank");

  public static final RegistryArguments<UsageType<UsageAction>> USAGE_ACTION = new RegistryArguments<>(
      ((Registry) UsageActions.REGISTRY), "Usage action"
  );
  public static final RegistryArguments<UsageType<UsageTest>> USAGE_CHECK = new RegistryArguments<>(
      ((Registry) UsageTests.REGISTRY), "Usage check"
  );

  private final Registry<T> registry;
  private final String unknown;

  public RegistryArguments(Registry<T> registry, String unknownMessage) {
    this.registry = registry;
    this.unknown = unknownMessage;
  }

  @Override
  public Holder<T> parse(StringReader reader) throws CommandSyntaxException {
    int cursor = reader.getCursor();
    String key = Arguments.FTC_KEY.parse(reader);

    return registry.getHolder(key).orElseThrow(() -> {
      reader.setCursor(cursor);
      return Exceptions.unknown(unknown, reader, key);
    });
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, registry.keys());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return Arguments.FTC_KEY.getVanillaType(context);
  }
}