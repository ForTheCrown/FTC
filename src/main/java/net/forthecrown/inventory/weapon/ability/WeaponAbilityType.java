package net.forthecrown.inventory.weapon.ability;

import static net.forthecrown.inventory.weapon.ability.WeaponAbility.START_LEVEL;
import static net.forthecrown.inventory.weapon.ability.WeaponAbility.UNLIMITED_USES;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.InventoryStorage;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.script2.Script;
import net.forthecrown.core.script2.ScriptSource;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.inventory.weapon.RoyalSword;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.user.User;
import net.forthecrown.user.UserTeleport.Type;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

@Getter
@RequiredArgsConstructor
public class WeaponAbilityType {
  private final ImmutableList<ItemStack> recipe;
  private final ItemStack item;

  private final Component displayName;
  private final ImmutableList<Component> description;

  private final ScriptSource source;
  private final UpgradeCooldown cooldown;
  private final String[] args;

  private final NamespacedKey advancementKey;

  private final UseLimit limit;

  private final AbilityTrialArea trialArea;

  private final IntList levelRequirements;

  public WeaponAbilityType(Builder builder) {
    this.recipe = builder.items.build();
    this.item = builder.item;

    this.displayName = Objects.requireNonNull(builder.displayName);
    this.description = builder.description.build();

    this.limit = Objects.requireNonNull(builder.limit);
    this.cooldown = Objects.requireNonNull(builder.cooldown);

    this.source = Objects.requireNonNull(builder.source);
    this.args = Objects.requireNonNull(builder.args);

    this.advancementKey = builder.advancementKey;
    this.levelRequirements = builder.levelRequirements;
    this.trialArea = builder.trialArea;

    // Check arguments
    Preconditions.checkArgument(
        ItemStacks.notEmpty(item),
        "Cannot have empty item"
    );
  }

  public static Builder builder() {
    return new Builder();
  }

  public Optional<String> enterTrialArea(User user) {
    if (trialArea == null) {
      return Optional.empty();
    }

    String inventoryStore = SwordAbilityManager.getInstance()
        .getTrialInventoryStore();

    if (!Strings.isNullOrEmpty(inventoryStore)) {
      var store = InventoryStorage.getStorage();

      if (store.hasStoredInventory(user.getPlayer(), inventoryStore)) {
        Loggers.getLogger().error(
            "{} already has inventory saved in {}, cannot enter trial area",
            user, inventoryStore
        );

        return Optional.of(
            "Internal error with inventory separation, tell a staff member"
        );
      }

      store.storeInventory(user.getPlayer(), inventoryStore, true);
    }

    trialArea.enter(user, this);
    return Optional.empty();
  }

  private WeaponAbility create() {
    var script = Script.of(source);
    script.compile(args);
    script.put("cooldown", cooldown);
    script.put("abilityType", this);

    script.eval().throwIfError();

    return new WeaponAbility(this, script);
  }

  public WeaponAbility create(User user, int totalUses) {
    int limit = getLimit().get(user);
    var ability = create();
    ability.setRemainingUses(limit);
    ability.setMaxUses(limit);
    ability.setLevel(getLevel(totalUses));
    return ability;
  }

  public WeaponAbility load(CompoundTag tag) {
    var ability = create();
    ability.load(tag);
    return ability;
  }

  public int getMaxLevel() {
    return levelRequirements.size() + 1;
  }

  public int getLevel(int totalUses) {
    if (levelRequirements.isEmpty()) {
      return START_LEVEL;
    }

    for (int i = levelRequirements.size() - 1; i >= 0; i--) {
      int req = levelRequirements.getInt(i);

      if (totalUses >= req) {
        return 2 + i;
      }
    }

    return START_LEVEL;
  }

  public int getUpgradeUses(int level) {
    if (levelRequirements.isEmpty()) {
      return -1;
    }
    int index = level - 1;

    if (index < 0 || index >= levelRequirements.size()) {
      return -1;
    }

    return levelRequirements.getInt(index);
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

    writer.field("Max Level", Text.format("{0, number, -roman}", getMaxLevel()));
    writer.field("Uses", Text.formatNumber(limit.get(user)));

    writer.newLine();
    writer.newLine();

    writer.field("Levelling requirements", "");
    var prefixed = writer.withPrefix(
        Component.text("- ", NamedTextColor.GRAY)
    );

    int level = 2;
    for (int i: levelRequirements) {
      prefixed.field(
          "Level " + level++,
          Text.format("{0, number} Total Uses", i)
      );
    }

    writer.newLine();
    writer.newLine();

    writer.field("Cooldown", cooldown);

    long change = cooldown.getCooldownChange();
    if (change > 0) {
      writer.field("Cooldown decrease",
          PeriodFormat.of(Time.ticksToMillis(change))
              .withShortNames()
      );

      writer.line(
          "^ Cooldown decrease per sword rank",
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

  public record AbilityTrialArea(Location location,
                                 TrialInfoNode info,
                                 boolean giveSword,
                                 Script script,
                                 int level,
                                 long cooldownOverride
  ) {

    public void start() {
      if (script != null) {
        script.compile().eval();
      }
    }

    public void close() {
      if (script != null) {
        script.close();
      }
    }

    public void enter(User user, WeaponAbilityType type) {
      user.createTeleport(() -> location, Type.TELEPORT)
          .setDelayed(false)
          .setSilent(true)
          .setAsync(false)
          .start();

      if (giveSword) {
        ItemStack item
            = ExtendedItems.ROYAL_SWORD.createItem(user.getUniqueId());

        RoyalSword sword = ExtendedItems.ROYAL_SWORD.get(item);
        assert sword != null;

        // Create ability, give it unlimited uses and a
        // constant 2-second cooldown
        var ability = type.create(user, 0);
        ability.setRemainingUses(UNLIMITED_USES);
        ability.setCooldownOverride(cooldownOverride);
        ability.setLevel(level);

        sword.setAbility(ability);
        sword.update(item);

        user.getInventory().addItem(item);
      }

      if (info != null) {
        var config = TextReplacementConfig.builder()
            .matchLiteral("%ability")
            .replacement(type.fullDisplayName(user))
            .build();

        info.start(user, config);
      }
    }
  }

  @Setter
  @Getter
  @RequiredArgsConstructor
  public static class TrialInfoNode {
    private final Component text;
    private final long delay;

    private TrialInfoNode next;

    public void start(User user, TextReplacementConfig config) {
      Tasks.runLater(() -> {
        if (!user.isOnline()) {
          return;
        }

        user.sendMessage(text.replaceText(config));

        if (next != null) {
          next.start(user, config);
        }
      }, delay);
    }
  }

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  @RequiredArgsConstructor
  public static class Builder {

    Component displayName;
    final ImmutableList.Builder<Component> description
        = ImmutableList.builder();

    ItemStack item;
    final ImmutableList.Builder<ItemStack> items = ImmutableList.builder();

    ScriptSource source;
    String[] args;

    UpgradeCooldown cooldown;
    UseLimit limit;

    NamespacedKey advancementKey;

    AbilityTrialArea trialArea;

    final IntList levelRequirements = new IntArrayList();

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