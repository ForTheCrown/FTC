package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.cosmetics.emotes.EmoteMessages.EMOTE_SCARE_COOLDOWN;
import static net.forthecrown.cosmetics.emotes.EmoteMessages.scareSender;
import static net.forthecrown.cosmetics.emotes.EmoteMessages.scareTarget;

import java.time.LocalDate;
import java.time.Month;
import net.forthecrown.command.Commands;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EmoteScare extends Emote {

  public EmoteScare() {
    super("scare", 30 * 20, EMOTE_SCARE_COOLDOWN);

    setDescription("Scares another player.");
    setPermission(EmotePermissions.EMOTE_SCARE);

    register();
  }

  @Override
  public boolean test(CommandSource source) {
    var month = LocalDate.now().getMonth();
    boolean testRes = super.test(source);

    if (month == Month.OCTOBER) {
      if (!testRes && source.isPlayer()) {
        Commands.executeConsole("lp user %s permission set %s",
            source.textName(), getPermission()
        );
      }

      return true;
    }

    return testRes;
  }

  @Override
  public int execute(User sender, User target) {
    sender.sendMessage(scareSender(target));
    target.sendMessage(scareTarget(sender, test(sender.getCommandSource())));

    scare(target.getPlayer());
    return 0;
  }

  @Override
  public int executeSelf(User user) {
    scare(user.getPlayer());
    return 0;
  }

  private void scare(Player player) {
    Location loc = player.getLocation();
    player.spawnParticle(Particle.MOB_APPEARANCE, loc.getX(), loc.getY(), loc.getZ(), 1);

    Tasks.runLater(() -> {
      player.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 2.0F, 1F);
      player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 9, false, false, false));

      for (int i = 0; i < 3; i++) {
        Tasks.runLater(() -> {
          player.playSound(loc,
              Sound.ENTITY_ENDERMAN_SCREAM,
              SoundCategory.MASTER,
              1.5F, 1F
          );
        }, i * 3L);
      }
    }, 3L);
  }
}