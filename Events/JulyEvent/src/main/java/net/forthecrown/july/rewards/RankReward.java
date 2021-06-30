package net.forthecrown.july.rewards;

import net.forthecrown.core.user.CrownUser;
import net.forthecrown.core.user.UserManager;
import net.forthecrown.july.EventConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class RankReward {
    public static void checkNeedsCoolRank(Player player, long time){
        if(EventConstants.CROWN.getScoreboard().getEntries().size() < 5) return;
        CrownUser user = UserManager.getUser(player);

        if(time < EventConstants.FAST_TIME){
            user.setTabPrefix(RaceRanks.FAST_CUTIE.getPrefix());

            player.sendMessage(
                    Component.text("You were so fast you got the ")
                            .color(NamedTextColor.YELLOW)
                            .append(RaceRanks.FAST_CUTIE.prefix())
                            .append(Component.text("rank"))
            );
        } else if(time >= EventConstants.SLOW_TIME){
            user.setTabPrefix(RaceRanks.SLOWPOKE.getPrefix());

            player.sendMessage(
                    Component.text("You were so slow that you got the")
                            .color(NamedTextColor.GRAY)
                            .append(RaceRanks.SLOWPOKE.prefix())
                            .append(Component.text("rank"))
            );
        }
    }
}
