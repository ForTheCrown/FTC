package net.forthecrown.structure;

import static org.bukkit.Material.ANDESITE;
import static org.bukkit.Material.ANDESITE_SLAB;
import static org.bukkit.Material.ANDESITE_STAIRS;
import static org.bukkit.Material.ANDESITE_WALL;
import static org.bukkit.Material.COBBLESTONE;
import static org.bukkit.Material.COBBLESTONE_SLAB;
import static org.bukkit.Material.COBBLESTONE_STAIRS;
import static org.bukkit.Material.COBBLESTONE_WALL;
import static org.bukkit.Material.CRACKED_STONE_BRICKS;
import static org.bukkit.Material.DIORITE;
import static org.bukkit.Material.DIORITE_SLAB;
import static org.bukkit.Material.DIORITE_STAIRS;
import static org.bukkit.Material.GRANITE;
import static org.bukkit.Material.GRANITE_SLAB;
import static org.bukkit.Material.GRANITE_STAIRS;
import static org.bukkit.Material.POLISHED_ANDESITE;
import static org.bukkit.Material.POLISHED_ANDESITE_SLAB;
import static org.bukkit.Material.POLISHED_ANDESITE_STAIRS;
import static org.bukkit.Material.POLISHED_DIORITE;
import static org.bukkit.Material.POLISHED_DIORITE_SLAB;
import static org.bukkit.Material.POLISHED_DIORITE_STAIRS;
import static org.bukkit.Material.POLISHED_GRANITE;
import static org.bukkit.Material.POLISHED_GRANITE_SLAB;
import static org.bukkit.Material.POLISHED_GRANITE_STAIRS;
import static org.bukkit.Material.STONE;
import static org.bukkit.Material.STONE_BRICKS;
import static org.bukkit.Material.STONE_BRICK_SLAB;
import static org.bukkit.Material.STONE_BRICK_STAIRS;
import static org.bukkit.Material.STONE_BRICK_WALL;
import static org.bukkit.Material.STONE_SLAB;
import static org.bukkit.Material.STONE_STAIRS;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.forthecrown.utils.VanillaAccess;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class RotLookup {

  public static final ImmutableMap<Material, List<Material>> MAP
      = ImmutableMap.<Material, List<Material>>builder()

      .put(STONE,                     List.of(ANDESITE))
      .put(STONE_SLAB,                List.of(ANDESITE_SLAB))
      .put(STONE_STAIRS,              List.of(ANDESITE_STAIRS))

      .put(ANDESITE,                  List.of(COBBLESTONE))
      .put(ANDESITE_SLAB,             List.of(COBBLESTONE_SLAB))
      .put(ANDESITE_STAIRS,           List.of(COBBLESTONE_STAIRS))
      .put(ANDESITE_WALL,             List.of(COBBLESTONE_WALL))

      .put(STONE_BRICKS,              List.of(CRACKED_STONE_BRICKS))
      .put(STONE_BRICK_STAIRS,        List.of(COBBLESTONE_STAIRS, ANDESITE_STAIRS))
      .put(STONE_BRICK_SLAB,          List.of(COBBLESTONE_SLAB, ANDESITE_SLAB))
      .put(STONE_BRICK_WALL,          List.of(COBBLESTONE_WALL, ANDESITE_WALL))

      .put(POLISHED_ANDESITE,         List.of(ANDESITE))
      .put(POLISHED_ANDESITE_SLAB,    List.of(ANDESITE_SLAB))
      .put(POLISHED_ANDESITE_STAIRS,  List.of(ANDESITE_STAIRS))

      .put(POLISHED_GRANITE,          List.of(GRANITE))
      .put(POLISHED_GRANITE_SLAB,     List.of(GRANITE_SLAB))
      .put(POLISHED_GRANITE_STAIRS,   List.of(GRANITE_STAIRS))

      .put(POLISHED_DIORITE,          List.of(DIORITE))
      .put(POLISHED_DIORITE_SLAB,     List.of(DIORITE_SLAB))
      .put(POLISHED_DIORITE_STAIRS,   List.of(DIORITE_STAIRS))

      .build();

  public static Material rot(Material material, Random random) {
    Objects.requireNonNull(material);
    Objects.requireNonNull(random);

    var list = MAP.get(material);

    if (list == null || list.isEmpty()) {
      return null;
    }

    if (list.size() == 1) {
      return list.get(0);
    }

    return list.get(random.nextInt(list.size()));
  }

  public static BlockData rot(BlockData data, Random random) {
    Objects.requireNonNull(data);
    var mat = rot(data.getMaterial(), random);

    if (mat == null) {
      return null;
    }

    var newData = mat.createBlockData();
    return VanillaAccess.merge(newData, data);
  }
}