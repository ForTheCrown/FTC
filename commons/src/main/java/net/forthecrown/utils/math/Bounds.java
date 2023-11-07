package net.forthecrown.utils.math;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

public final class Bounds {
  private Bounds() {}

  public static final float PLAYER_WIDTH       = 0.6F;
  public static final float PLAYER_HALF_WIDTH  = PLAYER_WIDTH / 2;
  public static final float PLAYER_HEIGHT      = 1.8F;
  public static final float PLAYER_HALF_HEIGHT = PLAYER_HEIGHT / 2;

  public static Bounds3i playerBounds(Player player, Location location) {
    boolean halved = player.isGliding() || player.isSwimming();
    return playerBounds(halved, location);
  }

  public static Bounds3i playerBounds(boolean halved, Location loc) {
    double height = halved ? PLAYER_HALF_HEIGHT : PLAYER_HEIGHT;

    Vector3d vec = Vectors.doubleFrom(loc);

    Vector3i min = vec.sub(PLAYER_HALF_WIDTH,      0, PLAYER_HALF_WIDTH).floor().toInt();
    Vector3i max = vec.add(PLAYER_HALF_WIDTH, height, PLAYER_HALF_WIDTH).ceil().toInt();

    return Bounds3i.of(min, max);
  }
}