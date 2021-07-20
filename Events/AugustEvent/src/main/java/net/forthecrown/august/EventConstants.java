package net.forthecrown.august;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.crownevents.CrownEventUtils;
import net.forthecrown.crownevents.TimerMessageFormatter;
import net.forthecrown.squire.Squire;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.FtcRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;

public class EventConstants {
    public static final Objective CROWN = CrownEventUtils.getCrownObjective();
    public static final FtcRegion PINATA_REGION = new FtcRegion(Worlds.OVERWORLD, -67, 64, 868, -38, 82, 900);

    public static final Location EXIT = new Location(Worlds.OVERWORLD, 1, 1, 1);
    public static final Location START = new Location(Worlds.OVERWORLD, 1, 1,1);

    public static final NamespacedKey TICKET_KEY = Squire.createKey("august_event", "ticket");
    public static final NamespacedKey PINATA_KEY = Squire.createKey("august_event", "pinata");

    public static final TimerMessageFormatter TIMER_FORMAT = (timer, time) -> Component.text("Time left: ")
            .color(NamedTextColor.YELLOW)
            .decorate(TextDecoration.BOLD)
            .append(Component.text(timer).color(NamedTextColor.WHITE));

    private static final ItemStack TICKET = new ItemStackBuilder(Material.FLOWER_BANNER_PATTERN, 1)
            .setName(
                    Component.text("Pinata event ticket")
                            .style(ChatFormatter.nonItalic(NamedTextColor.AQUA))
            )
            .addLore(
                    Component.text("Use this to enter the event and compete!")
                            .style(ChatFormatter.nonItalic(NamedTextColor.GRAY))
            )
            .addData(TICKET_KEY, (byte) 1)
            .addEnchant(Enchantment.CHANNELING, 1)
            .setFlags(ItemFlag.HIDE_ENCHANTS)
            .build();

    public static ItemStack ticket() {
        return TICKET.clone();
    }
}
