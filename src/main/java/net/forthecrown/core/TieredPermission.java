package net.forthecrown.core;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.user.User;
import org.apache.commons.lang3.Range;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;

public class TieredPermission {

  /**
   * The prefix used for permissions
   */
  @Getter
  private final String prefix;

  /**
   * The range of supported integer values for tiers
   */
  @Getter
  private final IntList tiers;

  private final PermissionTier[] permissions;

  @Getter
  private final TierPriority priority;

  @Getter
  private final Permission unlimitedPermission;

  private TieredPermission(Builder builder) {
    this.prefix = Objects.requireNonNull(builder.prefix);
    this.priority = Objects.requireNonNull(builder.priority);
    this.tiers = IntLists.unmodifiable(builder.tiers);

    Preconditions.checkArgument(!tiers.isEmpty(), "Empty tier list");

    this.permissions = new PermissionTier[tiers.size()];
    this.unlimitedPermission = builder.unlimitedPermission;

    int index = 0;
    for (int tier: tiers) {
      PermissionTier perm = new PermissionTier(
          tier,
          Permissions.register(prefix + tier)
      );

      permissions[index++] = perm;
    }

    Arrays.sort(permissions, getPriority());
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getMaxTier() {
    return priority == TierPriority.HIGHEST
        ? permissions[0].tier()
        : permissions[permissions.length - 1].tier();
  }

  public int getMinTier() {
    return priority == TierPriority.LOWEST
        ? permissions[0].tier()
        : permissions[permissions.length - 1].tier();
  }

  public OptionalInt getTier(User user) {
    return _getTier(user::hasPermission);
  }

  public OptionalInt getTier(Permissible permissible) {
    return _getTier(permissible::hasPermission);
  }

  public boolean hasUnlimited(Permissible permissible) {
    return getUnlimitedPermission() != null
        && permissible.hasPermission(getUnlimitedPermission());
  }

  public boolean hasUnlimited(User user) {
    return getUnlimitedPermission() != null
        && user.hasPermission(getUnlimitedPermission());
  }

  private OptionalInt _getTier(Predicate<Permission> permissionHolder) {
    if (getUnlimitedPermission() != null
        && permissionHolder.test(getUnlimitedPermission())
    ) {
      return OptionalInt.of(
          priority == TierPriority.HIGHEST
              ? Integer.MAX_VALUE
              : Integer.MIN_VALUE
      );
    }

    for (var t: permissions) {
      if (permissionHolder.test(t.permission)) {
        return OptionalInt.of(t.tier);
      }
    }

    return OptionalInt.empty();
  }

  public boolean contains(int tier) {
    return getPermission(tier) != null;
  }

  public Permission getPermission(int tier) {
    for (var t: permissions) {
      if (t.tier == tier) {
        return t.permission;
      }
    }

    return null;
  }

  public enum TierPriority implements Comparator<PermissionTier> {
    HIGHEST {
      @Override
      public int compare(PermissionTier o1, PermissionTier o2) {
        return Integer.compare(o2.tier, o1.tier);
      }
    },

    LOWEST {
      @Override
      public int compare(PermissionTier o1, PermissionTier o2) {
        return Integer.compare(o1.tier, o2.tier);
      }
    }
  }

  private record PermissionTier(int tier, Permission permission) {}

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  public static class Builder {

    private String prefix;
    private Permission unlimitedPermission;

    private TierPriority priority = TierPriority.HIGHEST;

    private final IntList tiers = new IntArrayList();

    public Builder tiersFrom1To(int end) {
      return range(Range.between(1, end));
    }

    public Builder range(Range<Integer> range) {
      tiers.clear();
      for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
        tiers.add(i);
      }

      return this;
    }

    public Builder tiers(int... tiers) {
      this.tiers.addAll(IntList.of(tiers));
      return this;
    }

    public Builder addTier(int tier) {
      tiers.add(tier);
      return this;
    }

    public Builder unlimitedPerm(String perm) {
      return unlimitedPermission(Permissions.register(perm));
    }

    public Builder allowUnlimited() {
      return unlimitedPerm(prefix + (prefix.endsWith(".") ? "" : ".") + "unlimited");
    }

    public TieredPermission build() {
      return new TieredPermission(this);
    }
  }
}