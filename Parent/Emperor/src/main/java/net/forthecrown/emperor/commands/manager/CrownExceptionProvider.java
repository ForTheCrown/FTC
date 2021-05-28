package net.forthecrown.emperor.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.kyori.adventure.text.Component;

public class CrownExceptionProvider {
    static final DynamicCommandExceptionType GENERIC =                  new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    static final TranslatableExceptionType NO_REPLY_TARGETS =           new TranslatableExceptionType("commands.noReply");

    static final TranslatableExceptionType CANNOT_AFFORD_TRANSACTION =  new TranslatableExceptionType("commands.cannotAfford");
    static final TranslatableExceptionType CANNOT_AFFORD_INFOLESS =     new TranslatableExceptionType("commands.cannotAffordInfoless");

    static final TranslatableExceptionType SENDER_EMOTE_DISABLED =      new TranslatableExceptionType("commands.senderEmoteDisabled");
    static final TranslatableExceptionType TARGET_EMOTE_DISABLED =      new TranslatableExceptionType("commands.targetEmoteDisabled");

    static final TranslatableExceptionType SENDER_TPA_DISABLED =        new TranslatableExceptionType("commands.senderTpaDisabled");
    static final TranslatableExceptionType TARGET_TPA_DISABLED =        new TranslatableExceptionType("commands.targetTpaDisabled");

    static final TranslatableExceptionType SENDER_PAY_DISABLED =        new TranslatableExceptionType("commands.senderPayDisabled");
    static final TranslatableExceptionType TARGET_PAY_DISABLED =        new TranslatableExceptionType("commands.targetPayDisabled");
    static final TranslatableExceptionType CANNOT_PAY_SELF =            new TranslatableExceptionType("commands.cannotPaySelf");

    static final TranslatableExceptionType CANNOT_TELEPORT =            new TranslatableExceptionType("commands.cannotTeleport");
    static final TranslatableExceptionType CANNOT_TP_TO_SELF =          new TranslatableExceptionType("commands.cannotTpToSelf");

    static final TranslatableExceptionType NO_TP_REQUESTS_INFOLESS =    new TranslatableExceptionType("commands.noTpReqInfoless");
    static final TranslatableExceptionType NO_TP_INCOMING =             new TranslatableExceptionType("commands.noTpIncoming");
    static final TranslatableExceptionType NO_TP_OUTGOING =             new TranslatableExceptionType("commands.noTpOutgoing");
    static final TranslatableExceptionType ALREADY_SENT =               new TranslatableExceptionType("commands.tpaAlreadySent");

    static final UserCommandExceptionType CANNOT_MUTE =                 new UserCommandExceptionType(u -> Component.text("Cannot mute ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_BAN =                  new UserCommandExceptionType(u -> Component.text("Cannot ban ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_KICK =                 new UserCommandExceptionType(u -> Component.text("Cannot kick ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_JAIL =                 new UserCommandExceptionType(u -> Component.text("Cannot jail ").append(u.displayName()));

    static final TranslatableExceptionType NICK_TOO_LONG =              new TranslatableExceptionType("commands.nickTooLong");

    static final TranslatableExceptionType MUST_BE_HOLDING_ITEM =       new TranslatableExceptionType("commands.mustHoldItem");

    static final TranslatableExceptionType NO_RETURN =                  new TranslatableExceptionType("commands.noBackLoc");
    static final TranslatableExceptionType ALREADY_BARON =              new TranslatableExceptionType("commands.alreadyBaron");
    static final TranslatableExceptionType HOLDING_COINS =              new TranslatableExceptionType("commands.holdCoins");

    static final TranslatableExceptionType BLOCKED_PLAYER =             new TranslatableExceptionType("user.blocked");
    static final TranslatableExceptionType INV_FULL =                   new TranslatableExceptionType("commands.invFull");
}
