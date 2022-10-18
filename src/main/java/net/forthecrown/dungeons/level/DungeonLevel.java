package net.forthecrown.dungeons.level;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.Getter;
import net.forthecrown.core.Crown;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.LongConsumer;

import static net.forthecrown.dungeons.level.DungeonPiece.TAG_CHILDREN;
import static net.forthecrown.dungeons.level.DungeonPiece.TAG_TYPE;

public class DungeonLevel implements Iterable<DungeonPiece> {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final Logger LOGGER = Crown.logger();

    public static final String
            TAG_PIECES = "pieces",
            TAG_ROOT = "root";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** Piece ID to piece lookup map */
    private final Map<UUID, DungeonPiece> pieceLookup = new Object2ObjectOpenHashMap<>();

    /** Chunk to piece list map used for faster spatial lookups */
    private final Long2ObjectMap<List<DungeonPiece>> chunkLookup = new Long2ObjectOpenHashMap<>();

    /** The root room from which all other rooms have sprung */
    @Getter
    private DungeonRoom root;

    /** Bounds of the entire level */
    @Getter
    private Bounds3i levelBounds;

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Adds this piece and ALL of its children to this level.
     * <p>
     * This method will recurse until all descendant nodes of
     * the given piece have been added to this level.
     * <p>
     * If the current root is null, and the given piece is a
     * root-type piece, the given piece will be set as the new
     * root piece
     *
     * @param piece The piece to add
     */
    public void addPiece(DungeonPiece piece) {
        if (this.root == null
                && piece instanceof DungeonRoom room
                && room.getType().hasFlags(Rooms.FLAG_ROOT)
        ) {
            this.root = room;
        }

        pieceLookup.put(piece.getId(), piece);

        if (piece instanceof DungeonRoom) {
            forEachChunk(piece.getBounds(), value -> {
                var list = chunkLookup.computeIfAbsent(value, l -> new ObjectArrayList<>());
                list.add(piece);
            });
        }

        if (levelBounds == null) {
            levelBounds = piece.getBounds();
        } else {
            levelBounds = levelBounds.combine(piece.getBounds());
        }

        for (var c: piece.getChildren().values()) {
            addPiece(c);
        }
    }

    /**
     * Gets all pieces that intersect with the given piece
     * @param area The area to get the intersecting pieces of
     * @return All pieces that intersect the input
     */
    public Set<DungeonPiece> getIntersecting(Bounds3i area) {
        if (!levelBounds.overlaps(area)) {
           return ObjectSets.emptySet();
        }

        Set<DungeonPiece> intersecting = new ObjectOpenHashSet<>();

        forEachChunk(area, value -> {
            var list = chunkLookup.get(value);

            if (list == null || list.isEmpty()) {
                return;
            }

            for (var e: list) {
                if (e.getBounds().overlaps(area)) {
                    intersecting.add(e);
                }
            }
        });

        return intersecting;
    }

    private void forEachChunk(Bounds3i bb, LongConsumer consumer) {
        int minX = bb.minX() >> 4;
        int minZ = bb.minZ() >> 4;
        int maxX = bb.maxX() >> 4;
        int maxZ = bb.maxZ() >> 4;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                long packed = Chunk.getChunkKey(x, z);
                consumer.accept(packed);
            }
        }
    }

    public void save(CompoundTag tag) {
        ListTag pieces = savePieces();
        tag.put(TAG_PIECES, pieces);
        tag.putUUID(TAG_ROOT, root.getId());
    }

    public void load(CompoundTag tag) {
        chunkLookup.clear();
        pieceLookup.clear();
        root = null;

        loadPieces(tag.getList(TAG_PIECES, Tag.TAG_COMPOUND), tag.getUUID(TAG_ROOT));
    }

    // Note on piece serialization:
    //
    // This overly elaborate-ass piece serialization format
    // the result of my attempts at ensuring the NBT max tag
    // depth is never reached by serializing all pieces into
    // a flat list and then, during loading, relinking all
    // the pieces using their UUIDs.
    //
    // Which occurs in the loadPieces(Tag, UUID) method,
    // which loads all the pieces from the flat List tag and
    // then uses a temporary Set<Pair> and Map<UUID, Piece>
    // combo to relink them before calling addRecursively()
    // and having them added to the level properly.
    //    -- Jules

    private ListTag savePieces() {
        ListTag pieces = new ListTag();
        for (var p: this) {
            CompoundTag pTag = new CompoundTag();

            p.save(pTag);
            pTag.put(TAG_TYPE, PieceTypes.save(p.getType()));

            var children = p.getChildren();

            if (!children.isEmpty()) {
                ListTag childTag = new ListTag();

                for (var c: children.keySet()) {
                    childTag.add(TagUtil.writeUUID(c));
                }

                pTag.put(TAG_CHILDREN, childTag);
            }

            pieces.add(pTag);
        }

        return pieces;
    }

    private void loadPieces(ListTag tag, UUID rootId) {
        Map<UUID, DungeonPiece> linkMap = new Object2ObjectOpenHashMap<>();
        Set<Pair<DungeonPiece, Set<UUID>>> piecesAndChildren = new ObjectOpenHashSet<>();

        for (var e: tag) {
            CompoundTag pTag = (CompoundTag) e;
            PieceType type = PieceTypes.load(pTag.get(TAG_TYPE));

            DungeonPiece piece = type.load(pTag);
            Set<UUID> children = new ObjectOpenHashSet<>();

            if (pTag.contains(TAG_CHILDREN, Tag.TAG_LIST)) {
                children.addAll(
                        TagUtil.readCollection(pTag.get(TAG_CHILDREN), TagUtil::readUUID)
                );
            }

            linkMap.put(piece.getId(), piece);

            if (!children.isEmpty()) {
                piecesAndChildren.add(Pair.of(piece, children));
            }
        }

        for (var pair: piecesAndChildren) {
            var piece = pair.getFirst();

            for (var child: pair.getSecond()) {
                var childPiece = linkMap.get(child);

                if (childPiece == null) {
                    LOGGER.warn("Missing child mapping for ID: '{}', parent: '{}'",
                            child, piece.getId()
                    );

                    continue;
                }

                piece.addChild(childPiece);
            }
        }

        DungeonRoom rootPiece = (DungeonRoom) linkMap.get(rootId);

        if (rootPiece == null) {
            LOGGER.warn("No root piece in NBT! Cannot load piece data, no root under ID: {}", rootId);
            return;
        }

        addPiece(rootPiece);
    }

    @NotNull
    @Override
    public Iterator<DungeonPiece> iterator() {
        return Iterators.unmodifiableIterator(pieceLookup.values().iterator());
    }
}