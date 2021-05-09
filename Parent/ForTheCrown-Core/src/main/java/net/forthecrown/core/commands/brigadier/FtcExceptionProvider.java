package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.forthecrown.core.api.Balances;
import net.forthecrown.core.utils.CrownUtils;
import net.md_5.bungee.api.ChatColor;

public interface FtcExceptionProvider {
    DynamicCommandExceptionType GENERIC = new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    DynamicCommandExceptionType CANNOT_AFFORD_TRANSACTION = new DynamicCommandExceptionType(o -> () -> ChatColor.GRAY + "Cannot afford " + ChatColor.YELLOW + o);
    SimpleCommandExceptionType CANNOT_AFFORD_INFOLESS = new SimpleCommandExceptionType(() -> ChatColor.GRAY + "Cannot afford that");

    SimpleCommandExceptionType SENDER_EMOTE_DISABLED = new SimpleCommandExceptionType(() -> ChatColor.GRAY + "You have emotes turned off.\n" + ChatColor.GRAY + "Do " + ChatColor.RESET + "/toggleemotes" + ChatColor.GRAY + " to enable them.");
    DynamicCommandExceptionType TARGET_EMOTE_DISABLED = new DynamicCommandExceptionType(o -> () -> ChatColor.GRAY + o.toString() + " has disabled emotes.");

    static CommandSyntaxException create(String messasge){
        return GENERIC.create(CrownUtils.translateHexCodes(messasge));
    }

    static CommandSyntaxException createWithContext(String message, String input, int cursor){
        StringReader reader = new StringReader(input);
        reader.setCursor(cursor);

        return createWithContext(message, reader);
    }

    static CommandSyntaxException createWithContext(String message, StringReader reader){
        return GENERIC.createWithContext(reader, CrownUtils.translateHexCodes(message));
    }

    static CommandSyntaxException cannotAfford(int amount){
        return CANNOT_AFFORD_TRANSACTION.create(Balances.getFormatted(amount));
    }

    static CommandSyntaxException cannotAfford(){
        return CANNOT_AFFORD_INFOLESS.create();
    }

    static CommandSyntaxException senderEmoteDisabled(){
        return SENDER_EMOTE_DISABLED.create();
    }

    static CommandSyntaxException targetEmoteDisabled(String name){
        return TARGET_EMOTE_DISABLED.create(name);
    }
}
