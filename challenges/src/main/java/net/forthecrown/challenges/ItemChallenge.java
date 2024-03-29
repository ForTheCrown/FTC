package net.forthecrown.challenges;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Slot;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

@Getter
@RequiredArgsConstructor
public class ItemChallenge implements Challenge {

  @Setter
  private ItemStack targetItem;

  private final Slot menuSlot;
  private final Reward reward;
  private final ImmutableList<Component> description;
  private final ResetInterval resetInterval;
  private final boolean wildcardAllowed;

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public Component getName() {
    return getTargetItem()
        .map(stack -> {
          var builder = Component.text()
              .content("Find ")
              .color(NamedTextColor.GOLD);

          if (stack.getAmount() < 2) {
            builder
                .append(Component.text("a "))
                .append(Text.itemDisplayName(stack)
                    .color(NamedTextColor.YELLOW)
                );
          } else {
            builder.append(Text.itemAndAmount(stack)
                .color(NamedTextColor.YELLOW)
            );
          }

          return builder.build();
        })
        .orElseGet(() -> Component.text("Get item"));
  }

  public Optional<ItemStack> getTargetItem() {
    return ItemStacks.isEmpty(targetItem)
        ? Optional.empty()
        : Optional.of(targetItem.clone());
  }

  @Override
  public StreakBasedValue getGoal() {
    return getTargetItem()
        .map(stack -> StreakBasedValue.fixed(stack.getAmount()))
        .orElse(StreakBasedValue.ONE);
  }

  @Override
  public float getGoal(User user) {
    return getTargetItem()
        .map(ItemStack::getAmount)
        .orElse(1);
  }

  @Override
  public StreakCategory getStreakCategory() {
    return StreakCategory.ITEMS;
  }

  @Override
  public void deactivate() {
    targetItem = null;
  }

  @Override
  public CompletionStage<String> activate(boolean resetting) {
    var manager = Challenges.getManager();
    var storage = manager.getStorage();

    var holder = manager
        .getChallengeRegistry()
        .getHolderByValue(this)
        .orElseThrow();

    var container = storage.loadContainer(holder);

    if (resetting) {
      var future = container.next(Challenges.RANDOM);

      return future.whenComplete((item, exception) -> {
        if (exception != null) {
          Loggers.getLogger().error(
              "Error getting random challenge item:",
              exception
          );

          return;
        }

        if (ItemStacks.notEmpty(item)) {
          container.getUsed().add(item);
          container.setActive(item);
          setTargetItem(item.clone());
        } else {
          container.setActive(null);
          setTargetItem(null);
        }

        storage.saveContainer(container);
      }).thenApply(itemStack -> {
        if (ItemStacks.isEmpty(itemStack)) {
          return "";
        }

        return ItemStacks.toNbtString(itemStack);
      });
    } else {
      setTargetItem(container.getActive());
    }

    return CompletableFuture.completedFuture("");
  }

  @Override
  public void trigger(Object input) {
    var player = ChallengeHandle.getPlayer(input);
    var item = getTargetItem().orElseThrow();

    final int targetAmount = item.getAmount();

    var inventory = player.getInventory();
    var found = findContained(inventory);

    if (found.totalFound() < targetAmount) {
      throw new IllegalArgumentException(String.format(
          "Not enough items, required %s, found %s",
          targetAmount, found.totalFound()
      ));
    }

    int remaining = targetAmount;

    for (ItemStack n : found.found()) {
      if (n.getAmount() < remaining) {
        remaining -= n.getAmount();
        n.setAmount(0);
      } else {
        n.subtract(remaining);
        break;
      }
    }

    Challenges.apply(this, holder -> {
      Challenges.getManager()
          .getEntry(player.getUniqueId())
          .addProgress(holder, item.getAmount());
    });
  }

  public MenuNode toInvOption() {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var baseItem = getTargetItem().orElse(null);

          if (baseItem == null) {
            return null;
          }

          var builder = ItemStacks.toBuilder(baseItem)
              .setName(getName())
              .clearLore()
              .addFlags(ItemFlag.HIDE_ENCHANTS);

          var entry = Challenges.getManager().getEntry(user.getUniqueId());

          if (entry.hasCompleted(this)) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                .addLore("&aAlready completed!");
          } else {
            builder.addLore("&6Click to take from your inventory!");
          }

          builder.addLore(
              "&7Get all the items to complete the challenge."
          );

          if (!getDescription().isEmpty()) {
            builder.addLore("");

            for (var c : getDescription()) {
              builder.addLore(replacePlaceholders(c, user));
            }
          }

          int streak = entry.getStreak(getStreakCategory()).get();

          if (!getReward().isEmpty(streak)) {
            var writer = TextWriters.buffered();
            writer.newLine();
            writer.newLine();

            writer.setFieldStyle(
                Style.style(NamedTextColor.GRAY)
            );
            writer.setFieldValueStyle(
                Style.style(NamedTextColor.GRAY)
            );

            getReward().write(writer, streak, user.getUniqueId());
            builder.addLore(writer.getBuffer());
          }

          if (baseItem.getType() == Material.AXOLOTL_BUCKET
              || baseItem.getType() == Material.TROPICAL_FISH_BUCKET
          ) {
            builder.addLoreRaw(Component.empty())
                .addLore("&7Any variant");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          click.setCooldownTime(5);

          if (ItemStacks.isEmpty(targetItem)) {
            return;
          }

          var entry = Challenges.getManager().getEntry(user);

          if (entry.hasCompleted(this)) {
            throw Exceptions.format("Challenge already completed");
          }

          var inventory = user.getInventory();
          var found = findContained(inventory);

          if (found.totalFound() < targetItem.getAmount()) {
            throw Exceptions.format("You do not have {0, item}", targetItem);
          }

          trigger(user);
          click.shouldReloadMenu(true);
        })

        .build();
  }

  private ItemSearchResult findContained(Inventory inv) {
    Set<ItemStack> found = new ObjectOpenHashSet<>();
    int foundCount = 0;

    var it = ItemStacks.nonEmptyIterator(inv);

    while (it.hasNext()) {
      var n = it.next();

      if (!matches(n)) {
        continue;
      }

      foundCount += n.getAmount();
      found.add(n);
    }

    if (wildcardAllowed && foundCount < targetItem.getAmount()) {
      ItemStack wildcard = findWildcard(inv);

      if (wildcard != null) {
        Set<ItemStack> items = new ObjectOpenHashSet<>();
        items.add(wildcard);
        return new ItemSearchResult(items, wildcard.getAmount(), true);
      }
    }

    return new ItemSearchResult(found, foundCount, false);
  }

  private ItemStack findWildcard(Inventory inventory) {
    var it = ItemStacks.nonEmptyIterator(inventory);

    while (it.hasNext()) {
      var n = it.next();
      var meta = n.getItemMeta();

      if (ItemStacks.hasTagElement(meta, "shop_wildcard")) {
        return n;
      }
    }

    return null;
  }

  private boolean matches(ItemStack item) {
    return getTargetItem()
        .map(target -> {
          if (target.isSimilar(item)) {
            return true;
          }

          if (target.getType() != item.getType()) {
            return false;
          }

          // We already ensured that the items are of the exact same
          // type, so we only need to check target item
          var typeName = target.getType().name();

          // Axolotl and fish buckets need to be singled out
          // due to material being able to represent different
          // variants of the mob
          if (validBucket(typeName)) {
            return true;
          }

          var sMeta = target.getItemMeta();
          var iMeta = item.getItemMeta();

          // Skull items just need to the same texture,
          // nothing else matters
          if (sMeta instanceof SkullMeta skullMeta) {
            SkullMeta iSkullMeta = (SkullMeta) iMeta;
            var itemProfile = iSkullMeta.getPlayerProfile();
            var targetProfile = skullMeta.getPlayerProfile();

            if (itemProfile == null || targetProfile == null) {
              // Both must be null, if either is not null, then there's an issue,
              // Null profile heads may be heads belonging to actual players,
              // aka, the non-custom heads
              return itemProfile == targetProfile;
            }

            var itemTextures = itemProfile.getTextures();
            var targetTextures = targetProfile.getTextures();

            return texturesMatch(targetTextures, itemTextures);
          }

          // isSimilar check happens above, so we can return false here
          return false;
        })

        .orElse(false);
  }

  private static boolean validBucket(String typeName) {
    if (!typeName.contains("BUCKET")) {
      return false;
    }

    return typeName.contains("AXOLOTL")
        || typeName.contains("TROPICAL")
        || typeName.contains("TADPOLE");
  }

  private static boolean texturesMatch(PlayerTextures t1, PlayerTextures t2) {
    var url1 = t1.getSkin();
    var url2 = t2.getSkin();
    return Objects.equals(url1, url2);
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ItemChallenge challenge)) {
      return false;
    }

    return getResetInterval() == challenge.getResetInterval()
        && Objects.equals(menuSlot, challenge.menuSlot)
        && Objects.equals(description, challenge.description)
        && Objects.equals(reward, challenge.reward);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getResetInterval(),
        menuSlot,
        description,
        reward
    );
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  private record ItemSearchResult(Set<ItemStack> found,
                                  int totalFound,
                                  boolean wildcardUsed
  ) {

  }
}