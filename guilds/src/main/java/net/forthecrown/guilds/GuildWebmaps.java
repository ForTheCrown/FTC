package net.forthecrown.guilds;

import static net.forthecrown.utils.math.Vectors.CHUNK_SIZE;
import static net.forthecrown.utils.math.Vectors.toBlock;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Tasks;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.WebMaps;
import org.bukkit.Color;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector2i;

public final class GuildWebmaps {
  private GuildWebmaps() {}

  private static final Logger LOGGER = Loggers.getLogger();

  private static final String MARKER_SET_ID = "chunk_markers";
  private static final String MARKER_LABEL = "Guild Areas";

  private static Optional<MapLayer> getSet() {
    return WebMaps.findOrDefineLayer(Guilds.getWorld(), MARKER_SET_ID, MARKER_LABEL);
  }

  public static void renderChunk(Vector2i chunkPos, Guild guild) {
    String markerId = chunkId(chunkPos);
    var setOpt = getSet();

    if (setOpt.isEmpty()) {
      return;
    }

    var set = setOpt.get();

    if (set.findAreaMarker(markerId).isPresent()) {
      return;
    }

    double[] xCorners = getCorners(chunkPos.x());
    double[] zCorners = getCorners(chunkPos.y());

    LOGGER.debug("xCorners={}", xCorners);
    LOGGER.debug("zCorners={}", zCorners);

    var markerResult = set.createAreaMarker(
        markerId,
        guild.getName(),
        xCorners,
        zCorners
    );

    if (markerResult.isError()) {
      LOGGER.error("Error creating area marker for chunk {} (guild={}): {}",
          chunkPos, guild, markerResult.getError()
      );

      return;
    }

    var marker = markerResult.getValue();

    Color fillColor;
    Color lineColor;

    try {
      var textColor = guild.getSettings()
          .getPrimaryColor()
          .getTextColor();

      fillColor = WebMaps.fromTextColor(textColor);
    } catch (Exception ignored) {
      fillColor = marker.getFillColor();
    }

    try {
      var textColor = guild.getSettings()
          .getSecondaryColor()
          .getTextColor();

      lineColor = WebMaps.fromTextColor(textColor);
    } catch (Exception ignored) {
      lineColor = marker.getFillColor();
    }

    marker.setFillColor(WebMaps.setAlpha(fillColor, 0.5));
    marker.setLineColor(WebMaps.setAlpha(lineColor, 0.8));

    LOGGER.debug("Created chunk marker at {} for {}", chunkPos, guild.getName());
  }

  public static void unrenderChunk(Vector2i chunkPos) {
    String markerId = chunkId(chunkPos);

    getSet()
        .flatMap(mapLayer -> mapLayer.findAreaMarker(markerId))
        .ifPresent(mapAreaMarker -> {
          mapAreaMarker.delete();
          LOGGER.debug("Deleted marker for chunk {}", chunkPos);
        });
  }

  private static String chunkId(Vector2i pos) {
    return MARKER_SET_ID + "::" + pos.x() + "_" + pos.y();
  }

  private static double[] getCorners(int chunkCoord) {
    int xBlock = toBlock(chunkCoord);

    return new double[]{ xBlock, xBlock + CHUNK_SIZE };
  }

  public static void updateGuildChunks(Guild guild) {
    Tasks.runAsync(() -> {
      LongSet guildChunks = Guilds.getManager().getGuildChunks(guild);

      guildChunks.forEach(c -> {
        var cPos = Guilds.chunkFromPacked(c);

        GuildWebmaps.unrenderChunk(cPos);
        GuildWebmaps.renderChunk(cPos, guild);
      });
    });
  }
}