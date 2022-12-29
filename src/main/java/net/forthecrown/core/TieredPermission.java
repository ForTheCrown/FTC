package net.forthecrown.core;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.user.User;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
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
  private final Range<Integer> range;

  private final Int2ObjectAVLTreeMap<Permission> permissions;

  @Getter
  private final Permission unlimitedPermission;

  public TieredPermission(String prefix, Range<Integer> range, Permission unlimitedPermission) {
    this.prefix = prefix;
    this.range = range;

    // Creates a sorted map that's sorted from largest to smallest
    // this is for iteration in the getTier method, which requires
    // us to scan from greatest tier to smallest to return an accurate
    // result
    permissions = new Int2ObjectAVLTreeMap<>(Comparator.reverseOrder());

    // Register all permissions
    for (int tier = range.getMinimum(); tier <= range.getMaximum(); tier++) {
      permissions.put(tier, Permissions.register(prefix + tier));
    }

    this.unlimitedPermission = unlimitedPermission;
  }

  public static Builder builder() {
    return new Builder();
  }

  public OptionalInt getTier(boolean highest, User user) {
    return _getTier(highest, user::hasPermission);
  }

  public OptionalInt getTier(boolean highest, Permissible permissible) {
    return _getTier(highest, permissible::hasPermission);
  }

  public boolean hasUnlimited(Permissible permissible) {
    return permissible.hasPermission(getUnlimitedPermission());
  }

  public boolean hasUnlimited(User user) {
    return user.hasPermission(getUnlimitedPermission());
  }

  private OptionalInt _getTier(boolean highest, Predicate<Permission> validate) {
    if (validate.test(getUnlimitedPermission())) {
      return OptionalInt.of(
          highest ? Integer.MAX_VALUE : Integer.MIN_VALUE
      );
    }

    Integer result = null;

    for (var v : permissions.int2ObjectEntrySet()) {
      if (!validate.test(v.getValue())) {
        continue;
      }

      if (result == null) {
        result = v.getIntKey();
        continue;
      }

      if (highest) {
        result = Math.max(result, v.getIntKey());
      } else {
        result = Math.min(result, v.getIntKey());
      }
    }

    return result == null
        ? OptionalInt.empty()
        : OptionalInt.of(result);
  }

  public boolean contains(int tier) {
    return permissions.containsKey(tier);
  }

  public Permission getPermission(int tier) {
    return permissions.get(tier);
  }

  @Getter
  @Setter
  @Accessors(chain = true, fluent = true)
  static class Builder {

    private String prefix;
    private Permission unlimitedPermission;
    private Range<Integer> range = Range.between(1, 5);

    public Builder tiersFrom1To(int end) {
      return range(Range.between(1, end));
    }

    public Builder tiersFrom0To(int end) {
      return range(Range.between(0, end));
    }

    public Builder unlimitedPerm(String perm) {
      return unlimitedPermission(Permissions.register(perm));
    }

    public Builder allowUnlimited() {
      return unlimitedPerm(prefix + (prefix.endsWith(".") ? "" : ".") + "unlimited");
    }

    public TieredPermission build() {
      return new TieredPermission(
          Validate.notBlank(prefix),
          range,
          unlimitedPermission
      );
    }
  }
}