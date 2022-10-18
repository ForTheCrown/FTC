package net.forthecrown.datafix;

import org.bukkit.permissions.Permission;

import static net.forthecrown.core.Permissions.*;

class PermissionsUpdate extends DataUpdater {
    static final String GROUP_TIER_1 = "donator-tier-1";
    static final String GROUP_TIER_2 = "donator-tier-2";
    static final String GROUP_TIER_3 = "donator-tier-3";
    static final String GROUP_HELPER = "helper";
    static final String GROUP_POLICE = "police";

    protected boolean update() {
        addGroupPerm("default",    MAX_HOMES.getPermission(1));

        addGroupPerm("free-rank",  MAX_HOMES.getPermission(2));

        addGroupPerm(GROUP_TIER_1, AUTO_SELL);
        addGroupPerm(GROUP_TIER_1, CHAT_LINKS);
        addGroupPerm(GROUP_TIER_1, MAX_HOMES.getPermission(3));

        addGroupPerm(GROUP_TIER_2, CHAT_COLORS);
        addGroupPerm(GROUP_TIER_2, CMD_SUICIDE);
        addGroupPerm(GROUP_TIER_2, MAX_HOMES.getPermission(4));

        addGroupPerm(GROUP_TIER_3, CMD_BEEZOOKA);
        addGroupPerm(GROUP_TIER_3, CMD_KITTY_CANNON);
        addGroupPerm(GROUP_TIER_3, CMD_NICKNAME);
        addGroupPerm(GROUP_TIER_3, CHAT_EMOTES);
        addGroupPerm(GROUP_TIER_3, CHAT_GRADIENTS);
        addGroupPerm(GROUP_TIER_3, MAX_HOMES.getPermission(5));

        addGroupPerm(GROUP_HELPER, PUNISH_NOTES);
        addGroupPerm(GROUP_HELPER, PUNISH_SEPARATE);
        addGroupPerm(GROUP_HELPER, CMD_GAMEMODE_SPECTATOR);
        addGroupPerm(GROUP_HELPER, CMD_GAMEMODE);
        addGroupPerm(GROUP_HELPER, CMD_GET_POS);
        addGroupPerm(GROUP_HELPER, CMD_MEMORY);
        addGroupPerm(GROUP_HELPER, CMD_TELEPORT);
        addGroupPerm(GROUP_HELPER, IGNORELIST_OTHERS);
        addGroupPerm(GROUP_HELPER, CMD_LIST);
        addGroupPerm(GROUP_HELPER, NEARBY_ADMIN);
        addGroupPerm(GROUP_HELPER, PUNISH_SOFTMUTE);
        addGroupPerm(GROUP_HELPER, PUNISH_MUTE);
        addGroupPerm(GROUP_HELPER, PUNISH_KICK);
        addGroupPerm(GROUP_HELPER, PUNISH_JAIL);

        addGroupPerm(GROUP_POLICE, MARKET_WARNING);
        addGroupPerm(GROUP_POLICE, PUNISH_BAN);
        addGroupPerm(GROUP_POLICE, PUNISH_BANIP);

        return true;
    }

    static void addGroupPerm(String group, Permission permission) {
        consoleCommand("lp group %s permission set %s true", group, permission.getName());
    }
}