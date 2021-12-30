package net.forthecrown.core.rw;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OrePopulator extends BlockPopulator {
    private final Material material;
    private final int maxSize, minY, maxY, amountInChunk;

    public OrePopulator(Material material, int maxSize, int minY, int maxY, int amountInChunk) {
        this.material = material;
        this.maxSize = maxSize;
        this.minY = minY;
        this.maxY = maxY;
        this.amountInChunk = amountInChunk;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        int amountMod = amountInChunk / 4;
        int amount = Math.max(amountInChunk / 2, amountInChunk + random.nextInt(-amountMod, amountMod + 1));

        for (int i = 0; i< amount; i++) {
            placeVein(random, limitedRegion, i);
        }
    }

    private void placeVein(Random random, LimitedRegion limitedRegion, int yMod) {
        int size = random.nextInt(Math.min(maxSize, (limitedRegion.getBuffer()*2) + 16));

        int minX = limitedRegion.getCenterBlockX() + random.nextInt(-8, 9);
        int minZ = limitedRegion.getCenterBlockZ() + random.nextInt(-8, 9);
        int minY = random.nextInt(this.minY, maxY + 1);
        minY = minY + (minY / yMod);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    int x = minX + i;
                    int y = minY + j;
                    int z = minZ + k;

                    if(!limitedRegion.isInRegion(x, y ,z)
                            || random.nextBoolean()
                            || limitedRegion.getType(x, y, z).isAir()
                    ) continue;
                    limitedRegion.setType(x, y, z, material);
                }
            }
        }
    }
}
