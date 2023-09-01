package net.forthecrown.inventory.weapon;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.inventory.ExtendedItem;
import net.forthecrown.inventory.weapon.ability.SwordAbilityManager;
import net.forthecrown.inventory.weapon.ability.WeaponAbility;
import net.forthecrown.inventory.weapon.ability.WeaponAbilityType;
import net.forthecrown.inventory.weapon.goals.WeaponGoal;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.IntTag;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;

/**
 * A RoyalSword is a special in-game sword, this is the class which represents it... shocking, I
 * know.
 */
@Getter
public class RoyalSword extends ExtendedItem {
  private static final Logger LOGGER = Loggers.getLogger();

  public static final String
      TAG_RANK = "rank",
      TAG_LAST_FLAVOR = "lastFlavorChange",
      TAG_EXTRA_DATA = "extraData",
      TAG_GOALS = "goals",
      TAG_ABILITY = "ability",
      TAG_ABILITY_TYPE = "abilityType",
      TAG_ABILITY_USES = "abilityUses";

  public static final Component BORDER = Component.text(
      "                              ",
      Style.style()
          .color(NamedTextColor.GRAY)
          .decorate(TextDecoration.STRIKETHROUGH)
          .decoration(TextDecoration.ITALIC, false)
          .build()
  );

  @Setter
  private SwordRank rank;
  @Setter
  private SwordRank lastFlavorChange;

  @Setter
  private WeaponAbility ability;

  private final Object2IntMap<String> progress
      = new Object2IntOpenHashMap<>();

  private final Object2IntMap<String> abilityUses
      = new Object2IntOpenHashMap<>();

  public RoyalSword(RoyalSwordType type, CompoundTag tag) {
    super(type, tag);
    load(tag);
  }

  public RoyalSword(RoyalSwordType type, UUID owner) {
    super(type, owner);
  }

  @Override
  protected void onUpdate(ItemStack item, ItemMeta meta) {
    if (rank == null) {
      incrementRank(item);
    }

    if (ability != null) {
      ability.onUpdate();
    }
  }

  public void onAbilityUse(Holder<WeaponAbilityType> holder) {
    int currentUses = this.abilityUses.getOrDefault(holder.getKey(), 0);
    currentUses++;
    abilityUses.put(holder.getKey(), currentUses);
  }

  public void damage(Player killer, EntityDamageByEntityEvent event, ItemStack item) {
    var goals = rank.getGoals();

    if (goals.isEmpty()) {
      return;
    }

    for (var goal : goals.values()) {
      if (!goal.test(event)) {
        continue;
      }

      int goalProgress = this.progress.getInt(goal.getName());
      int newProgress = goalProgress + goal.getIncrementAmount(event);

      this.progress.put(
          goal.getName(),
          Math.min(goal.getGoal(), newProgress)
      );
    }

    //If we should rank up... rank up
    if (shouldRankUp()) {
      boolean rankSet = rank != null;
      incrementRank(item);

      if (rankSet) {
        levelUpEffects(killer, killer.getLocation());
      }
    }

    //Always update item
    update(item);
  }

  public void incrementRank(ItemStack item) {
    if (getRank() != null
        && getRank().getNext() == null
    ) {
      return;
    }

    // If the current rank is null, then that
    // means the sword has not been assigned
    // a rank yet, so it's gotta be the first
    // rank
    SwordRank next = (getRank() == null) ?
        SwordRanks.RANKS[0]
        : getRank().getNext();

    if (next == null) {
      return;
    }

    if (next.hasFlavorText()) {
      lastFlavorChange = next;
    }

    progress.clear();
    setRank(next);

    ItemMeta meta = item.getItemMeta();
    next.apply(this, item, meta);
    item.setItemMeta(meta);
  }

  private void levelUpEffects(Player player, Location l) {
    if (rank == null) {
      return;
    }

    World w = l.getWorld();
    //Play sounds
    w.playSound(l, Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
    w.playSound(l, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);

    //Spawn totem particles
    for (int i = 0; i <= 5; i++) {
      Tasks.runLater(() -> {
        Particle.TOTEM.builder()
            .location(l.add(0, 2, 0))
            .count(30)
            .offset(0.2D, 0.1D, 0.2D)
            .extra(0.275d)
            .spawn();
      }, i * 5L);
    }

    player.sendMessage(
        Text.format("Your sword was upgraded to Rank {0, number, -roman}",
            NamedTextColor.GRAY, rank.getViewerRank()
        )
    );
  }

  /**
   * Tests to see if the sword should be ranked up
   *
   * @return Whether to rank up the sword.
   */
  public boolean shouldRankUp() {
    if (rank == null) {
      return true;
    }

    // Next rank is null or we have no
    // goals
    if (rank.getNext() == null
        || rank.getGoals().isEmpty()
    ) {
      return false;
    }

    var goals = rank.getGoals();
    for (var g : goals.values()) {
      if (progress.getInt(g.getName()) < g.getGoal()) {
        return false;
      }
    }

    return true;
  }

  public int getTotalUses(Holder<WeaponAbilityType> holder) {
    return abilityUses.getOrDefault(holder.getKey(), 0);
  }

  /* ------------------------------- LORE --------------------------------- */

  @Override
  protected void writeLore(TextWriter writer) {
    writer.formattedLine("Rank {0, number, -roman}",
        NamedTextColor.GRAY,
        getRank().getViewerRank()
    );

    if (lastFlavorChange != null) {
      writer.line(BORDER);
      lastFlavorChange.writeFlavor(writer);
      writer.line(BORDER);
    }

    if (ability != null && hasPlayerOwner()) {
      writer.line("Ability Upgrade: (", NamedTextColor.GRAY);
      writer.write(ability.displayName());

      writer.formatted(" {0, number, -roman}",
          NamedTextColor.GRAY,
          ability.getLevel()
      );

      writer.write(")", NamedTextColor.GRAY);

      var prefixed = writer.withPrefix(
          Component.text("• ", NamedTextColor.GRAY)
      );

      ability.write(prefixed, getRank());

      SwordAbilityManager.getInstance().getRegistry()
          .getHolderByValue(ability.getType())

          .ifPresent(holder -> {
            var type = holder.getValue();
            int maxLevel = type.getMaxLevel();

            if (ability.getLevel() >= maxLevel) {
              return;
            }

            int totalUses = getTotalUses(holder);
            int levelUpUses = type.getUpgradeUses(ability.getLevel());

            if (levelUpUses == -1) {
              return;
            }

            int remainingUses = levelUpUses - totalUses;
            prefixed.formattedLine(
                "Level up after {0, number} more uses",
                NamedTextColor.GRAY,
                remainingUses
            );
          });

      writer.newLine();
      writer.newLine();
    }

    if (!rank.getGoals().isEmpty()) {
      writeGoals(writer);
    }

    if (getRank().getNext() != null) {
      getRank().getNext().writePreview(writer);
    }

    if (hasPlayerOwner()) {
      var owner = getOwnerUser();

      if (owner.isOnline() && !owner.hasPermission("ftc.donator1")) {
        writer.formattedLine(
            "Only donators can upgrade Royal Swords beyond rank {0, number, -roman}",
            NamedTextColor.DARK_GRAY,
            SwordRanks.DONATOR_RANK
        );
      }

      writer.formattedLine("Owner: {0, user}",
          NamedTextColor.DARK_GRAY,
          owner
      );
    }

    SwordRank prev = getRank();

    while (prev != null) {
      if (!prev.hasStatusDisplay()) {
        prev = prev.getPrevious();
        continue;
      }

      prev.writeStatus(writer);
      break;
    }
  }

  protected void writeGoals(TextWriter writer) {
    var goals = getRank().getGoals();

    if (goals.size() == 1) {
      var goal = goals.values()
          .iterator()
          .next();

      writer.formattedLine("Goal: {0, number}/{1, number} {2}",
          NamedTextColor.AQUA,

          progress.getOrDefault(goal.getName(), 0),

          goal.getGoal(),
          goal.loreDisplay()
      );

      return;
    }

    writer.line("Goals:", NamedTextColor.AQUA);
    TextWriter gWriter = writer.withPrefix(Component.text("• ", NamedTextColor.AQUA));

    for (var g : goals.values()) {
      int progress = this.progress.getInt(g.getName());

      gWriter.formattedLine("{0}/{1} {2}",
          NamedTextColor.AQUA,

          progress,
          g.getGoal(),
          g.loreDisplay()
      );
    }
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void load(CompoundTag tag) {
    if (tag.contains(TAG_LAST_FLAVOR)) {
      int lastFlavor = tag.getInt(TAG_LAST_FLAVOR);
      setLastFlavorChange(SwordRanks.RANKS[lastFlavor]);
    }

    if (tag.contains(TAG_ABILITY_TYPE)) {
      SwordAbilityManager.getInstance()
          .getRegistry()
          .readTag(tag.get(TAG_ABILITY_TYPE))

          .ifPresentOrElse(type -> {
            CompoundTag abilityTag = tag.getCompound(TAG_ABILITY);
            setAbility(type.load(abilityTag));
            LOGGER.debug("Loaded ability");
          }, () -> {
            LOGGER.warn("Unknown ability type {} found in {}'s sword",
                tag.get(TAG_ABILITY_TYPE), getOwner()
            );
          });
    }

    if (tag.contains(TAG_ABILITY_USES)) {
      CompoundTag usesTag = tag.getCompound(TAG_ABILITY_USES);
      abilityUses.clear();
      usesTag.keySet().forEach(s -> {
        abilityUses.put(s, usesTag.getInt(s));
      });

      LOGGER.debug("Loaded total uses");
    }

    if (!tag.contains(TAG_RANK)) {
      return;
    }

    this.rank = SwordRanks.RANKS[tag.getInt(TAG_RANK)];

    if (tag.contains(TAG_GOALS)) {
      for (var e : tag.getCompound(TAG_GOALS).entrySet()) {
        WeaponGoal g = rank.getGoals().get(e.getKey());

        if (g == null) {
          Loggers.getLogger().warn("Unknown goal '{}', found in sword, owner={}",
              e.getKey(), getOwner()
          );
          continue;
        }

        int value = ((IntTag) e.getValue()).intValue();

        if (value == 0) {
          continue;
        }

        progress.put(g.getName(), value);
      }
    }
  }

  @Override
  public void save(CompoundTag tag) {
    if (rank != null) {
      tag.putInt(TAG_RANK, rank.getIndex());

      var goals = rank.getGoals();

      if (!goals.isEmpty()) {
        CompoundTag goalTag = BinaryTags.compoundTag();

        for (var g : goals.values()) {
          goalTag.putInt(g.getName(), progress.getInt(g.getName()));
        }

        tag.put(TAG_GOALS, goalTag);
      }
    }

    if (lastFlavorChange != null) {
      tag.putInt(TAG_LAST_FLAVOR, lastFlavorChange.getIndex());
    }

    if (!abilityUses.isEmpty()) {
      CompoundTag usesTag = BinaryTags.compoundTag();
      abilityUses.forEach(usesTag::putInt);
      tag.put(TAG_ABILITY_USES, usesTag);
    }

    if (ability != null) {
      ability.getType()
          .getHolder()
          .ifPresentOrElse(holder -> {
            CompoundTag abilityTag = BinaryTags.compoundTag();
            ability.save(abilityTag);

            tag.put(TAG_ABILITY, abilityTag);
            tag.putString(TAG_ABILITY_TYPE, holder.getKey());
          }, () -> {
            LOGGER.warn(
                "Unregistered weapon ability found in {} sword, "
                    + "displayName='{}'",

                getOwnerUser(),
                Text.plain(ability.getType().getDisplayName())
            );
          });
    }
  }
}