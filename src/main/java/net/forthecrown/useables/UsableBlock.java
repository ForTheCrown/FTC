package net.forthecrown.useables;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.text.writer.TextWriter;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.spongepowered.math.vector.Vector3i;

@RequiredArgsConstructor
public class UsableBlock extends BukkitSavedUsable {
    @Getter
    private final World world;
    @Getter
    private final Vector3i position;

    @Override
    public void adminInfo(TextWriter writer) {
        writer.field("Position", position);
        writer.field("World", world.getName());
        super.adminInfo(writer);
    }

    public Block getBlock() {
        return Vectors.getBlock(position, world);
    }

    public TileState getTileEntity() {
        return (TileState) getBlock().getState();
    }

    @Override
    public void save() {
        var tile = getTileEntity();
        save(tile.getPersistentDataContainer());

        tile.update();
    }

    @Override
    public PersistentDataContainer getDataContainer() {
        return getTileEntity().getPersistentDataContainer();
    }

    @Override
    protected NamespacedKey getDataKey() {
        return Usables.BLOCK_KEY;
    }
}