package net.forthecrown.july;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.useables.UsageCheck;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckIsNotAlt implements UsageCheck {
    public static final Key KEY = Key.key(JulyMain.inst.getName().toLowerCase(), "is_not_alt");

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException { }

    @Override
    public void parse(JsonElement element) throws CommandSyntaxException { }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{}";
    }

    @Override
    public Component failMessage() {
        return Component.text("Alt accounts cannot use this");
    }

    @Override
    public boolean test(Player player) {
        return !UserManager.inst().isAlt(player.getUniqueId());
    }

    @Override
    public JsonElement serialize() {
        return JsonNull.INSTANCE;
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }
}
