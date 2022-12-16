package net.forthecrown.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.chat.ChatArgument;
import net.forthecrown.commands.arguments.chat.MessageArgument;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.user.User;
import net.forthecrown.waypoint.Waypoint;
import net.kyori.adventure.text.Component;

import java.util.List;

public interface Arguments {
    ChatArgument CHAT = new ChatArgument();

    HolidayArgument HOLIDAY = new HolidayArgument();

    HomeArgument HOME = new HomeArgument();

    MarketArgument MARKET = new MarketArgument();

    RewardRangeArgument REWARD_RANGE = new RewardRangeArgument();

    TriggerArgument TRIGGER = new TriggerArgument();

    UseCmdArgument<Warp> WARPS = new UseCmdArgument<>(
            () -> Usables.getInstance().getWarps(), Warp.class
    );
    UseCmdArgument<Kit> KITS = new UseCmdArgument<>(
            () -> Usables.getInstance().getKits(), Kit.class
    );

    UserArgument USER         = new UserArgument(false, true);
    UserArgument USERS        = new UserArgument(true, true);
    UserArgument ONLINE_USERS = new UserArgument(true, false);
    UserArgument ONLINE_USER  = new UserArgument(false, false);

    MessageArgument MESSAGE = new MessageArgument();

    SellMaterialArgument SELL_MATERIAL = new SellMaterialArgument();

    FtcKeyArgument FTC_KEY = new FtcKeyArgument();

    GuildArgument GUILD = new GuildArgument();

    WaypointArgument WAYPOINT = new WaypointArgument();

    ScriptArgument SCRIPT = new ScriptArgument();

    static List<User> getUsers(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.getUsers(c.getSource(), true);
    }

    static User getUser(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.get(c.getSource(), true);
    }

    static Waypoint getWaypoint(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        ParseResult<Waypoint> result = c.getArgument(argument, ParseResult.class);
        return result.get(c.getSource(), true);
    }

    static Waypoint getWaypointNoChecks(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        ParseResult<Waypoint> result = c.getArgument(argument, ParseResult.class);
        return result.get(c.getSource(), false);
    }

    static Component getMessage(CommandContext<CommandSource> c, String arg) {
        var source = c.getSource();
        return c.getArgument(arg, MessageArgument.Result.class)
                .format(source.asBukkit());
    }
}