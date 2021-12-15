package net.forthecrown.inventory.crown;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public final class Crowns {
    private Crowns() {}

    public static final int MODEL_DATA = 1478819153;
    public static final String TAG_KEY = "royal_crown";
    private static final Int2ObjectMap<CrownRank> RANKS = new Int2ObjectOpenHashMap<>();

    public static void init() {
        //lol
        int rank = 0;
        register(CrownRank.simple(++rank));
        register(CrownRank.simple(++rank));
        register(CrownRank.simple(++rank));
        register(CrownRank.simple(++rank));
        register(CrownRank.simple(++rank));
    }

    public static CrownRank getRank(int rank) {
        return RANKS.get(rank);
    }

    private static void register(CrownRank rank) {
        RANKS.put(rank.applyAtRank(), rank);
    }

    public static boolean isCrown(ItemStack item) {
        if(FtcItems.isEmpty(item)) return false;
        return FtcItems.hasTagElement(item.getItemMeta(), TAG_KEY);
    }

    public static ItemStack make(UUID owner, boolean queen) {
        ItemStack item = new ItemStackBuilder(Material.GOLDEN_HELMET)
                .setName(FtcItems.CROWN_TITLE)
                .setModelData(MODEL_DATA)
                .setUnbreakable(true)
                .build();

        RoyalCrown crown = new RoyalCrown(owner, item);
        crown.setRank(0);
        crown.setQueen(queen);
        crown.upgrade();

        return item;
    }
}
