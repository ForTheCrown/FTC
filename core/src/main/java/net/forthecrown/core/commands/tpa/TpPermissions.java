package net.forthecrown.core.commands.tpa;

import static net.forthecrown.Permissions.registerCmd;

import net.forthecrown.utils.TieredPermission;
import net.forthecrown.utils.TieredPermission.TierPriority;
import org.bukkit.permissions.Permission;

public final class TpPermissions {
  private TpPermissions() {}

  public static final Permission TPA = registerCmd("tpa");

  public static final TieredPermission TP_DELAY = TieredPermission.builder()
      .prefix("ftc.teleport.delay.")
      .unlimitedPerm("ftc.teleport.bypass")
      .priority(TierPriority.LOWEST)
      .allowUnlimited()
      .tiersFrom1To(5)
      .build();
}
