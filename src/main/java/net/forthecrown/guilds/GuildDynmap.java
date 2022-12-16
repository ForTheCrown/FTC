package net.forthecrown.guilds;

import it.unimi.dsi.fastutil.longs.LongSet;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.FtcDynmap;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.Vectors;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.Logger;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

public @UtilityClass class GuildDynmap {
    private static final Logger LOGGER = FTC.getLogger();

    private static final String
            MARKER_SET_ID = "chunk_markers",
            MARKER_LABEL = "Guild Areas";

    private MarkerSet getSet() {
        var markers = FtcDynmap.getMarkerAPI();
        var set = markers.getMarkerSet(MARKER_SET_ID);

        if (set != null) {
            return set;
        }

        return markers.createMarkerSet(MARKER_SET_ID, MARKER_LABEL, null, true);
    }

    public void renderChunk(ChunkPos chunkPos, Guild guild) {
        String markerId = chunkId(chunkPos);
        var set = getSet();

        if (set.findAreaMarker(markerId) != null) {
            return;
        }

        double[] xCorners = getCorners(chunkPos.x);
        double[] zCorners = getCorners(chunkPos.z);

        LOGGER.debug("xCorners={}", xCorners);
        LOGGER.debug("zCorners={}", zCorners);

        AreaMarker marker = set.createAreaMarker(
                markerId,
                guild.getName(),
                true,
                Guilds.getWorld().getName(),
                xCorners, zCorners,
                true
        );

        int pColor, sColor;
        try {
            pColor = guild.getSettings()
                    .getPrimaryColor()
                    .getTextColor()
                    .value();
        } catch (Exception ignored) {
            pColor = marker.getFillColor();
        }
        try {
            sColor = guild.getSettings()
                    .getSecondaryColor()
                    .getTextColor()
                    .value();
        } catch (Exception ignored) {
            sColor = marker.getFillColor();
        }

        marker.setFillStyle(0.5, pColor);
        marker.setLineStyle(marker.getLineWeight(), 0.8, sColor);

        LOGGER.debug("Created chunk marker at {} for {}",
                chunkPos, guild.getName()
        );
    }

    public void unrenderChunk(ChunkPos chunkPos) {
        String markerId = chunkId(chunkPos);

        var markerSet = getSet();
        AreaMarker marker = markerSet.findAreaMarker(markerId);

        if (marker != null) {
            marker.deleteMarker();
            LOGGER.debug("Deleted marker for chunk {}", chunkPos);
        }
    }

    private String chunkId(ChunkPos pos) {
        return MARKER_SET_ID + "::" + pos.x + "_" + pos.z;
    }

    private double[] getCorners(int chunkCoord) {
        int xBlock = Vectors.toBlock(chunkCoord);

        return new double[] {
                xBlock,
                xBlock + Vectors.CHUNK_SIZE
        };
    }

    public void updateGuildChunks(Guild guild) {
        Tasks.runAsync(() -> {
            LongSet guildChunks = GuildManager.get().getGuildChunks(guild);

            guildChunks.forEach(c -> {
                var cPos = Guilds.chunkFromPacked(c);

                GuildDynmap.unrenderChunk(cPos);
                GuildDynmap.renderChunk(cPos, guild);
            });
        });
    }
}