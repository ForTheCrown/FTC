package net.forthecrown.august;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.crownevents.CrownEventUtils;
import net.forthecrown.crownevents.TimerMessageFormatter;
import net.forthecrown.inventory.CrownItems;
import net.forthecrown.utils.ItemStackBuilder;
import net.forthecrown.utils.Worlds;
import net.forthecrown.utils.math.FtcRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;

public class EventConstants {
    public static final String EVENT_NAMESPACE = "august_event";

    public static final Objective CROWN = CrownEventUtils.getCrownObjective();

    public static final FtcRegion PINATA_REGION = new FtcRegion(Worlds.OVERWORLD, -67, 64, 868, -38, 82, 900);
    public static final FtcRegion SAFE_ZONE = new FtcRegion(Worlds.OVERWORLD, -62, 67, 875, -44, 81, 893);
    public static final FtcRegion ARENA_REGION = new FtcRegion(Worlds.OVERWORLD, -75, 62, 863, -34, 83, 906);

    public static final Location EXIT = new Location(Worlds.OVERWORLD, -66.5, 74, 900.5, 90, 0);
    public static final Location START = new Location(Worlds.OVERWORLD, -63, 72, 872, -45, 12);
    public static final Location MIDDLE = new Location(Worlds.OVERWORLD, -51.5, 78, 884.5);
    public static final Location SPAWN = new Location(Worlds.OVERWORLD, -44.5, 71, 877.5);

    public static final NamespacedKey TICKET_KEY = EventUtil.createEventKey("ticket");
    public static final NamespacedKey PINATA_KEY = EventUtil.createEventKey("pinata");

    public static final int MAX_TICKS_IN_EVENT = 1200;
    public static final int VELOCITY_BOUND = 2;
    public static final double SQUID_HEALTH = 2047;

    public static final net.minecraft.network.chat.Component PLUS_ONE;

    static {
        MutableComponent component = new TextComponent("+1");
        component.withStyle(ChatFormatting.YELLOW);

        PLUS_ONE = component;
    }

    public static final TimerMessageFormatter TIMER_FORMAT = (timer, time) -> Component.text("Time left: ")
            .color(NamedTextColor.YELLOW)
            .decorate(TextDecoration.BOLD)
            .append(Component.text(timer).color(NamedTextColor.WHITE));

    public static final ImmutableList<PinataDrop> DROPS = ImmutableList.of(
            new PinataDrop(0, 39, new ItemStack(Material.EMERALD, 1)),
            new PinataDrop(40, 59, new ItemStack(Material.GOLD_INGOT, 1)),
            new PinataDrop(60, 79, new ItemStack(Material.IRON_INGOT, 1)),

            new PinataDrop(80, 89, new ItemStack(Material.DIAMOND, 1)),
            new PinataDrop(90, 100, new ItemStack(Material.NETHERITE_INGOT, 1))
    );

    private static final ItemStack TICKET = new ItemStackBuilder(Material.PAPER, 1)
            .setName(
                    Component.text("Pinata event ticket")
                            .style(ChatFormatter.nonItalic(NamedTextColor.AQUA))
            )
            .addLore(
                    Component.text("Use this to enter the event and compete for the ")
                            .append(CrownItems.CROWN_TITLE)
                            .append(Component.text("!"))
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
