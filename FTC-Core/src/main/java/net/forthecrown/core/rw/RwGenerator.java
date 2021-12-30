package net.forthecrown.core.rw;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RwGenerator extends ChunkGenerator {
    @Override
    public @NotNull List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return List.of(
                new SpawnPopulator(),
                new OrePopulator(Material.COAL_ORE, 12, 0, 320, 15),
                new OrePopulator(Material.LAPIS_ORE, 12, -64, 64, 6)
        );
    }
}
