package net.forthecrown.dungeons.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.GameMode;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;

public record BossContext(float modifier, List<Player> players) {

  public static final float MIN_MODIFIER = 1.0F;

  public static BossContext create(WorldBounds3i room) {
    // A lot of this stuff is arbitrary and should be changed
    //
    // It should dynamically change the boss difficulty based on
    // the amount of people in the room and the gear they have.
    // Aka the amount of gear they have and the quality of the
    // gear

    Collection<Player> players = room.getPlayers();
    players.removeIf(player -> player.getGameMode() == GameMode.SPECTATOR);

    return new BossContext(1F, new ObjectArrayList<>(players));
  }

  /**
   * Creates a health value by multiplying the given initial health value with the context's
   * modifier and rounding up
   *
   * @param initial The initial health value
   * @return The modifier multiplied health value
   */
  public double health(double initial) {
    return Math.ceil(initial * modifier());
  }

  public double damage(double initial) {
    final double damage = (modifier() / 3) * initial;
    return Math.ceil(damage + modifier());
  }

  public AttributeModifier healthModifier() {
    return new AttributeModifier(
        "boss_context_modifier", modifier, Operation.MULTIPLY_SCALAR_1
    );
  }
}