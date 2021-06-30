package net.forthecrown.pirates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.loot.CrownLootTable;
import net.forthecrown.utils.loot.WeightedLootTable;
import net.forthecrown.utils.math.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class TreasureShulker extends AbstractJsonSerializer {

    private static final Key commonKey = Squire.createPiratesKey("common_loot");
    private static final Key rareKey = Squire.createPiratesKey("rare_loot");
    private static final Key specialKey = Squire.createPiratesKey("special_loot");

    private CrownLootTable commonLoot;
    private CrownLootTable rareLoot;
    private CrownLootTable specialLoot;

    private Location location;
    private UUID currentID;

    private final Set<UUID> alreadyFound = new HashSet<>();

    private final CrownRandom random = new CrownRandom();

    public TreasureShulker(){
        super("treasure_shulker");

        reload();
    }

    @Override
    protected void save(JsonObject json) {
        json.add("loc", JsonUtils.writeLocation(location));
        json.addProperty("currentID", currentID == null ? null : currentID.toString());

        json.add("commonLoot", commonLoot.serialize());
        json.add("rareLoot", rareLoot.serialize());
        json.add("specialLoot", specialLoot.serialize());

        if(!alreadyFound.isEmpty()){
            json.add("alreadyFound", JsonUtils.writeCollection(alreadyFound, id -> new JsonPrimitive(id.toString())));
        }
    }

    @Override
    protected void reload(JsonObject json) {
        this.location = JsonUtils.readLocation(json.getAsJsonObject("loc"));
        this.currentID = UUID.fromString(json.get("currentID").getAsString());

        this.commonLoot = WeightedLootTable.deserialize(json.get("commonLoot"));
        this.rareLoot = WeightedLootTable.deserialize(json.get("rareLoot"));
        this.specialLoot = WeightedLootTable.deserialize(json.get("specialLoot"));

        alreadyFound.clear();
        if(json.has("alreadyFound")){
            JsonUtils.readList(json.get("alreadyFound"), (Consumer<JsonElement>) e -> alreadyFound.add(UUID.fromString(e.getAsString())));
        }
    }

    public CrownRandom getRandom() {
        return random;
    }

    public void relocate(){
        Entity entity = Bukkit.getEntity(currentID);
        if(entity == null){
            spawn();
            return;
        }

        moveLocToRandom();

        Block block = location.getBlock();
        if(!block.getType().isAir()) block.setType(Material.AIR);

        entity.teleport(location);
    }

    public void spawn(){
        moveLocToRandom();

        CrownCore.getTreasureWorld().spawn(location, Shulker.class, shulker -> {
            currentID = shulker.getUniqueId();

            shulker.getPersistentDataContainer().set(Pirates.SHULKER_KEY, PersistentDataType.BYTE, (byte) 1);

            shulker.setPersistent(true);
            shulker.setRemoveWhenFarAway(false);
        });
    }

    public void kill(){
        if(currentID != null){
            Entity entity = Bukkit.getEntity(currentID);
            if(entity == null) return;

            entity.remove();
            return;
        }

        if(location != null){
            location.getNearbyEntitiesByType(Shulker.class, 1).forEach(s -> {
                if(!s.getPersistentDataContainer().has(Pirates.SHULKER_KEY, PersistentDataType.BYTE)) return;
                s.remove();
            });
        }
    }

    public boolean hasAlreadyFound(UUID id){
        return alreadyFound.contains(id);
    }

    public void find(UUID id){
        alreadyFound.add(id);
    }

    public Loot createLoot(Player player, Entity interacted){
        return new Loot(player, interacted.getLocation(), random, getRandomLoot());
    }

    public CrownLootTable getCommonLoot() {
        return commonLoot;
    }

    public CrownLootTable getRareLoot() {
        return rareLoot;
    }

    public CrownLootTable getSpecialLoot() {
        return specialLoot;
    }

    public CrownLootTable getRandomLoot(){
        int index = random.nextInt(100);

        if(CrownUtils.isInRange(index, 0, 20)) return getSpecialLoot();
        if(CrownUtils.isInRange(index, 20, 50)) return getRareLoot();
        return getCommonLoot();
    }

    public void moveLocToRandom(){
        int x = random.intInRange(250, 1970);
        int y = random.intInRange(40, 50);
        int z = random.intInRange(250, 1970);

        if(random.nextBoolean()) x = -x;
        if(random.nextBoolean()) z = -z;

        this.location = new Location(CrownCore.getTreasureWorld(), x, y, z);
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        moveLocToRandom();
        json.add("loc", JsonUtils.writeLocation(location));

        json.add("specialLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3847),
                location.getWorld(),
                specialKey
        ).serialize());

        json.add("rareLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3848),
                location.getWorld(),
                rareKey
        ).serialize());

        json.add("commonLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3849),
                location.getWorld(),
                commonKey
        ).serialize());


        return json;
    }

    private CrownLootTable lootTableFromChest(BlockPos pos, World world, Key key){
        Chest chest = pos.stateAs(world);
        List<ItemStack> items = new ArrayList<>();

        for (ItemStack i: chest.getBlockInventory()){
            if(i == null) continue;
            items.add(i);
        }

        return CrownLootTable.of(key, items);
    }

    public static class Loot implements HoverEventSource<Component> {

        Collection<ItemStack> items;
        int rhineReward;

        public Loot(Player player, Location location, CrownRandom random, LootTable lootTable){
            this.items = lootTable.populateLoot(random, new LootContext.Builder(location).killer(player).build());
            this.rhineReward = random.intInRange(CrownCore.getTreasureMinPrize(), CrownCore.getTreasureMaxPrize());
        }

        public boolean giveRewards(Player player){
            if(!hasSpaceForItems(player.getInventory())){
                player.sendMessage(Component.translatable("commands.invFull").color(NamedTextColor.RED));
                return false;
            }

            if(Pirates.getTreasure().hasAlreadyFound(player.getUniqueId())){
                player.sendMessage(Component.translatable("pirates.treasure.alreadyFound", NamedTextColor.GRAY));
                return false;
            }

            CrownCore.getBalances().add(player.getUniqueId(), rhineReward, false);
            items.forEach(i -> player.getInventory().addItem(i));

            player.sendMessage(display());
            return true;
        }

        private boolean hasSpaceForItems(PlayerInventory inventory){
            int freeAmount = 0;

            for (ItemStack i: inventory.getContents()){
                if(i != null) continue;

                freeAmount++;
                if(freeAmount >= items.size()) return true;
            }

            return freeAmount >= items.size();
        }

        public Component display(){
            return Component.text()
                    .color(NamedTextColor.YELLOW)
                    .append(Balances.formatted(rhineReward))
                    .append(
                            Component.text("and some items")
                                    .hoverEvent(this)
                    )
                    .build();
        }


        @Override
        public @NonNull HoverEvent<Component> asHoverEvent(@NonNull UnaryOperator<Component> op) {
            TextComponent.Builder builder = Component.text()
                    .append(Component.text("Items: "));

            for (ItemStack i: items){
                builder
                        .append(Component.newline())
                        .append(ChatFormatter.itemName(i));
            }

            return HoverEvent.showText(builder.build());
        }
    }
}
