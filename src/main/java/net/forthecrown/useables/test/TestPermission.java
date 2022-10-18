package net.forthecrown.useables.test;

import com.google.common.collect.Collections2;
import com.google.gson.JsonElement;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.useables.CheckHolder;
import net.forthecrown.useables.ConstructType;
import net.forthecrown.useables.UsableConstructor;
import net.forthecrown.useables.UsageTest;
import net.forthecrown.useables.UsageType;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class TestPermission extends UsageTest {
    public static final UsageType<TestPermission> TYPE = UsageType.of(TestPermission.class)
            .setSuggests((context, builder) -> {
                return CompletionProvider.suggestMatching(
                        builder,

                        Collections2.transform(
                                Bukkit.getPluginManager().getPermissions(),
                                Permission::getName
                        )
                );
            });

    // --- INSTANCE FIELDS ---

    private final String permission;

    public TestPermission(String permission) {
        super(TYPE);
        this.permission = permission;
    }

    @Override
    public Component displayInfo() {
        return Text.format("'{0}'", permission);
    }

    @Override
    public Tag save() {
        return StringTag.valueOf(permission);
    }

    @Override
    public boolean test(Player player, CheckHolder holder) {
        return player.hasPermission(permission);
    }

    @Override
    public Component getFailMessage(Player player, CheckHolder holder) {
        return Messages.NO_PERMISSION;
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor
    public static TestPermission parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        return new TestPermission(reader.readString());
    }

    @UsableConstructor(ConstructType.JSON)
    public static TestPermission loadJson(JsonElement element) {
        return new TestPermission(element.getAsString());
    }

    @UsableConstructor(ConstructType.TAG)
    public static TestPermission load(Tag element) {
        return new TestPermission(element.getAsString());
    }
}