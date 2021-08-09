package net.forthecrown.pirates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.pirates.BlackMarketUtils;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.loot.CrownLootTable;
import net.forthecrown.utils.math.BlockPos;
import net.forthecrown.utils.math.MathUtil;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Score;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
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
        ForTheCrown.logger().info("Treasure Shulker loaded");

        ForTheCrown.getDayUpdate().addListener(() -> {
            alreadyFound.clear();
            relocate();
        });
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

        JsonElement element = json.get("currentID");
        if(element != null && !element.isJsonNull()) this.currentID = UUID.fromString(element.getAsString());
        else currentID = null;

        this.commonLoot = CrownLootTable.deserialize(json.get("commonLoot"));
        this.rareLoot = CrownLootTable.deserialize(json.get("rareLoot"));
        this.specialLoot = CrownLootTable.deserialize(json.get("specialLoot"));

        alreadyFound.clear();
        if(json.has("alreadyFound")){
            JsonUtils.readList(json.get("alreadyFound"), e -> UUID.fromString(e.getAsString()) , alreadyFound::add);
        }
    }

    public CrownRandom getRandom() {
        return random;
    }

    public void relocate(){
        kill();
        spawn();
    }

    public void spawn(){
        moveLocToRandom();

        Block block = location.getBlock();
        if(!block.getType().isAir()) block.setType(Material.AIR);

        ComVars.getTreasureWorld().spawn(location, Shulker.class, shulker -> {
            currentID = shulker.getUniqueId();

            shulker.getPersistentDataContainer().set(Pirates.SHULKER_KEY, PersistentDataType.BYTE, (byte) 1);
            shulker.setRemoveWhenFarAway(false);

            shulker.setAI(false);
            shulker.setInvulnerable(true);
            shulker.setColor(DyeColor.GRAY);
        });
    }

    public void kill(){
        if(currentID == null && location == null){
            ForTheCrown.logger().warning("Tried to kill treasure shulker, but both location and id were null");
            return;
        }

        if(currentID != null){
            Entity entity = Bukkit.getEntity(currentID);
            if(entity == null) return;

            entity.remove();
            return;
        }

        location.getNearbyEntitiesByType(Shulker.class, 1).forEach(s -> {
            if(!s.getPersistentDataContainer().has(Pirates.SHULKER_KEY, PersistentDataType.BYTE)) return;
            s.remove();
        });
    }

    public boolean hasAlreadyFound(UUID id){
        return alreadyFound.contains(id);
    }

    public void find(UUID id){
        alreadyFound.add(id);
    }

    public Loot createLoot(Player player, Entity interacted){
        Rarity rarity = getRandomRarity();

        return new Loot(rarity, player, interacted.getLocation(), random, getLootByRarity(rarity));
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

    public CrownLootTable getLootByRarity(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> getCommonLoot();
            case SPECIAL -> getSpecialLoot();
            case RARE -> getRareLoot();
        };
    }

    public Rarity getRandomRarity() {
        return Rarity.values()[random.intInRange(0, Rarity.values().length-1)];
    }

    public CrownLootTable getRandomLoot(){
        int index = random.nextInt(100);

        if(MathUtil.isInRange(index, 0, 20)) return getSpecialLoot();
        if(MathUtil.isInRange(index, 20, 50)) return getRareLoot();
        return getCommonLoot();
    }

    public void moveLocToRandom(){
        Location loc = getRandomLoc();
        short safeGuard = 300;

        while (loc.getBlock().getType() == Material.WATER) {
            loc = getRandomLoc();

            safeGuard--;
            if(safeGuard < 0) {
                ForTheCrown.logger().warning("Couldn't find valid location for TreasureShulker in 300 attempts");
                break;
            }
        }

        this.location = loc;
    }

    private Location getRandomLoc() {
        int x = random.intInRange(250, 1970);
        int y = random.intInRange(40, 50);
        int z = random.intInRange(250, 1970);

        if(random.nextBoolean()) x = -x;
        if(random.nextBoolean()) z = -z;

        return new Location(ComVars.getTreasureWorld(), x, y, z);
    }

    public Location getLocation() {
        Shulker shulker = getShulker();
        if(shulker != null) return shulker.getLocation();

        return location.clone();
    }

    public UUID getCurrentID() {
        return currentID;
    }

    public Shulker getShulker() {
        if(currentID == null) return null;
        return (Shulker) Bukkit.getEntity(currentID);
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        moveLocToRandom();
        json.add("loc", JsonUtils.writeLocation(location));

        json.add("specialLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3847),
                Worlds.OVERWORLD,
                specialKey
        ).serialize());

        json.add("rareLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3848),
                Worlds.OVERWORLD,
                rareKey
        ).serialize());

        json.add("commonLoot", lootTableFromChest(
                new BlockPos(-674, 59, 3849),
                Worlds.OVERWORLD,
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

    public enum Rarity {
        COMMON (1),
        RARE (2),
        SPECIAL (5);

        final int ppReward;
        Rarity(int i) {
            this.ppReward = i;
        }
    }

    public static class Loot implements HoverEventSource<Component> {

        final Rarity rarity;
        Collection<ItemStack> items;
        int rhineReward;

        public Loot(Rarity rarity, Player player, Location location, CrownRandom random, CrownLootTable lootTable){
            this.rarity = rarity;
            this.items = lootTable.populateLoot(random, new LootContext.Builder(location).killer(player).build(), ComVars.getMaxTreasureItems());
            this.rhineReward = random.intInRange(ComVars.getTreasureMinPrize(), ComVars.getTreasureMaxPrize());
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

            ForTheCrown.getBalances().add(player.getUniqueId(), rhineReward, false);
            items.forEach(i -> player.getInventory().addItem(i));

            Score pp = BlackMarketUtils.getPiratePointScore(player.getName());
            pp.setScore(pp.getScore() + rarity.ppReward);

            player.sendMessage(
                    Component.translatable("pirates.shulker.found",
                            NamedTextColor.GRAY,
                            display()
                    )
            );
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
                    .append(FtcFormatter.rhines(rhineReward))
                    .append(
                            Component.text(" and some items")
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
                        .append(FtcFormatter.itemDisplayName(i));
            }

            return HoverEvent.showText(builder.build());
        }
    }
}
