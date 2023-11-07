package net.forthecrown.waypoints;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.BlockArgument.Result;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.spongepowered.math.vector.Vector3i;

public interface WaypointPlatform {
  WaypointPlatform DEFAULT = (placer) -> {
    int minX = -2;
    int minZ = -2;
    int maxX =  2;
    int maxZ =  2;

    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        Material material;

        if ((x == minX || x == maxX) && (z == minZ || z == maxZ)) {
          material = Material.CHISELED_STONE_BRICKS;
        } else {
          int absX = Math.abs(x);
          int absZ = Math.abs(z);

          if (absX > 1 || absZ > 1 || (absX ^ absZ) != 1) {
            material = Material.STONE_BRICKS;
          } else {
            material = Material.POLISHED_ANDESITE;
          }
        }

        placer.setBlock(x, z, material.createBlockData());
      }
    }
  };

  void placeAt(FloorPlacer placer);

  record FloorPlacer(World world, Vector3i basePos, boolean clear) {

    public void setBlock(int x, int z, BlockData data) {
      var pos = basePos.add(x, 0, z);
      Block block = Vectors.getBlock(pos, world);

      if (clear) {
        block.setType(Material.AIR, false);
      } else {
        if (!block.getType().isAir()) {
          block.breakNaturally(true, false);
        }

        block.setBlockData(data, false);
      }
    }
  }

  record LoadedPlatform(BlockData[][] materials) implements WaypointPlatform {

    static final Codec<BlockData> BLOCK_DATA_CODEC = Codec.STRING.comapFlatMap(
        s -> FtcCodecs.safeParse(s, ArgumentTypes.block()).map(Result::getParsedState),
        BlockData::getAsString
    );

    static final Codec<Map<Character, BlockData>> WHERE_CODEC
        = Codec.unboundedMap(FtcCodecs.CHAR, BLOCK_DATA_CODEC)
        .fieldOf("where")
        .codec();

    static final Codec<List<String>> PATTERN_CODEC
        = Codec.STRING.listOf().fieldOf("pattern").codec();

    static DataResult<WaypointPlatform> load(JsonObject json) {
      if (json == null) {
        return Results.error("Null object");
      }

      DataResult<Map<Character, BlockData>> whereResult
          = WHERE_CODEC.parse(JsonOps.INSTANCE, json);

      DataResult<List<String>> patternResult
          = PATTERN_CODEC.parse(JsonOps.INSTANCE, json);

      return whereResult.apply2(LoadedPlatform::loadFrom, patternResult)
          .flatMap(Function.identity());
    }

    private static DataResult<WaypointPlatform> loadFrom(
        Map<Character, BlockData> map,
        List<String> pattern
    ) {
      int height = pattern.size();
      DataResult<BlockData[][]> result = Results.success(new BlockData[height][]);

      for (int i = 0; i < pattern.size(); i++) {
        String p = pattern.get(i);
        DataResult<BlockData[]> layerResult = Results.success(new BlockData[p.length()]);

        for (int charIndex = 0; charIndex < p.length(); charIndex++) {
          char ch = p.charAt(charIndex);
          BlockData data = map.get(ch);
          DataResult<BlockData> dataRes;

          if (data == null) {
            dataRes = Results.error("Unknown block '%s' in pattern '%s'", ch, p);
          } else {
            dataRes = Results.success(data);
          }

          int chI = charIndex;
          layerResult = layerResult.apply2((arr, blockData2) -> {
            arr[chI] = blockData2;
            return arr;
          }, dataRes);
        }

        int layerIndex = i;
        result = result.apply2((arr, layer) -> {
          arr[layerIndex] = layer;
          return arr;
        }, layerResult);
      }

      return result.map(LoadedPlatform::new);
    }

    @Override
    public void placeAt(FloorPlacer placer) {
      int h = materials.length / 2;

      int minX = -h;
      int minZ = -h;
      int maxX =  h;
      int maxZ =  h;

      for (int x = minX; x <= maxX; x++) {
        int xIndex = h + x;
        if (xIndex >= materials.length) {
          break;
        }

        BlockData[] arr = materials[xIndex];

        for (int z = minZ; z <= maxZ; z++) {
          int zIndex = z + h;
          if (zIndex >= arr.length) {
            break;
          }

          BlockData data = arr[zIndex];

          if (data == null) {
            continue;
          }

          placer.setBlock(x, z, data);
        }
      }
    }
  }
}
