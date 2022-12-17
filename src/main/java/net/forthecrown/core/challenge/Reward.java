package net.forthecrown.core.challenge;


import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script2.Script;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.math.GenericMath;

@Getter
@Builder(builderClassName = "Builder")
@Data
public class Reward {

  public static final String
      KEY_GEMS = "gems",
      KEY_RHINES = "rhines",
      KEY_GUILDEXP = "guildExp",
      KEY_ITEM = "item",
      KEY_SCRIPT = "script";

  /**
   * Empty reward constant which never rewards anything, like my life lmao
   */
  public static final Reward EMPTY = new Reward(
      StreakBasedValue.EMPTY,
      StreakBasedValue.EMPTY,
      StreakBasedValue.EMPTY,
      null, null
  );

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * Rhine reward
   */
  private final StreakBasedValue rhines;

  /**
   * Gem reward
   */
  private final StreakBasedValue gems;

  /**
   * Guild EXP reward
   */
  private final StreakBasedValue guildExp;

  /**
   * Item given to player
   */
  private final ItemStack item;

  /**
   * Script ran when claiming item
   */
  private final String claimScript;

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Gets the reward item, null, if no item set
   */
  public ItemStack getItem() {
    return ItemStacks.isEmpty(item) ? null : item.clone();
  }

  /**
   * Tests if the reward is empty
   */
  public boolean isEmpty() {
    if (this == EMPTY) {
      return true;
    }

    return rhines == StreakBasedValue.EMPTY
        && gems == StreakBasedValue.EMPTY
        && guildExp == StreakBasedValue.EMPTY
        && ItemStacks.isEmpty(item)
        && Strings.isNullOrEmpty(claimScript);
  }

  /**
   * Tests if the reward is empty for the given streak value
   */
  public boolean isEmpty(int streak) {
    if (isEmpty()) {
      return true;
    }

    int rhines = this.rhines.getInt(streak);
    int gems = this.gems.getInt(streak);
    int guildExp = this.guildExp.getInt(streak);

    return rhines < 1
        && gems < 1
        && guildExp < 1
        && ItemStacks.isEmpty(item)
        && Strings.isNullOrEmpty(claimScript);
  }

  /**
   * Gives rewards to the given user
   *
   * @param user   The user to give rewards to
   * @param streak The user's challenge streak
   */
  public void give(User user, int streak) {
    int rhineReward = rhines.getInt(streak);
    int gemReward = gems.getInt(streak);
    int guildReward = guildExp.getInt(streak);

    TextJoiner joiner = TextJoiner.newJoiner()
        .setPrefix(Component.text("You got: ", NamedTextColor.YELLOW))
        .setSuffix(Component.text(".", NamedTextColor.YELLOW))
        .setDelimiter(Component.text(", ", NamedTextColor.YELLOW))
        .setColor(NamedTextColor.GOLD);

    if (rhineReward > 0) {
      user.addBalance(rhineReward);
      joiner.add(UnitFormat.rhines(rhineReward));
    }

    if (gemReward > 0) {
      user.addGems(0);
      joiner.add(UnitFormat.gems(rhineReward));
    }

    var guild = user.getGuild();
    if (guildReward > 0 && guild != null) {
      var member = guild.getMember(user.getUniqueId());

      var modifier = GuildManager.get().getExpModifier();
      float mod = modifier.getModifier();
      int modified = modifier.applyAsInt(guildReward);

      member.addExpEarned(modified);

      // Floating point precision
      if (GenericMath.floor(mod) > 1) {
        joiner.add(
            Text.format(
                "{0, number} Guild Exp ({1, number}x multiplier)",
                modified, mod
            )
        );
      } else {
        joiner.add(
            Text.format("{0, number} Guild Exp", guildReward)
        );
      }
    }

    if (ItemStacks.notEmpty(item)) {
      Util.giveOrDropItem(
          user.getInventory(),
          user.getLocation(),
          item.clone()
      );

      joiner.add(Text.itemDisplayName(item));
    }

    user.sendMessage(joiner);

    if (!Strings.isNullOrEmpty(claimScript)) {
      Script.run(claimScript, "onRewardClaim", user, streak);
    }
  }

  /**
   * Writes info about this reward to the given writer, using the given streak as context
   */
  public void write(TextWriter writer, int streak) {
    if (isEmpty()) {
      return;
    }

    writer.field("Rewards", "");

    int rhines = this.rhines.getInt(streak);
    int gems = this.gems.getInt(streak);
    int guildExp = this.guildExp.getInt(streak);

    if (rhines > 0) {
      writer.field("Rhines", Text.NUMBER_FORMAT.format(rhines));
    }

    if (gems > 0) {
      writer.field("Gems", Text.NUMBER_FORMAT.format(gems));
    }

    if (guildExp > 0) {
      var modifier = GuildManager.get().getExpModifier();
      float mod = modifier.getModifier();

      if (mod > 0) {
        writer.field("Guild Exp",
            Text.format(
                "{0, number} ({1, number}x multiplier, {2, number} originally)",
                modifier.applyAsInt(guildExp),
                mod,
                guildExp
            )
        );
      } else {
        writer.field("Guild Exp", Text.formatNumber(guildExp));
      }
    }

    if (ItemStacks.notEmpty(item)) {
      writer.field("Item", Text.itemAndAmount(item));
    }

    if (FTC.inDebugMode()
        && !Strings.isNullOrEmpty(claimScript)
    ) {
      writer.field("Script", claimScript);
    }
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public static Reward deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return builder()
        .rhines(StreakBasedValue.read(json.get(KEY_RHINES)))
        .gems(StreakBasedValue.read(json.get(KEY_GEMS)))
        .guildExp(StreakBasedValue.read(json.get(KEY_GUILDEXP)))

        .item(json.getItem(KEY_ITEM))
        .claimScript(json.getString(KEY_SCRIPT))

        .build();
  }
}