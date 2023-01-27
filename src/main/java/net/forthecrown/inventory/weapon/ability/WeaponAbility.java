package net.forthecrown.inventory.weapon.ability;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptResult;
import net.forthecrown.inventory.weapon.RoyalSword;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
public class WeaponAbility {
  public static final String TAG_LEVEL = "level";
  public static final String TAG_USES = "uses";

  public static final int START_LEVEL = 1;

  protected int level = START_LEVEL;
  protected int uses = 0;

  private final WeaponAbilityType type;
  private final Script script;

  public WeaponAbility(WeaponAbilityType type, Script script) {
    this.type = type;
    this.script = script;

    assert script.isCompiled() : "Script not compiled when given to ability";
    setLevel(START_LEVEL);
    setUses(0);
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

    float useLimit = getType().getLimit().get(user);
    float uses = this.uses;

    final float progress = uses / useLimit;
    TextColor color = TextColor.lerp(
        progress,
        NamedTextColor.GREEN,
        NamedTextColor.RED
    );

    writer.formattedLine("Uses left: {0, number}",
        NamedTextColor.GRAY,

        Text.formatNumber(useLimit - uses)
            .color(color)
    );
  }

  public long getCooldownTicks(SwordRank rank) {
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

  public void setUses(int uses) {
    this.uses = Math.max(0, uses);
    script.put("uses", this.uses);
  }

  /* ----------------------------- CALLBACKS ------------------------------ */

  /**
   * Right-click callback, triggered when the player right-clicks, aka
   * interacts, with a block, entity or air.
   *
   * @param player       The player that right-clicked
   * @param clicked      The right-clicked entity,
   * @param clickedBlock
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
   * @param clickedBlock
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
    setUses(tag.getInt(TAG_USES));

    script.invokeIfExists("onLoad", tag);
  }

  public void save(CompoundTag tag) {
    tag.putInt(TAG_LEVEL, getLevel());
    tag.putInt(TAG_USES, getUses());

    script.invokeIfExists("onSave", tag);
  }

  /* -------------------------- UPDATE CALLBACK --------------------------- */

  public void onUpdate(ItemStack item, ItemMeta meta, RoyalSword royalSword) {
  }
}