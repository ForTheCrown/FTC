package net.forthecrown.user.data;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserRank implements ComponentLike {
  private final RankTier tier;
  private final Component truncatedPrefix;
  private final String genderEquivalentKey;
  private final Slot menuSlot;
  private final ImmutableList<Component> description;
  private final boolean defaultTitle;
  private final boolean hidden;

  /** This rank's menu node, lazily initialized */
  private MenuNode menuNode;

  private UserRank(Builder builder) {
    this.tier = Objects.requireNonNull(builder.tier);
    this.truncatedPrefix = builder.truncatedPrefix;
    this.genderEquivalentKey = builder.genderEquivalentKey;
    this.menuSlot = builder.menuSlot;
    this.description = builder.description.build();
    this.defaultTitle = builder.defaultTitle;
    this.hidden = builder.hidden;

    if (defaultTitle) {
      Objects.requireNonNull(
          menuSlot,
          "Default ranks cannot have null menu slots"
      );
    }

    if (menuSlot != null) {
      int r = menuSlot.getRow();
      int c = menuSlot.getColumn();

      // Ensure given slot is in the upper 2x7 area and
      // not in the extra rank array below
      Validate.isTrue(r > 0 && r < 3, "Invalid row %s", r);
      Validate.isTrue(c > 0 && c < 8, "Invalid column %s", c);
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
          var titles = user.getTitles();
          boolean has = titles.hasTitle(this);
          boolean active = titles.getTitle() == this;

          if (hidden && !has) {
            return null;
          }

          var builder = ItemStacks.builder(
              has ? Material.GLOBE_BANNER_PATTERN : Material.PAPER
          );

          builder.setName(getTruncatedPrefix())
              .addFlags(
                  ItemFlag.HIDE_POTION_EFFECTS,
                  ItemFlag.HIDE_ATTRIBUTES,
                  ItemFlag.HIDE_DYE
              );

          description.forEach(builder::addLore);

          if (active) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                .addFlags(ItemFlag.HIDE_ENCHANTS);
          }

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          var titles = user.getTitles();
          boolean has = titles.hasTitle(this);
          boolean active = titles.getTitle() == this;

          if (hidden && !has) {
            return;
          }

          if (!has) {
            throw Exceptions.DONT_HAVE_TITLE;
          }

          if (active) {
            throw Exceptions.ALREADY_YOUR_TITLE;
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

  @Getter @Setter
  @Accessors(fluent = true, chain = true)
  public static class Builder {
    private RankTier tier;
    private Component truncatedPrefix;
    private String genderEquivalentKey;
    private Slot menuSlot;

    private boolean defaultTitle = false;
    private boolean hidden = false;

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