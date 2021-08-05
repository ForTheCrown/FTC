package net.forthecrown.august;

import com.destroystokyo.paper.ParticleBuilder;
import com.google.common.collect.ImmutableList;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.crownevents.TimerMessageFormatter;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.FtcRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class EventConstants {
    public static final String EVENT_NAMESPACE = "august_event";

    public static final FtcRegion PINATA_REGION = new FtcRegion(Worlds.OVERWORLD, -67, 64, 868, -38, 82, 900);
    public static final FtcRegion ARENA_REGION = new FtcRegion(Worlds.OVERWORLD, -75, 62, 863, -34, 83, 906);

    public static final Location EXIT = new Location(Worlds.OVERWORLD, -66.5, 74, 900.5, 90, 0);
    public static final Location START = new Location(Worlds.OVERWORLD, -63.5, 72, 872.5, -45, 12);
    public static final Location SPAWN = new Location(Worlds.OVERWORLD, -44.5, 71, 877.5);

    public static final NamespacedKey TICKET_KEY = EventUtil.createEventKey("ticket");
    public static final NamespacedKey PINATA_KEY = EventUtil.createEventKey("pinata");
    public static final NamespacedKey BEBE_KEY = EventUtil.createEventKey("bebe");

    public static final int MAX_TICKS_IN_EVENT = 1200;
    public static final int VELOCITY_BOUND = 1;
    public static final double SQUID_HEALTH = 2047;

    public static final Particle.DustOptions DUST = new Particle.DustOptions(Color.WHITE, 2);
    public static final ParticleBuilder MOVE_PARTICLE = new ParticleBuilder(Particle.REDSTONE)
            .color(Color.LIME)
            .count(1)
            .extra(1);

    public static final TimerMessageFormatter TIMER_FORMAT = (timer, time) -> text("Time left: ")
            .color(NamedTextColor.YELLOW)
            .decorate(TextDecoration.BOLD)
            .append(text(timer).color(NamedTextColor.WHITE));

    public static final ImmutableList<PinataDrop> DROPS = ImmutableList.of(
            new PinataDrop(0, 39, new ItemStack(Material.EMERALD, 1)),
            new PinataDrop(40, 59, new ItemStack(Material.GOLD_INGOT, 1)),
            new PinataDrop(60, 84, new ItemStack(Material.IRON_INGOT, 1)),

            new PinataDrop(85, 94, new ItemStack(Material.DIAMOND, 1)),
            new PinataDrop(95, 100, new ItemStack(Material.NETHERITE_SCRAP, 1))
    );

    public static final Component[] BEBE_NAMES = {
            text("P").color(RED),
            text("i").color(GOLD),
            text("n").color(YELLOW),
            text("a").color(GREEN),
            text("t").color(BLUE),
            text("a").color(LIGHT_PURPLE)
    };
    public static final Component PINATA_NAME = text()
            .append(BEBE_NAMES)
            .build();

    private static final ItemStack TICKET = new ItemStackBuilder(Material.PAPER, 1)
            .setName(
                    text("Pinata event ticket")
                            .style(FtcFormatter.nonItalic(NamedTextColor.AQUA))
            )
            .addLore(
                    text("Use this to enter the event and compete for the ")
                            .append(CrownItems.CROWN_TITLE)
                            .append(text("!"))
                            .style(FtcFormatter.nonItalic(NamedTextColor.GRAY))
            )
            .addData(TICKET_KEY, (byte) 1)
            .addEnchant(Enchantment.CHANNELING, 1)
            .setFlags(ItemFlag.HIDE_ENCHANTS)
            .build();

    public static ItemStack ticket() {
        return TICKET.clone();
    }
}
