package net.forthecrown.useables.actions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.admin.CommandTeleportExact;
import net.forthecrown.commands.economy.CommandShopHistory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.types.args.ArgsArgument;
import net.forthecrown.grenadier.types.args.Argument;
import net.forthecrown.grenadier.types.args.ParsedArgs;
import net.forthecrown.grenadier.types.pos.Position;
import net.forthecrown.grenadier.types.pos.PositionArgument;
import net.forthecrown.useables.*;
import net.forthecrown.user.UserTeleport;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.Tag;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ActionTeleport extends UsageAction {

    private static final Argument<Position> POS_ARG = Argument.builder("pos", PositionArgument.position())
            .setDefaultValue(Position.SELF)
            .build();

    private static final ArgsArgument ARGS = ArgsArgument.builder()
            .addOptional(CommandTeleportExact.YAW)
            .addOptional(CommandTeleportExact.PITCH)
            .addOptional(CommandTeleportExact.WORLD)
            .addOptional(POS_ARG)
            .build();

    // --- TYPE ---
    public static final UsageType<ActionTeleport> TYPE = UsageType.of(ActionTeleport.class)
            .setSuggests(ARGS::listSuggestions);

    private final Location location;

    public ActionTeleport(Location location) {
        super(TYPE);
        this.location = location.clone();
    }

    @Override
    public void onUse(Player player, ActionHolder holder) {
        var user = Users.get(player);

        if (!user.canTeleport()) {
            player.teleport(location);
            return;
        }

        user.createTeleport(() -> location, UserTeleport.Type.TELEPORT)
                .setSilent(holder.isSilent())
                .setDelayed(false)
                .start();
    }

    @Override
    public @Nullable Component displayInfo() {
        return Text.clickableLocation(location, true);
    }

    @Override
    public @Nullable Tag save() {
        return TagUtil.writeLocation(location);
    }

    // --- TYPE CONSTRUCTORS ---

    @UsableConstructor(ConstructType.PARSE)
    public static ActionTeleport parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
        ParsedArgs args = CommandShopHistory.EMPTY;

        if (reader.canRead()) {
            args = ARGS.parse(reader);
        }

        var loc = source.getLocation();
        args.get(POS_ARG).apply(loc);

        loc.setYaw(args.getOrDefault(CommandTeleportExact.YAW, loc.getYaw()));
        loc.setPitch(args.getOrDefault(CommandTeleportExact.PITCH, loc.getPitch()));

        return new ActionTeleport(loc);
    }

    @UsableConstructor(ConstructType.TAG)
    public static ActionTeleport load(Tag tag) {
        return new ActionTeleport(TagUtil.readLocation(tag));
    }
}