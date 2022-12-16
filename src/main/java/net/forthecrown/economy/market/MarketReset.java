package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.forthecrown.utils.transformation.RegionCopyPaste;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.spongepowered.math.vector.Vector3i;

@Getter
@RequiredArgsConstructor
public class MarketReset {
    /* ----------------------------- CONSTANTS ------------------------------ */

    public static final String
            KEY_PLACE_POS = "placementMinPoint",
            KEY_COPY_POS = "copyMinPoint",
            KEY_COPY_SIZE = "copyAreaSize";

    /** The depth of the market's template below the actual market */
    public static final int TEMPLATE_DEPTH = 40;

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private final Vector3i placement;
    private final Vector3i copyPosition;
    private final Vector3i size;

    /* ----------------------------- STATIC METHODS ------------------------------ */

    static boolean isEntranceSign(BlockState state) {
        if (!(state instanceof Sign sign)) {
            return false;
        }

        return sign.getPersistentDataContainer()
                .has(ShopEntrance.DOOR_SIGN, PersistentDataType.BYTE);
    }

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Places the market's template area in the given world
     * @param world The world to place in
     */
    public void place(World world) {
        var regionPaste = RegionCopyPaste.create(
                WorldBounds3i.of(world, copyPosition, copyPosition.add(size)),
                new WorldVec3i(world, placement)
        )
                // Ignore entrance signs
                .addFilter((copy1, paste, copyPaste) -> !isEntranceSign(paste.getState()))

                // Ignore any entrance sign skulls
                .addFilter((copy1, paste, copyPaste) -> {
                    BlockState state = paste.getState();

                    if (!(state instanceof Skull skull)) {
                        return true;
                    }

                    return !skull.getPersistentDataContainer()
                            .has(ShopEntrance.NOTICE_KEY, PersistentDataType.STRING);
                });

        regionPaste.run();

        // Yeet any player-placed entities
        WorldBounds3i.of(world, placement, placement.add(size))
                .getEntities(entity -> {
                    return entity instanceof ArmorStand
                            || entity instanceof Painting
                            || entity instanceof ItemFrame
                            || entity instanceof Vehicle;
                })
                .forEach(Entity::remove);
    }

    /**
     * Creates, aka copies, a template from the shop's placement area
     * and pastes it to the copy area to use to later reset the shop.
     * @param world The world to copy and paste in
     */
    public void copy(World world) {
        RegionCopyPaste.create(
                        WorldBounds3i.of(world, placement, placement.add(size)),
                        new WorldVec3i(world, copyPosition)
        )
                // Ignore any existing sign shops
                .addFilter((copy1, paste, copyPaste) -> !SignShops.isShop(copy1))
                // Ignore entrance signs
                .addFilter((copy1, paste, copyPaste) -> !isEntranceSign(copy1.getState()))

                .run();
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.add(KEY_PLACE_POS, placement);
        json.add(KEY_COPY_POS, copyPosition);
        json.add(KEY_COPY_SIZE, size);

        return json.getSource();
    }

    public static MarketReset deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return new MarketReset(
                Vectors.read3i(json.get(KEY_PLACE_POS)),
                Vectors.read3i(json.get(KEY_COPY_POS)),
                Vectors.read3i(json.get(KEY_COPY_SIZE))
        );
    }
}