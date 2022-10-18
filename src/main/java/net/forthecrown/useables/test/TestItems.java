package net.forthecrown.useables.test;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextJoiner;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.*;
import net.forthecrown.useables.util.UsageUtil;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TestItems extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestItems> TYPE_HAS = UsageType.of(TestItems.class)
            .setSuggests(UsageUtil::suggestItems);

    public static final UsageType<TestItems> TYPE_HAS_NOT = UsageType.of(TestItems.class)
            .setSuggests(UsageUtil::suggestItems);

    private final ImmutableList<ItemStack> items;

    /** If true, means players MUST have the items, if false, they MUST NOT have the items */
    private final boolean requires;

    public TestItems(UsageType<TestItems> type, ImmutableList<ItemStack> items) {
        super(type);

        this.items = items;
        requires = type == TYPE_HAS;
    }

    @Override
    public @Nullable Component displayInfo() {
        return TextJoiner.onComma()
                .add(items.stream().map(Text::itemAndAmount))
                .asComponent();
    }

    @Override
    public @Nullable Tag save() {
        return UsageUtil.saveItems(items);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        var inventory = player.getInventory();

        for (var i: items) {
            if (ItemStacks.isEmpty(i)) {
                continue;
            }

            boolean contains = inventory.containsAtLeast(i, i.getAmount());

            if (requires != contains) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
        return Text.format("You must {0}have these items:\n{1}",
                NamedTextColor.GRAY,

                requires ? "" : "not ",

                TextJoiner.on("\n- ")
                        .add(items.stream()
                                .map(Text::itemAndAmount)
                        )
        );
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static TestItems parse(UsageType<TestItems> type, StringReader reader, CommandSource source)
            throws CommandSyntaxException
    {
        return new TestItems(type, UsageUtil.parseItems(reader, source));
    }

    @UsableConstructor(ConstructType.JSON)
    public static TestItems fromJson(UsageType<TestItems> type, JsonElement element) {
        return new TestItems(type, ImmutableList.of(JsonUtils.readItem(element)));
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestItems load(UsageType<TestItems> type, Tag tag) {
        return new TestItems(type, UsageUtil.loadItems(tag));
    }
}