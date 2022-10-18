package net.forthecrown.events.player;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.forthecrown.core.Crown;
import net.forthecrown.core.ServerIcons;
import net.forthecrown.text.Messages;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Iterator;

public class MotdListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onPaperServerListPing(PaperServerListPingEvent event) {
        int max = Bukkit.getMaxPlayers();
        int newMax = Util.RANDOM.nextInt(max, max + max / 2);

        event.setMaxPlayers(newMax);

        event.motd(motd());
        event.setServerIcon(ServerIcons.getCurrent());

        Iterator<Player> iterator = event.iterator();
        while (iterator.hasNext()) {
            User user = Users.get(iterator.next());

            // Remove vanished players from
            // preview
            if (user.get(Properties.VANISHED)) {
                iterator.remove();
            }
        }
    }

    Component motd() {
        return Component.text()
                .color(NamedTextColor.GRAY)

                .append(Component.text("For The Crown").style(Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)))
                .append(Component.text(" - "))
                .append(afterDashText())

                .append(Component.newline())
                .append(Component.text("Currently on " + Bukkit.getMinecraftVersion()))

                .build();
    }

    Component afterDashText() {
        if (Crown.inDebugMode()) {
            return Component.text("Test server").color(NamedTextColor.GREEN);
        }

        if (Bukkit.hasWhitelist()) {
            return Component.text("Maintenance").color(NamedTextColor.RED);
        }

        if (Util.RANDOM.nextInt(50) == 45) {
            return Component.text("You're amazing ")
                    .append(Messages.HEART)
                    .color(NamedTextColor.RED);
        }

        return Component.text("Survival Minecraft").color(NamedTextColor.YELLOW);
    }
}