package net.forthecrown.titles;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Slot;
import net.forthecrown.registry.Registries;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserRank implements ComponentLike {

  /** The rank's tier */
  private final RankTier tier;

  /** The rank's prefix without the trailing space */
  private final Component truncatedPrefix;

  /** The registry key of the opposite gender variant of this rank */
  @Pattern(Registries.VALID_KEY_REGEX)
  private final String genderEquivalentKey;

  /** This rank's menu slot, may be null */
  private final Slot menuSlot;

  /** Description text */
  private final ImmutableList<Component> description;

  /**
   * If true, it means this rank comes free with the tier, otherwise, this
   * rank will have to be earned in some other way
   */
  private final boolean defaultTitle;

  /**
   * If true, means this rank will not be displayed until a user has been given
   * this rank
   */
  private final boolean hidden;

  /**
   * Determines if this rank can be reloaded, aka, if the user ranks are
   * reloaded, then this rank will be unregistered
   */
  private final boolean reloadable;

  /** This rank's menu node, lazily initialized */
  private MenuNode menuNode;

  private UserRank(Builder builder) {
    this.tier = Objects.requireNonNull(builder.tier);
    this.truncatedPrefix = Objects.requireNonNull(builder.truncatedPrefix);

    this.genderEquivalentKey = builder.genderEquivalentKey;
    this.menuSlot = builder.menuSlot;
    this.description = builder.description.build();
    this.defaultTitle = builder.defaultTitle;
    this.hidden = builder.hidden;
    this.reloadable = builder.reloadable;

    if (!Strings.isNullOrEmpty(genderEquivalentKey)) {
      Registries.ensureValidKey(genderEquivalentKey);
    }

    if (defaultTitle) {
      Objects.requireNonNull(
          menuSlot,
          "Default ranks cannot have null menu slots"
      );
    }

    if (menuSlot != null) {
      int r = menuSlot.getY();
      int c = menuSlot.getX();

      // Ensure given slot is in the upper 2x7 area and
      // not in the extra rank array below
      Preconditions.checkArgument(r > 0 && r < 3, "Invalid row %s", r);
      Preconditions.checkArgument(c > 0 && c < 8, "Invalid column %s", c);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public Component getPrefix() {
    return getTruncatedPrefix().append(Component.space());
  }

  @Override
  public @NotNull Component asComponent() {
    return getTruncatedPrefix().hoverEvent(
        TextJoiner.onNewLine()
            .setColor(NamedTextColor.GRAY)
            .add(description)
            .asComponent()
    );
  }

  public UserRank getGenderEquivalent() {
    return Strings.isNullOrEmpty(genderEquivalentKey)
        ? null
        : UserRanks.REGISTRY.orNull(getGenderEquivalentKey());
  }

  public MenuNode getMenuNode() {
    if (menuNode != null) {
      return menuNode;
    }

    return menuNode = MenuNode.builder()
        .setItem((user, context) -> {
          UserTitles titles = user.getComponent(UserTitles.class);

          boolean has = titles.hasTitle(this);
          boolean active = titles.getTitle() == this;

          // If hidden, and the user doesn't have it, don't display
          if (hidden && !has) {
            return null;
          }

          var builder = ItemStacks.builder(
              has ? Material.GLOBE_BANNER_PATTERN : Material.PAPER
          );

          builder.setName(getTruncatedPrefix())
              .addFlags(
                  ItemFlag.HIDE_ITEM_SPECIFICS,
                  ItemFlag.HIDE_ATTRIBUTES,
                  ItemFlag.HIDE_DYE
              );

          description.forEach(builder::addLore);

          if (active) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                .addFlags(ItemFlag.HIDE_ENCHANTS)
                .addLore("&aYour active title!");
          }

          if (has) {
            builder.addLore("&7Click to set as your rank");
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          UserTitles titles = user.getComponent(UserTitles.class);

          boolean has = titles.hasTitle(this);
          boolean active = titles.getTitle() == this;

          if (hidden && !has) {
            return;
          }

          if (!has) {
            throw Exceptions.create("You don't have this title.");
          }

          if (active) {
            throw Exceptions.create("This is already your title");
          }

          titles.setTitle(this);
          click.shouldReloadMenu(true);

          user.sendMessage(
              Text.format("Set your rank to &f{0}&r!",
                  NamedTextColor.GRAY,
                  this
              )
          );
        })

        .build();
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public int hashCode() {
    return Objects.hash(tier, getTruncatedPrefix());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof UserRank rank)) {
      return false;
    }

    return Objects.equals(rank.getTier(), getTier())
        && Objects.equals(rank.getPrefix(), getPrefix());
  }

  /* ----------------------------- SUB CLASS ------------------------------ */

  @Getter
  @Setter
  @Accessors(fluent = true, chain = true)
  public static class Builder {
    private RankTier tier;
    private Component truncatedPrefix;

    @Pattern(Registries.VALID_KEY_REGEX)
    private String genderEquivalentKey;

    private Slot menuSlot;

    private boolean defaultTitle = false;
    private boolean hidden = false;

    private boolean reloadable = false;

    private ImmutableList.Builder<Component> description
        = ImmutableList.builder();

    public Builder prefix(String s) {
      return truncatedPrefix(Text.renderString(s));
    }

    public Builder slot(int x, int y) {
      return menuSlot(Slot.of(x, y));
    }

    public Builder asDefault() {
      return defaultTitle(true);
    }

    public Builder addDesc(String d) {
      return addDesc(Text.valueOf(d));
    }

    public Builder addDesc(Component c) {
      description.add(c);
      return this;
    }

    public UserRank build() {
      return new UserRank(this);
    }

    public UserRank registered(String key) {
      var rank = new UserRank(this);
      UserRanks.REGISTRY.register(key, rank);
      return rank;
    }
  }
}