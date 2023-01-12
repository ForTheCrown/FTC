package net.forthecrown.inventory.weapon;

import net.forthecrown.core.config.ConfigData;
import org.spongepowered.math.vector.Vector3i;

@ConfigData(filePath = "royal_sword.json")
public class SwordConfig {
  public static boolean allowNonOwnerSwords = false;

  public static Vector3i swordPostAnimDump = Vector3i.from(273, 99, 178);

  public static float swordAbilityCooldownScalar = 1.5F;

  public static double swordAnim_initialDistance = 2.5D;

  public static String swordAnim_claimScript = "weapon_ability/sword_claim.js";
  public static String swordAnim_claimTest = "weapon_ability/test_claim.js";
}