package net.forthecrown.inventory;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.math.vector.Vector3i;

@ConfigSerializable
public class ItemsConfig {

  public boolean allowNonOwnerSwords = false;

  public Vector3i swordPostAnimDump = Vector3i.from(273, 99, 178);

  public double swordAnim_initialDistance = 2.5D;

  public String swordAnim_claimScript = "weapon_ability/sword_claim.js";
  public String swordAnim_claimTest = "weapon_ability/test_claim.js";
}