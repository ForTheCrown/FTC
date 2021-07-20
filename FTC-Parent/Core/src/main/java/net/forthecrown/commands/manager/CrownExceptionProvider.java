package net.forthecrown.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.forthecrown.grenadier.exceptions.TranslatableExceptionType;
import net.kyori.adventure.text.Component;

/**
 * Class for storing exception constants
 */
public class CrownExceptionProvider {
    static final DynamicCommandExceptionType GENERIC =                  new DynamicCommandExceptionType(o -> new LiteralMessage(o.toString()));

    static final UserCommandExceptionType CANNOT_MUTE =                 new UserCommandExceptionType(u -> Component.text("Cannot mute ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_BAN =                  new UserCommandExceptionType(u -> Component.text("Cannot ban ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_KICK =                 new UserCommandExceptionType(u -> Component.text("Cannot kick ").append(u.displayName()));
    static final UserCommandExceptionType CANNOT_JAIL =                 new UserCommandExceptionType(u -> Component.text("Cannot jail ").append(u.displayName()));

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

    static final TranslatableExceptionType NICK_TOO_LONG =              new TranslatableExceptionType("commands.nickTooLong");

    static final TranslatableExceptionType MUST_BE_HOLDING_ITEM =       new TranslatableExceptionType("commands.mustHoldItem");

    static final TranslatableExceptionType NO_RETURN =                  new TranslatableExceptionType("commands.noBackLoc");
    static final TranslatableExceptionType ALREADY_BARON =              new TranslatableExceptionType("commands.alreadyBaron");
    static final TranslatableExceptionType HOLDING_COINS =              new TranslatableExceptionType("commands.holdCoins");

    static final TranslatableExceptionType INV_FULL =                   new TranslatableExceptionType("commands.invFull");
    static final TranslatableExceptionType NO_ONE_NEARBY =              new TranslatableExceptionType("commands.noOneNearby");

    static final TranslatableExceptionType BLOCKED_PLAYER =             new TranslatableExceptionType("user.blocked");
    static final TranslatableExceptionType EMPTY_GRAVE =                new TranslatableExceptionType("user.grave.empty");
    static final TranslatableExceptionType IGNORE_SELF_NO =             new TranslatableExceptionType("user.cannotIgnoreSelf");

    static final TranslatableExceptionType NO_HOMES =                   new TranslatableExceptionType("homes.noneToList");
    static final TranslatableExceptionType NO_DEF_HOME =                new TranslatableExceptionType("homes.noDefaultHome");
    static final TranslatableExceptionType HOME_NAME_IN_USE =           new TranslatableExceptionType("homes.nameInUse");
    static final TranslatableExceptionType OVER_HOME_LIMIT =            new TranslatableExceptionType("homes.overLimit");

    static final TranslatableExceptionType CANNOT_TPA =                 new TranslatableExceptionType("tpa.no");
    static final TranslatableExceptionType CANNOT_TPA_HERE =            new TranslatableExceptionType("tpa.noHere");
    static final TranslatableExceptionType CANNOT_RETURN =              new TranslatableExceptionType("commands.cannotReturn");
    static final TranslatableExceptionType CANNOT_TP_HOME =             new TranslatableExceptionType("homes.badWorld");
    static final TranslatableExceptionType CANNOT_SET_HOME =            new TranslatableExceptionType("homes.cannotSetHere");

    static final TranslatableExceptionType MARRIED_SENDER =             new TranslatableExceptionType("marriage.alreadyMarried.sender");
    static final TranslatableExceptionType MARRIED_TARGET =             new TranslatableExceptionType("marriage.alreadyMarried.target");
    static final TranslatableExceptionType MARRIAGE_CANNOT_CHANGE =     new TranslatableExceptionType("marriage.cannotChange.sender");
    static final TranslatableExceptionType MARRIAGE_CANNOT_CHANGE_T =   new TranslatableExceptionType("marriage.cannotChange.target");
    static final TranslatableExceptionType NOT_MARRIED =                new TranslatableExceptionType("marriage.notMarried");

    static final TranslatableExceptionType NOT_PIRATE =                 new TranslatableExceptionType("pirates.exclusive");
    static final TranslatableExceptionType GOTTA_BE_PIRATE =            new TranslatableExceptionType("pirates.wrongBranch");
}
