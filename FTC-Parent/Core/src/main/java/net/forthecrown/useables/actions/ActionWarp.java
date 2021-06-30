package net.forthecrown.useables.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.arguments.WarpType;
import net.forthecrown.useables.UsageAction;
import net.forthecrown.useables.warps.Warp;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.data.UserTeleport;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.CompletableFuture;

public class ActionWarp implements UsageAction {
    public static final Key KEY = Key.key(CrownCore.inst(), "warp");

    private Key warpKey;

    @Override
    public void parse(CommandContext<CommandSource> context, StringReader reader) throws CommandSyntaxException {
        parse(reader);
    }

    @Override
    public void onInteract(Player player) {
        Warp warp = CrownCore.getWarpRegistry().get(warpKey);
        if(warp == null){
            CrownCore.logger().warning("Warp action is null");
            return;
        }

        UserManager.getUser(player).createTeleport(warp::getDestination, true, UserTeleport.Type.WARP);
    }

    @Override
    public void parse(JsonElement json) throws CommandSyntaxException {
        parse(new StringReader(json.getAsString()));
    }

    private void parse(StringReader reader) throws CommandSyntaxException {
        warpKey = CrownCore.getWarpRegistry().get(CrownUtils.parseKey(reader)).key();
    }

    @Override
    public String asString() {
        return getClass().getSimpleName() + "{key=" + String.valueOf(warpKey) + "}";
    }

    @Override
    public JsonElement serialize() {
        return new JsonPrimitive(warpKey.asString());
    }

    @Override
    public @NonNull Key key() {
        return KEY;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return WarpType.warp().listSuggestions(context, builder, true);
    }

    public Key getWarpKey() {
        return warpKey;
    }

    public void setWarpKey(Key warpKey) {
        this.warpKey = warpKey;
    }
}
