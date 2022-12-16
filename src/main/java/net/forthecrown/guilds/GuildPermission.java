package net.forthecrown.guilds;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

@RequiredArgsConstructor
public enum GuildPermission {
    CAN_CHANGE_PUBLIC(
            "able to set the guild to Public or Private",
            "Public",
            "Allow changing the guild to public or invitation required.",
            "Deny changing the guild to public or invitation required",
            "Permission to set the guild to Public or Private."
    ),

    CAN_CHANGE_VISIT(
            "able to set the guild's visit location to Public or Private",
            "Visit",
            "Allow changing the visit location to public or private",
            "Deny changing the visit location to public or private.",
            "Permission to set the guild's visit location to Public or Private."
    ),

    CAN_CHANGE_GUILD_COSMETICS(
            "able to set guild cosmetics",
            "Cosmetics",
            "Allow changing the guild's cosmetics.",
            "Deny changing the guild's cosmetics.",
            "Permission to change the guild's cosmetics."
    ),

    CAN_CHANGE_BANNER(
            "able to change the guild's banner",
            "Banner",
            "Allow changing the guild's banner.",
            "Deny changing the guild's banner.",
            "Permission to change the guild's banner."
    ),

    CAN_CHANGE_RANKS(
            "able to edit guild ranks",
            "RankEdit",
            "Allow editing the guild's ranks.",
            "Deny editing the guild's ranks.",
            "Permission to edit the guild's ranks."
    ),
    CAN_CHANGE_EFFECT(
            "able to pick the guild's chunk effects",
            "Effects",
            "Allow picking the guild's chunk effects.",
            "Deny picking the guild's chunk effects.",
            "Permission to pick the guild's chunk effects."
    ),
    CAN_RENAME(
            "able to rename the guild",
            "Rename",
            "Allow renaming the guild.",
            "Deny renaming the guild.",
            "Permission to rename the guild."
    ),
    CAN_RELOCATE(
            "able to relocate the guild's visit location",
            "Location",
            "Allow changing the guild's visit location.",
            "Deny changing the guild's visit location.",
            "Permission to change the guild's visit location."
    ),
    CAN_KICK(
            "able to kick guild members",
            "Kick",
            "Allow kicking other guild members.",
            "Deny kicking other guild members.",
            "Permission to kick other guild members."
    ),
    CAN_INVITE(
            "able to invite players into the guild",
            "Invite",
            "Allow inviting players to the guild.",
            "Deny inviting players to the guild.",
            "Permission to invite players to the guild."
    ),
    CAN_RERANK(
            "able to promote and demote guild members",
            "RankAssign",
            "Allow promoting and demoting guild members.",
            "Deny promoting and demoting guild members.",
            "Permission to promote and demote guild members."
    ),
    CAN_CLAIM_CHUNKS(
            "able to claim and unclaim chunks",
            "Chunks",
            "Allow claiming and unclaiming chunks.",
            "Deny claiming and unclaiming chunks.",
            "Permission to claim and unclaim chunks."
    ),
    ;

    @Getter final String messageFormat;

    GuildPermission(String message, String displayName, String on, String off, String desc) {
        this.messageFormat = "N{1} " +  message;

        GuildPermissionsBook.addPermission(
                new GuildPermissionsBookOption(
                        this,
                        "/guild toggleperm %perm %rank %guild",
                        Component.text(displayName),
                        on,
                        off,
                        desc
                )
        );

    }
}