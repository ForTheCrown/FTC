package net.forthecrown.cosmetics.emotes;

import static net.forthecrown.cosmetics.emotes.EmoteMessages.HEART;
import static net.kyori.adventure.text.Component.text;

import java.util.Random;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class EmotePet extends Emote {

  public EmotePet() {
    super("pet", 20 * 2, text("You pet too much"));
    register();
  }

  static final Component[] PARTS = {
      text("Head"),
      text("Butt"),
      text("Tummy")
  };

  static final Random RANDOM = new Random();

  static Component randomPart() {
    return PARTS[RANDOM.nextInt(PARTS.length)];
  }
  
  @Override
  public int execute(User sender, User target) {
    Location loc = sender.getLocation();
    var part = randomPart();

    sender.sendMessage(
        Text.format("{0} Pet &e{1, user}&r's &e{2}&r {0}",
            HEART, target, part
        )
    );
    target.sendMessage(
        Text.format("{0} &e{1, user}&r pet your &e{2}&r {0}",
            HEART, sender, part
        )
    );

    if (target.getPlayer().getGameMode() != GameMode.SPECTATOR) {
      Location targetLoc = target.getLocation();

      Particle.HEART.builder()
          .location(targetLoc.add(0, 1, 0))
          .count(5)
          .offset(0.5, 0.5, 0.5)

          // Spawn, relocate and spawn again
          .spawn()
          .location(loc.add(0, 1, 0))
          .spawn();

      targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
      loc.getWorld().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);
    }
    return 0;
  }

  @Override
  public int executeSelf(User user) {
    Location loc = user.getLocation();

    user.sendMessage(
        Text.format("{0} You petted your own {1} {0}",
            HEART, randomPart()
        )
    );

    user.getPlayer().playSound(loc, Sound.ENTITY_PUFFER_FISH_BLOW_UP, 3.0F, 2F);

    Particle.HEART.builder()
        .location(loc.add(0, 1, 0))
        .count(5)
        .offset(0.5, 0.5, 0.5)
        .spawn();

    return 0;
  }

}