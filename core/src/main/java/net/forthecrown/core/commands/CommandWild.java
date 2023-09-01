package net.forthecrown.core.commands;

import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.Wild;
import net.forthecrown.grenadier.GrenadierCommand;
import net.kyori.adventure.sound.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CommandWild extends FtcCommand {

  static final Sound SOUND = Sound.sound()
      .type(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT)
      .volume(1)
      .pitch(1)
      .build();

  private final Wild wild;

  public CommandWild(Wild wild) {
    super("wild");

    this.wild = wild;

    setDescription("Allows you to teleport to random locations");
    setAliases("rtp", "randomtp", "randomteleport");
    simpleUsages();

    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command.executes(c -> {
      var player = c.getSource().asPlayer();
      var location = wild.getWildLocation(player);
      player.teleport(location);

      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
      player.playSound(SOUND);

      return 0;
    });
  }
}
