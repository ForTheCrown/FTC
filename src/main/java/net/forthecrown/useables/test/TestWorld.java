package net.forthecrown.useables.test;

import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Text;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.WorldArgument;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.TagUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class TestWorld extends UsageTest {
    // --- TYPE ---
    public static final UsageType<TestWorld> TYPE = UsageType.of(TestWorld.class);

    private final NamespacedKey world;

    public TestWorld(World world) {
        super(TYPE);

        if (world == null) {
            Crown.logger().warn("Found unknown world while creating world usage test!");
            this.world = null;
        } else {
            this.world = world.getKey();
        }
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.format("world='{0}'", world);
    }

    @Override
    public @Nullable Tag save() {
        return TagUtil.writeKey(world);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        if (world == null) {
            return false;
        }

        return player.getWorld().getKey().equals(world);
    }

    @Override
    public @Nullable Component getFailMessage(Player player, CheckHolder holder) {
        if (world == null) {
            return Component.text("Cannot use this in this world", NamedTextColor.GRAY);
        }

        return Text.format("Can only use this in the {0}",
                NamedTextColor.GRAY,
                Text.formatWorldName(Bukkit.getWorld(world))
        );
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static TestWorld parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new TestWorld(
                WorldArgument.world().parse(reader)
        );
    }

    @UsableConstructor(ConstructType.JSON)
    public static TestWorld fromJson(JsonElement element) {
        return new TestWorld(Bukkit.getWorld(JsonUtils.readKey(element)));
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestWorld load(Tag tag) {
        return new TestWorld(Bukkit.getWorld(TagUtil.readKey(tag)));
    }
}