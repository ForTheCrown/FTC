package net.forthecrown.dungeons;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import lombok.Getter;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.Messages;
import net.forthecrown.core.holidays.RewardRange;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.TextJoiner;
import net.forthecrown.utils.text.format.UnitFormat;
import net.forthecrown.dungeons.boss.BossContext;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BossLootBox {
    @Getter
    private final ClaimReward reward = new ClaimReward();
    private final Map<UUID, FloatList> claimChances = new Object2ObjectOpenHashMap<>();

    public int getRemainingClaims(UUID uuid) {
        FloatList list = claimChances.get(uuid);
        return list == null ? 0 : list.size();
    }

    public void onBossDefeated(BossContext context) {
        for (var p: context.players()) {
            FloatList list = claimChances.computeIfAbsent(p.getUniqueId(), uuid -> new FloatArrayList());
            list.add(0, context.modifier());
        }
    }

    /**
     * Claims all items within this lootbox
     * @param user The user that's claiming
     */
    public void claim(User user) {
        user.ensureOnline();

        FloatList list = claimChances.get(user.getUniqueId());

        // No remaining claims
        if (list == null || list.isEmpty()) {
            return;
        }

        float lastMod = list.removeFloat(0) - BossContext.MIN_MODIFIER;
        float chance = lastMod / (GeneralConfig.maxBossDifficulty - BossContext.MIN_MODIFIER);

        int rhines = getChanced(getReward().getRhines(), chance, Util.RANDOM);
        int gems = getChanced(getReward().getGems(), chance, Util.RANDOM);

        // Just ran out of claims, remove from map
        if (list.isEmpty()) {
            claimChances.remove(user.getUniqueId());
        }

        TextJoiner joiner = TextJoiner.onComma()
                .setColor(NamedTextColor.YELLOW)
                .setPrefix(Messages.CLAIMED);

        // Give Rhines if we have any to give
        if (rhines > 0) {
            user.addBalance(rhines);
            joiner.add(
                    UnitFormat.rhines(rhines)
                            .color(NamedTextColor.GOLD)
            );
        }

        // Give gems, if there's any to give
        if (gems > 0) {
            user.addGems(gems);

            joiner.add(
                    UnitFormat.gems(gems)
                            .color(NamedTextColor.GOLD)
            );
        }

        // Loottable giving :D
        LootContext context = new LootContext.Builder(user.getLocation())
                .killer(user.getPlayer())
                .luck(lastMod)
                .build();

        Collection<ItemStack> items = getReward()
                .getLootTable()
                .populateLoot(Util.RANDOM, context);

        if (!items.isEmpty()) {
            // Place item display names in a list
            // in the hover event
            TextComponent.Builder builder = Component.text()
                    .append(Component.text("Items: "));

            for (var i: items) {
                builder
                        .append(Component.newline())
                        .append(Component.text("- "))
                        .append(Text.itemAndAmount(i));
            }

            joiner.add(
                    Component.text(items.size() + " items")
                            .hoverEvent(builder.build())

                            // This is either gonna be a lame Easter egg or
                            // freak someone out when they accidentally click on it
                            .clickEvent(ClickEvent.suggestCommand("Why'd you click on this lmao"))

                            .color(NamedTextColor.GOLD)
            );
        }

        // If there's boss items to give
        if (getReward().getBossItems() != null) {
            ItemStack bossItem = getReward().getBossItems().item();
            items.add(bossItem.clone());

            joiner.add(
                    Text.itemDisplayName(bossItem)
                            .color(NamedTextColor.GOLD)
            );
        }

        PlayerInventory inv = user.getInventory();
        Location location = user.getLocation();
        for (var i: items) {
            Util.giveOrDropItem(inv, location, i);
        }

        user.sendMessage(joiner.asComponent());
    }

    public void save(CompoundTag tag) {
        CompoundTag rewardTag = new CompoundTag();
        reward.save(rewardTag);

        tag.put("rewards", rewardTag);

        ListTag listTag = new ListTag();

        for (var e: claimChances.entrySet()) {
            IntArrayTag arrTag = NbtUtils.createUUID(e.getKey());

            if (e.getValue().isEmpty()) {
                continue;
            }

            for (var f: e.getValue()) {
                arrTag.add(IntTag.valueOf(Float.floatToIntBits(f)));
            }

            listTag.add(arrTag);
        }

        tag.put("userData", listTag);
    }

    public void load(CompoundTag tag) {
        CompoundTag rewardTag = tag.getCompound("rewards");
        reward.load(rewardTag);

        claimChances.clear();
        ListTag userData = tag.getList("userData", Tag.TAG_INT_ARRAY);

        for (var e: userData) {
            IntArrayTag intArr = (IntArrayTag) e;
            int[] arr = intArr.getAsIntArray();

            UUID id = UUIDUtil.uuidFromIntArray(arr);
            FloatList list = new FloatArrayList();

            for (int i = 4; i < arr.length; i++) {
                float f = Float.intBitsToFloat(arr[i]);
                list.add(f);
            }

            claimChances.put(id, list);
        }
    }

    public int getChanced(RewardRange range, float chance, Random random) {
        int dif = range.getSize();
        float initialResult = range.getMin() + (dif * (random.nextFloat() + chance));

        // Same rounding logic here as in RewardRange#get(Random)
        int rndValue = initialResult < 10_000 ? 100 : 1000;
        return (int) (initialResult - (initialResult % rndValue));
    }

    @Data
    public static class ClaimReward {
        private RewardRange
                rhines = RewardRange.NONE,
                gems = RewardRange.NONE;

        private LootTable lootTable = LootTables.EMPTY.getLootTable();
        private BossItems bossItems;

        public void save(CompoundTag tag) {
            if (bossItems != null) {
                tag.put("bossItems", TagUtil.writeEnum(bossItems));
            }

            tag.put("lootTable", TagUtil.writeKey(lootTable.getKey()));

            if (!rhines.isNone()) {
                tag.put("rhines", rhines.save());
            }

            if (!gems.isNone()) {
                tag.put("gems", gems.save());
            }
        }

        public void load(CompoundTag tag) {
            if (tag.contains("bossItems")) {
                this.bossItems = TagUtil.readEnum(BossItems.class, tag.get("bossItems"));
            } else {
                bossItems = null;
            }

            lootTable = Bukkit.getLootTable(TagUtil.readKey(tag.get("lootTable")));

            rhines = RewardRange.load(tag.get("rhines"));
            gems = RewardRange.load(tag.get("gems"));
        }
    }
}