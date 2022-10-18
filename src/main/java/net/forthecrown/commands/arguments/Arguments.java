package net.forthecrown.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.arguments.chat.ChatArgument;
import net.forthecrown.commands.arguments.chat.MessageArgument;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.useables.Usables;
import net.forthecrown.useables.command.Kit;
import net.forthecrown.useables.command.Warp;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;

import java.util.List;

public interface Arguments {
    ChatArgument CHAT = new ChatArgument();

    GlobalVarArgument COM_VAR = new GlobalVarArgument();

    HolidayArgument HOLIDAY = new HolidayArgument();

    HomeArgument HOME = new HomeArgument();

    MarketArgument MARKET = new MarketArgument();

    RegionArgument REGION = new RegionArgument();

    RewardRangeArgument REWARD_RANGE = new RewardRangeArgument();

    TriggerArgument TRIGGER = new TriggerArgument();

    UseCmdArgument<Warp> WARPS = new UseCmdArgument<>(
            () -> Usables.get().getWarps(), Warp.class
    );
    UseCmdArgument<Kit> KITS = new UseCmdArgument<>(
            () -> Usables.get().getKits(), Kit.class
    );

    UserArgument USER        = new UserArgument(false, true);
    UserArgument USERS       = new UserArgument(true, true);
    UserArgument ONLINE_USER = new UserArgument(false, false);

    MessageArgument MESSAGE = new MessageArgument();

    SellMaterialArgument SELL_MATERIAL = new SellMaterialArgument();

    FtcKeyArgument FTC_KEY = new FtcKeyArgument();

    static List<User> getUsers(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.getUsers(c.getSource(), true);
    }

    static User getUser(CommandContext<CommandSource> c, String argument) throws CommandSyntaxException {
        UserParseResult result = c.getArgument(argument, UserParseResult.class);
        return result.getUser(c.getSource(), true);
    }

    static PopulationRegion getRegion(CommandContext<CommandSource> c, String arg, boolean checkInvite) throws CommandSyntaxException {
        return c.getArgument(arg, RegionParseResult.class).getRegion(c.getSource(), checkInvite);
    }

    static PopulationRegion regionInviteIgnore(CommandContext<CommandSource> c, String arg) throws CommandSyntaxException {
        return getRegion(c, arg, false);
    }

    static Component getMessage(CommandContext<CommandSource> c, String arg) {
        var source = c.getSource();
        return c.getArgument(arg, MessageArgument.Result.class)
                .format(source.asBukkit());
    }
}