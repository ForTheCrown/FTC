package net.forthecrown.economy;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import org.bukkit.Material;

import java.util.Map;
import java.util.Set;

import static org.bukkit.Material.*;

public class ServerItemPriceMap extends AbstractJsonSerializer implements ItemPriceMap {

    private final Object2ShortMap<Material> itemPrices = new Object2ShortOpenHashMap<>();

    public ServerItemPriceMap() {
        super("item_prices");

        reload();
        Crown.logger().info("Item Prices loaded");
    }

    @Override
    public void set(Material material, short price) {
        itemPrices.put(material, price);
    }

    @Override
    public short getOrDefault(Material mat, short def) {
        return itemPrices.getOrDefault(mat, def);
    }

    @Override
    public boolean contains(Material mat) {
        return itemPrices.containsKey(mat);
    }

    @Override
    public Set<Material> getMaterials() {
        return itemPrices.keySet();
    }

    @Override
    protected void save(JsonWrapper json) {
        for (Object2ShortMap.Entry<Material> e: itemPrices.object2ShortEntrySet()) {
            json.add(e.getKey().toString().toLowerCase(), e.getShortValue());
        }
    }

    @Override
    protected void reload(JsonWrapper json) {
        itemPrices.clear();

        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            itemPrices.put(Material.getMaterial(e.getKey().toUpperCase()), e.getValue().getAsShort());
        }
    }

    @Override
    protected void createDefaults(JsonWrapper json) {
        //Minerals
        def(COAL, 10);
        def(EMERALD, 7);
        def(DIAMOND, 400);
        def(LAPIS_LAZULI, 10);
        def(REDSTONE, 10);
        def(QUARTZ, 8);
        def(COPPER_INGOT, 7);
        def(IRON_INGOT, 5);
        def(GOLD_INGOT, 7);
        def(NETHERITE_INGOT, 8000);
        def(AMETHYST_SHARD, 8);

        //Mining
        def(STONE, 2);
        def(GRANITE, 3);
        def(DIORITE, 3);
        def(ANDESITE, 3);
        def(COBBLESTONE, 2);
        def(GRAVEL, 2);
        def(TUFF, 5);
        def(CALCITE, 5);
        def(DIRT, 2);
        def(SAND, 2);
        def(COBBLED_DEEPSLATE, 2);
        def(SMOOTH_BASALT, 3);
        def(BASALT, 3);
        def(NETHERRACK, 2);
        def(BLACKSTONE, 3);
        def(END_STONE, 3);

        //drops
        def(ROTTEN_FLESH, 2);
        def(BONE, 5);
        def(ARROW, 5);
        def(STRING, 2);
        def(SPIDER_EYE, 5);
        def(LEATHER, 10);
        def(GUNPOWDER, 5);
        def(BLAZE_ROD, 5);
        def(SLIME_BALL, 4);
        def(COD, 3);
        def(INK_SAC, 5);
        def(GLOW_INK_SAC, 10);

        //crops
        def(BAMBOO, 2);
        def(STICK, 4);
        def(KELP, 2);
        def(CACTUS, 2);
        def(MELON, 8);
        def(VINE, 10);
        def(SUGAR_CANE, 3);
        def(POTATO, 4);
        def(WHEAT, 7);
        def(CARROT, 4);
        def(PUMPKIN, 3);
        def(BEETROOT_SEEDS, 3);
        def(BEETROOT, 5);
        def(SWEET_BERRIES, 5);
        def(CHORUS_FRUIT, 10);
        def(WHEAT_SEEDS, 3);

        save(json);
    }

    private void def(Material material, int price) {
        set(material, (short) price);
    }
}
