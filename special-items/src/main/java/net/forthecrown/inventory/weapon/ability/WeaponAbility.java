package net.forthecrown.inventory.weapon.ability;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.inventory.weapon.SwordRank;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.scripts.ExecResult;
import net.forthecrown.scripts.ExecResults;
import net.forthecrown.scripts.Script;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class WeaponAbility {
  public static final String TAG_LEVEL = "level";
  public static final String TAG_USES = "uses";
  public static final String TAG_COOLDOWN_OVERRIDE = "cooldownOverride";
  public static final String TAG_MAX_USES = "maxUses";

  public static final int START_LEVEL = 1;
  public static final int UNLIMITED_USES = -1;
  public static final int NO_OVERRIDE = -1;

  private int level = START_LEVEL;
  private int remainingUses = 0;
  private int maxUses = 0;

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

  public void write(TextWriter writer, SwordRank rank) {
    Component usesLeftText;

    if (remainingUses == UNLIMITED_USES) {
      usesLeftText = Component.text("Infinite", NamedTextColor.GREEN);
    } else {
      float useLimit = getMaxUses();
      float remaining = this.remainingUses;

      final float progress = (useLimit-remaining) / useLimit;
      TextColor color = Text.hsvLerp(
          progress,
          NamedTextColor.GREEN,
          NamedTextColor.RED
      );

      usesLeftText = Text.formatNumber(remaining).color(color);
    }

    writer.formattedLine("Uses left: {0, number}",
        NamedTextColor.GRAY,
        usesLeftText
    );

    var cooldownTicks = getCooldownTicks(rank);
    if (cooldownTicks > 0) {
      writer.formattedLine(
          "Cooldown: {0, time, -ticks -short}",
          NamedTextColor.GRAY,
          cooldownTicks
      );
    }
  }

  public long getCooldownTicks(SwordRank rank) {
    if (cooldownOverride != NO_OVERRIDE) {
      return cooldownOverride;
    }

    return script.invoke("getCooldown", rank)
        .map(o -> {
          if (o instanceof String str) {
            return UpgradeCooldown.parseTicks(str);
          }

          if (o instanceof Number number) {
            return number.longValue();
          }

          return type.getCooldown().get(rank);
        })
        .result()
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

  public void setMaxUses(int maxUses) {
    this.maxUses = maxUses;
    script.put("maxUses", maxUses);
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
                              RoyalSword sword,
                              @Nullable Entity clicked,
                              @Nullable Block clickedBlock
  ) {
    return invokeClickCallback("onRightClick", player, sword, clicked, clickedBlock);
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
                             RoyalSword sword,
                             @Nullable Entity clicked,
                             @Nullable Block clickedBlock
  ) {
    return invokeClickCallback("onLeftClick", player, sword, clicked, clickedBlock);
  }

  private boolean invokeClickCallback(String method,
                                      Player player,
                                      RoyalSword sword,
                                      @Nullable Entity entity,
                                      @Nullable Block block
  ) {
    if (!script.hasMethod(method)) {
      return false;
    }

    Object clickedInput = block == null ? entity : block;
    script.put("royalSword", sword);

    // Default to true, as that will cause a weapon cooldown, which should
    // happen, if the method wasn't declared, or failed, or didn't return
    // a result
    ExecResult<Object> result = script.invoke(method, player, clickedInput);
    ExecResult<Boolean> boolResult = ExecResults.toBoolean(result);

    script.remove("royalSword");
    return boolResult.result().orElse(false);
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load(CompoundTag tag) {
    setLevel(tag.getInt(TAG_LEVEL));
    setRemainingUses(tag.getInt(TAG_USES));

    if (tag.contains(TAG_MAX_USES)) {
      setMaxUses(tag.getInt(TAG_MAX_USES));
    } else {
      setMaxUses(getRemainingUses());
    }

    if (tag.contains(TAG_COOLDOWN_OVERRIDE)) {
      setCooldownOverride(tag.getLong(TAG_COOLDOWN_OVERRIDE));
    } else {
      setCooldownOverride(NO_OVERRIDE);
    }

    script.invoke("onLoad", tag);
  }

  public void save(CompoundTag tag) {
    tag.putInt(TAG_LEVEL, getLevel());
    tag.putInt(TAG_USES, getRemainingUses());
    tag.putInt(TAG_MAX_USES, getMaxUses());

    if (cooldownOverride != NO_OVERRIDE) {
      tag.putLong(TAG_COOLDOWN_OVERRIDE, getCooldownOverride());
    }

    script.invoke("onSave", tag);
  }

  /* -------------------------- UPDATE CALLBACK --------------------------- */

  public void onUpdate() {
    script.close();
  }
}