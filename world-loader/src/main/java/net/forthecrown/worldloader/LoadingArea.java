package net.forthecrown.worldloader;

import net.forthecrown.worldloader.WorldLoaderService.WorldLoad;
import org.bukkit.World;

public record LoadingArea(int minX, int minZ, int maxX, int maxZ) {

  public LoadingArea(int minX, int minZ, int maxX, int maxZ) {
    this.minX = Math.min(minX, maxX);
    this.minZ = Math.min(minZ, maxZ);
    this.maxX = Math.max(minX, maxX);
    this.maxZ = Math.max(minZ, maxZ);
  }

  public static LoadingArea getArea(LoadingArea set, World fallback) {
    if (set != null) {
      return set;
    }

    var border = fallback.getWorldBorder();
    double radius = border.getSize() / 2;
    var center = border.getCenter();

    var min = center.clone().subtract(radius, 0, radius);
    var max = center.clone().add(radius, 0, radius);

    return new LoadingArea(min.getBlockX(), min.getBlockZ(), max.getBlockX(), max.getBlockZ());
  }

  public static LoadingArea ofRadius(int centerX, int centerZ, int radiusX, int radiusZ) {
    return new LoadingArea(
        centerX - radiusX,
        centerZ - radiusZ,
        centerX + radiusX,
        centerZ + radiusZ
    );
  }

  public void apply(WorldLoad load) {
    load.areaBounds(minX, minZ, maxX, maxZ);
  }

  public double centerX() {
    return minX + ((double) (maxX - minX) / 2);
  }

  public double centerZ() {
    return minZ + ((double) (maxZ - minZ) / 2);
  }

  public int area() {
    return sizeX() * sizeZ();
  }

  public int sizeX() {
    return maxX - minX;
  }

  public int sizeZ() {
    return maxZ - minZ;
  }

  public LoadingArea div(int div) {
    return new LoadingArea(
        minX / div,
        minZ / div,
        maxX / div,
        maxZ / div
    );
  }

  public LoadingArea mul(int div) {
    return new LoadingArea(
        minX * div,
        minZ * div,
        maxX * div,
        maxZ * div
    );
  }
}
