package net.forthecrown.dungeons.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.dungeons.boss.DrawnedBoss;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.EnumArgument;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.useables.Usable;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionGiveArtifact extends UsageAction {

  private static final EnumArgument<DrawnedBoss.Artifacts> ARTIFACT_PARSER
      = ArgumentTypes.enumType(DrawnedBoss.Artifacts.class);

  // --- TYPE ---
  public static final UsageType<ActionGiveArtifact> TYPE = UsageType.of(ActionGiveArtifact.class)
      .setSuggests(ARTIFACT_PARSER::listSuggestions);

  private final DrawnedBoss.Artifacts artifacts;

  public ActionGiveArtifact(DrawnedBoss.Artifacts artifacts) {
    super(TYPE);
    this.artifacts = artifacts;
  }

  @Override
  public void onUse(Player player, Usable holder) {
    var item = artifacts.item();
    Util.giveOrDropItem(player.getInventory(), player.getLocation(), item);

    player.sendMessage(
        Text.format("You got the &e{0, item}&r.",
            NamedTextColor.GRAY,
            item
        )
    );
  }

  @Override
  public @Nullable Component displayInfo() {
    return Text.format("artifact='{0}'", artifacts.name().toLowerCase());
  }

  @Override
  public @Nullable BinaryTag save() {
    return TagUtil.writeEnum(artifacts);
  }

  // --- TYPE CONSTRUCTORS ---

  @UsableConstructor(ConstructType.PARSE)
  public static ActionGiveArtifact parse(StringReader reader, CommandSource source)
      throws CommandSyntaxException {
    return new ActionGiveArtifact(ARTIFACT_PARSER.parse(reader));
  }

  @UsableConstructor(ConstructType.TAG)
  public static ActionGiveArtifact load(BinaryTag tag) {
    return new ActionGiveArtifact(TagUtil.readEnum(DrawnedBoss.Artifacts.class, tag));
  }
}