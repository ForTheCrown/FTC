package net.forthecrown.dungeons.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.RegistryArguments;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionSpawnBoss extends UsageAction {

  // --- TYPE ---
  public static final UsageType<ActionSpawnBoss> TYPE = UsageType.of(ActionSpawnBoss.class)
      .setSuggests(RegistryArguments.DUNGEON_BOSS::listSuggestions);

  private final String bossKey;

  public ActionSpawnBoss(String bossKey) {
    super(TYPE);
    this.bossKey = bossKey;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    Bosses.REGISTRY.get(bossKey).ifPresent(boss -> {
      boss.attemptSpawn(player);
    });
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("boss='{0}'", bossKey);
  }

  @Override
  public @Nullable BinaryTag save() {
    return BinaryTags.stringTag(bossKey);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionSpawnBoss parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    return new ActionSpawnBoss(RegistryArguments.DUNGEON_BOSS.parse(reader).getKey());
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionSpawnBoss load(BinaryTag tag) {
    return new ActionSpawnBoss(tag.toString());
  }
}