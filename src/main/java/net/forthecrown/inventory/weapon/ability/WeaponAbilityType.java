package net.forthecrown.inventory.weapon.ability;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class WeaponAbilityType {
  private final ImmutableList<ItemStack> recipe;
  private final ItemStack item;
  private final int maxLevel;

  private final Component displayName;
  private final ImmutableList<Component> description;

  private final ScriptSource source;
  private final UpgradeCooldown cooldown;
  private final String[] args;

  private final NamespacedKey advancementKey;

  private final UseLimit limit;

  public WeaponAbilityType(Builder builder) {
    this.recipe = builder.items.build();
    this.item = builder.item;
    this.maxLevel = builder.maxLevel;

    this.displayName = Objects.requireNonNull(builder.displayName);
    this.description = builder.description.build();

    this.limit = Objects.requireNonNull(builder.limit);
    this.cooldown = Objects.requireNonNull(builder.cooldown);

    this.source = Objects.requireNonNull(builder.source);
    this.args = Objects.requireNonNull(builder.args);

    this.advancementKey = builder.advancementKey;

    // Check arguments
    Preconditions.checkArgument(
        ItemStacks.notEmpty(item),
        "Cannot have empty item"
    );
  }

  public static Builder builder() {
    return new Builder();
  }

  public WeaponAbility create() {
    var script = Script.of(source);
    script.compile(args);
    script.put("cooldown", cooldown);
    script.put("abilityType", this);

    script.eval().throwIfError();

    return new WeaponAbility(this, script);
  }

  public WeaponAbility load(CompoundTag tag) {
    var ability = create();
    ability.load(tag);
    return ability;
  }

  public ItemStack getItem() {
    return item.clone();
  }

  public Component fullDisplayName(User user) {
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.DARK_GRAY));
    writer.setFieldValueStyle(Style.style(NamedTextColor.DARK_GRAY));

    writeHover(writer, user);

    return displayName
        .colorIfAbsent(NamedTextColor.YELLOW)
        .hoverEvent(writer.asComponent());
  }

  public ItemStack createDisplayItem(User user) {
    var builder = ItemStacks.toBuilder(getItem());
    builder.setName(getDisplayName().colorIfAbsent(NamedTextColor.YELLOW));

    var writer = TextWriters.loreWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.setFieldValueStyle(Style.style(NamedTextColor.GRAY));

    writeHover(writer, user);
    builder.addLore(writer.getLore());

    builder.addFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

    return builder.build();
  }

  private void writeHover(TextWriter writer, User user) {
    description.forEach(component -> {
      writer.line(component.colorIfAbsent(NamedTextColor.GRAY));
    });

    if (!description.isEmpty()) {
      writer.newLine();
      writer.newLine();
    }

    var adv = getAdvancement();
    if (adv != null) {
      writer.field("Requires",
          adv.displayName()
              .color(
                  user.getPlayer().getAdvancementProgress(adv).isDone()
                      ? NamedTextColor.YELLOW
                      : NamedTextColor.GRAY
              )
      );
    }

    writer.field("Max Level", Text.format("{0, number, -roman}", maxLevel));
    writer.field("Uses", Text.formatNumber(limit.get(user)));

    writer.field("Cooldown", cooldown);

    long change = cooldown.getCooldownChange();
    if (change > 0) {
      writer.field("Cooldown decrease",
          PeriodFormat.of(Time.ticksToMillis(change))
              .withShortNames()
      );

      writer.line(
          "^ Cooldown decrease per sword level",
          NamedTextColor.DARK_GRAY
      );
    }
  }

  public Advancement getAdvancement() {
    return advancementKey == null
        ? null
        : Bukkit.getAdvancement(advancementKey);
  }

  /* ------------------------- OBJECT OVERRIDES --------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WeaponAbilityType that)) {
      return false;
    }

    return getMaxLevel() == that.getMaxLevel()
        && getRecipe().equals(that.getRecipe())
        && getItem().equals(that.getItem())
        && Objects.equals(getAdvancementKey(), that.getAdvancementKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getRecipe(),
        getItem(),
        getMaxLevel(),
        getAdvancementKey()
    );
  }

  /* ------------------------------ BUILDER ------------------------------- */

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor
  public static class Builder {
    final ImmutableList.Builder<ItemStack> items = ImmutableList.builder();

    final ImmutableList.Builder<Component> description
        = ImmutableList.builder();

    ItemStack item;
    int maxLevel;

    Component displayName;

    ScriptSource source;
    UpgradeCooldown cooldown;
    String[] args;

    NamespacedKey advancementKey;

    UseLimit limit;

    public Builder addItem(ItemStack item) {
      Validate.isTrue(ItemStacks.notEmpty(item), "Empty item given");

      items.add(item);
      return this;
    }

    public Builder addDesc(Component c) {
      description.add(c);
      return this;
    }

    public WeaponAbilityType build() {
      return new WeaponAbilityType(this);
    }
  }
}