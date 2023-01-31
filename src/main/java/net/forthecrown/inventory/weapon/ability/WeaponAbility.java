package net.forthecrown.inventory.weapon.ability;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptResult;
import net.forthecrown.inventory.weapon.SwordRank;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class WeaponAbility {
  public static final String TAG_LEVEL = "level";
  public static final String TAG_USES = "uses";
  public static final String TAG_COOLDOWN_OVERRIDE = "cooldownOverride";

  public static final int START_LEVEL = 1;
  public static final int UNLIMITED_USES = -1;
  public static final int NO_OVERRIDE = -1;

  private int level = START_LEVEL;
  private int remainingUses = 0;

  private long cooldownOverride = NO_OVERRIDE;

  private final WeaponAbilityType type;
  private final Script script;

  public WeaponAbility(WeaponAbilityType type, Script script) {
    this.type = type;
    this.script = script;

    assert script.isCompiled() : "Script not compiled when given to ability";

    setLevel(START_LEVEL);
    setRemainingUses(0);
    setCooldownOverride(NO_OVERRIDE);
  }

  public Component displayName() {
    return getType().getDisplayName()
        .colorIfAbsent(NamedTextColor.GRAY);
  }

  public void write(TextWriter writer, User user) {
    writer.line(displayName());

    if (level > START_LEVEL) {
      writer.formatted(" {0, number, -roman}", NamedTextColor.GRAY, level);
    }

    Component usesLeftText;

    if (remainingUses == UNLIMITED_USES) {
      usesLeftText = Component.text("Infinite", NamedTextColor.GREEN);
    } else {
      float useLimit = getType().getLimit().get(user);
      float remaining = this.remainingUses;

      final float progress = (useLimit-remaining) / useLimit;
      TextColor color = Text.lerp(
          progress,
          NamedTextColor.GREEN,
          NamedTextColor.GOLD,
          NamedTextColor.RED
      );

      usesLeftText = Text.formatNumber(remaining).color(color);
    }

    writer.formattedLine("Uses left: {0, number}",
        NamedTextColor.GRAY,
        usesLeftText
    );
  }

  public long getCooldownTicks(SwordRank rank) {
    if (cooldownOverride != NO_OVERRIDE) {
      return cooldownOverride;
    }

    return script.invokeIfExists("getCooldown", rank)
        .flatMap(ScriptResult::result)
        .flatMap(o -> {
          if (o instanceof String str) {
            return Optional.of(UpgradeCooldown.parseTicks(str));
          }

          if (o instanceof Number number) {
            return Optional.of(number.longValue());
          }

          return Optional.empty();
        })

        .orElseGet(() -> type.getCooldown().get(rank));
  }

  public void setLevel(int level) {
    this.level = Math.max(START_LEVEL, level);
    script.put("level", this.level);
  }

  public void setRemainingUses(int remainingUses) {
    this.remainingUses = Math.max(UNLIMITED_USES, remainingUses);
    script.put("remainingUses", this.remainingUses);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  /**
   * Right-click callback, triggered when the player right-clicks, aka
   * interacts, with a block, entity or air.
   *
   * @param player       The player that right-clicked
   * @param clicked      The right-clicked entity,
   * @param clickedBlock The block that was clicked, exclusive with clicked,
   *                     If this is not null, clicked is
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public boolean onRightClick(Player player,
                              @Nullable Entity clicked,
                              @Nullable Block clickedBlock
  ) {
    return invokeClickCallback("onRightClick", player, clicked, clickedBlock);
  }

  /**
   * Left-click callback, triggered when the player left-clicks a block, entity
   * or air.
   *
   * @param player       The player that left-clicked
   * @param clicked      The clicked entity, null, if a block or air was
   *                     clicked
   * @param clickedBlock The block that was clicked, exclusive with clicked,
   *                     If this is not null, clicked is
   * @return True, if the item should be placed on cooldown, false otherwise
   */
  public boolean onLeftClick(Player player,
                             @Nullable Entity clicked,
                             @Nullable Block clickedBlock
  ) {
    return invokeClickCallback("onLeftClick", player, clicked, clickedBlock);
  }

  private boolean invokeClickCallback(String method, Player player,
                                      @Nullable Entity entity,
                                      @Nullable Block block
  ) {
    if (!script.hasMethod(method)) {
      return false;
    }

    Object clickedInput = block == null ? entity : block;

    // Default to true, as that will cause a weapon cooldown, which should
    // happen, if the method wasn't declared, or failed, or didn't return
    // a result
    return script.invoke(method, player, clickedInput)
        .asBoolean()
        .orElse(true);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load(CompoundTag tag) {
    setLevel(tag.getInt(TAG_LEVEL));
    setRemainingUses(tag.getInt(TAG_USES));

    if (tag.contains(TAG_COOLDOWN_OVERRIDE)) {
      setCooldownOverride(tag.getLong(TAG_COOLDOWN_OVERRIDE));
    } else {
      setCooldownOverride(NO_OVERRIDE);
    }

    script.invokeIfExists("onLoad", tag);
  }

  public void save(CompoundTag tag) {
    tag.putInt(TAG_LEVEL, getLevel());
    tag.putInt(TAG_USES, getRemainingUses());

    if (cooldownOverride != NO_OVERRIDE) {
      tag.putLong(TAG_COOLDOWN_OVERRIDE, getCooldownOverride());
    }

    script.invokeIfExists("onSave", tag);
  }

  /* -------------------------- UPDATE CALLBACK --------------------------- */

  public void onUpdate() {
    script.close();
  }
}