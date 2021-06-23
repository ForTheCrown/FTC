package net.forthecrown.utils.loot;

import com.google.gson.JsonObject;
import net.forthecrown.core.serializer.JsonSerializable;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Random;

public interface CrownLootTable extends LootTable, JsonSerializable {

    @Override
    JsonObject serialize();

    @Override
    @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context);

    @Override
    void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context);

    @Override
    @NotNull NamespacedKey getKey();
}
