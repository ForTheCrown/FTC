package net.forthecrown.resourceworld;

import static net.forthecrown.McConstants.MIN_Y;
import static net.forthecrown.McConstants.Y_SIZE;

import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.HeightMap;
import org.bukkit.craftbukkit.v1_20_R3.CraftHeightMap;

public final class Constants {
  private Constants() {}

  /**
   * The distance a biome and flatness check should go, in QuartPos distance aka bit shifted twice
   * to the right
   */
  public static final int SPAWN_CHECK_QUART = 5;

  /**
   * The maximum Y a spawn can generate at, if we're above it, then we're most likely in a hilly
   * area that is unfit to for a spawn
   */
  public static final int MAX_Y = 75;

  /**
   * The amount of WG region is bigger than the spawn
   */
  public static final int WG_OVERREACH = 5;

  /**
   * The amount the WG region is bigger than the spawn, on the Y axis, goes from MAX_Y to
   * spawn_y_pos - WG_SIZE_Y
   */
  public static final int WG_SIZE_Y = 20;

  /**
   * The max Y difference a potential spawn position can have
   */
  public static final int MAX_Y_DIF = 2;

  /**
   * The maximum amount of attempts that can be made to find a seed
   */
  public static final int MAX_SEED_ATTEMPTS = 1024;

  /**
   * All legal biomes that spawn can be in
   */
  public static final Set<ResourceKey<Biome>> SPAWN_BIOMES = ObjectSet.of(
      Biomes.PLAINS,
      Biomes.SUNFLOWER_PLAINS,
      Biomes.SNOWY_PLAINS,
      Biomes.DESERT
  );

  /**
   * Required biome tags any potential seed must have within its world borders
   */
  public static final Set<TagKey<Biome>> REQUIRED_TAGS = ObjectSet.of(
      BiomeTags.IS_FOREST,
      BiomeTags.IS_MOUNTAIN,
      BiomeTags.IS_TAIGA,
      BiomeTags.IS_SAVANNA,
      BiomeTags.IS_JUNGLE,
      BiomeTags.IS_BADLANDS,
      BiomeTags.IS_DEEP_OCEAN,
      BiomeTags.IS_OCEAN,
      BiomeTags.IS_RIVER,
      BiomeTags.IS_BEACH,
      BiomeTags.HAS_IGLOO,
      BiomeTags.HAS_VILLAGE_SNOWY
  );

  public static Set<ResourceKey<Biome>> REQUIRED_BIOMES = ObjectSet.of(
      Biomes.CHERRY_GROVE
  );

  // The height maps for NMS and Bukkit that are used for height
  // calculation... shocking ik
  public static final Heightmap.Types HEIGHT_MAP_TYPE
      = Heightmap.Types.OCEAN_FLOOR_WG;

  public static final HeightMap BUKKIT_HEIGHT_MAP
      = CraftHeightMap.fromNMS(HEIGHT_MAP_TYPE);

  /**
   * An accessor that ChunkGenerator needs for a height check call
   */
  public static final LevelHeightAccessor HEIGHT_ACCESSOR
      = LevelHeightAccessor.create(MIN_Y, Y_SIZE);
}
