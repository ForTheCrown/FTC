package net.forthecrown.resourceworld;

import java.time.Duration;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class RwConfig {

  public long[] legalSeeds = {};
  public long[] bannedSeeds = {};

  public String toHazGate = "res_to_haz";
  public String toResGate = "haz_to_res";

  public String worldGuardSpawn = "rg_spawn";

  public String spawnStructure = "rw_spawn";

  public String portalWarp = "portal";

  public Duration sectionRetentionTime = Duration.ofMinutes(5);

  public Duration autosaveInterval;

  public MessagesConfig messages = new MessagesConfig();

  public DoubleDrop doubleDrop = new DoubleDrop();

  @ConfigSerializable
  public static class MessagesConfig {
    public Component resetStart
        = Text.valueOf("&eThe resource world is not closed, will open in ~1 hour");

    public Component resetEnd = Text.valueOf("&eThe resource world has reset");
  }

  @ConfigSerializable
  public static class DoubleDrop {
    public boolean enabled = true;
    public float rate = 0.5f;
  }
}
