package net.forthecrown.commands.help;

import github.scarsz.discordsrv.commands.CommandLink;
import github.scarsz.discordsrv.commands.CommandUnlink;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Messages;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.Worlds;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.config.ServerRules;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.waypoint.WaypointConfig;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.Waypoints;
import net.forthecrown.waypoint.visit.WaypointVisit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static net.forthecrown.core.Messages.DYNMAP_HELP_MESSAGE;
import static net.forthecrown.core.Messages.POLEHELP_MESSAGE;

public abstract class HelpCommand extends FtcCommand {
    protected HelpCommand(String name) {
        super(name);

        setPermission(Permissions.HELP);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        command.executes(c -> {
            User user = getUserSender(c);
            TextWriter writer = TextWriters.newWriter();
            writer.write(Messages.FTC_PREFIX);

            writeDisplay(writer, user);

            user.sendMessage(writer);
            return 0;
        });
    }

    public abstract void writeDisplay(TextWriter writer, User user);

    public static void createCommands() {
        new HelpBank();
        new HelpFindPost();
        new HelpDiscord();
        new HelpIp();
        new HelpMap();
        new HelpPost();
        new HelpRules();
        new HelpSpawn();
        new HelpShop();
    }

    public static class HelpBank extends HelpCommand {

        public HelpBank(){
            super("bank");

            setAliases("bankhelp", "helpbank");
            setPermission(Permissions.HELP);
            setDescription("Shows some basic info about the Bank");

            register();
        }

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            writer.write(Messages.BANK_HELP_MESSAGE);
        }
    }

    public static class HelpFindPost extends FtcCommand {
        public HelpFindPost() {
            super("findpost");

            setAliases("findpole", "findwaypoint");
            setDescription("Shows you the nearest region pole.");
            setPermission(Permissions.HELP);

            register();
        }

        /*
         * ----------------------------------------
         * 			Command description:
         * ----------------------------------------
         * Shows the player where the nearest region pole is based
         * on the location from which they executed the command.
         *
         *
         * Valid usages of command:
         * - /findpole
         * - /findpost
         *
         * Author: Wout
         */

        @Override
        protected void createCommand(BrigadierCommand command) {
            command.executes(c ->{
                Player player = c.getSource().asPlayer();
                Location loc = player.getLocation();

                // Players in the wrong world get information:
                if (loc.getWorld().equals(Worlds.resource())) {
                    player.sendMessage(ChatColor.RED + "You are currently in the resource world!");
                    player.sendMessage(ChatColor.GRAY + "There are no regions here.");
                    player.sendMessage(ChatColor.GRAY + "Try " + ChatColor.YELLOW + "/warp portal" + ChatColor.GRAY + " to get back to the normal world.");
                    player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                    return 0;
                } else if (!loc.getWorld().equals(Worlds.overworld())) {
                    player.sendMessage(ChatColor.RED + "You are not currently in the world with regions!");
                    player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.YELLOW + "/posthelp" + ChatColor.GRAY + " for more help.");
                    return 0;
                }

                var nearestPoint = WaypointManager.getInstance()
                        .getChunkMap()
                        .findNearest(loc)
                        .left();

                if (nearestPoint == null) {
                    return 0;
                }

                var pos = nearestPoint.getBounds().center()
                                .toInt();

                player.sendMessage(
                        Component.text()
                                .append(Messages.FTC_PREFIX)
                                .append(Component.text("Closest region pole: ", NamedTextColor.YELLOW))
                                .append(Component.newline())
                                .append(
                                        Text.format("&7x&r{0} &7y&r{1} &7z&r{2}",
                                                pos.x(), pos.y(), pos.z()
                                        )
                                )
                );

                return 0;
            });
        }
    }

    public static class HelpDiscord extends FtcCommand {
        public HelpDiscord(){
            super("Discord");

            setPermission(Permissions.HELP);
            setDescription("Gives you the servers discord link.");
            register();
        }

        /*
         * Sends the player the discord link
         */

        @Override
        protected void createCommand(BrigadierCommand command){
            command.executes(c ->{
                        c.getSource().sendMessage(
                        Component.text()
                                .append(Messages.FTC_PREFIX)
                                .append(Component.text("Join our discord: "))
                                .append(
                                        Component.text(GeneralConfig.discordLink)
                                                .color(NamedTextColor.AQUA)
                                                .clickEvent(ClickEvent.openUrl(GeneralConfig.discordLink))
                                                .hoverEvent(Component.text("Click to join :D"))
                                )
                                .build()
                );
                return 0;
            })
                    .then(literal("unlink")
                            .executes(c -> {
                                CommandUnlink.execute(c.getSource().asPlayer(), new String[0]);
                                return 0;
                            })
                    )

                    .then(literal("link")
                            .executes(c -> {
                                CommandLink.execute(c.getSource().asPlayer(), new String[0]);
                                return 0;
                            })
                    );
        }
    }

    public static class HelpIp extends HelpCommand {

        public HelpIp() {
            super("Ip");

            setPermission(Permissions.DEFAULT);
            setDescription("Shows the server's IP");

            register();
        }

        /*
         * ----------------------------------------
         * 			Command description:
         * ----------------------------------------
         * Why
         *
         * Valid usages of command:
         * /Ip
         *
         * Permissions used:
         *
         * Main Author: Jules
         */

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            writer.write(Messages.IP_HELP_MESSAGE);
        }
    }

    public static class HelpMap extends HelpCommand {
        public HelpMap(){
            super("map");

            setAliases("worldmap");
            setDescription("Shows the dynmap link");

            register();
        }

        /*
         * Sends the player the dynmap link
         */

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            writer.write(DYNMAP_HELP_MESSAGE);
        }
    }

    public static class HelpPost extends HelpCommand {

        public HelpPost(){
            super("posthelp");

            setAliases("polehelp");
            setPermission(Permissions.HELP);
            setDescription("Displays info for region poles.");

            register();
        }

        /*
         * ----------------------------------------
         * 			Command description:
         * ----------------------------------------
         * Displays information about region poles.
         *
         *
         * Valid usages of command:
         * - /posthelp
         * - /polehelp
         *
         * Referenced other classes:
         * - FtcCore: FtcCore.getPrefix
         * - Findpole
         *
         * Author: Wout
         */

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            writer.write(POLEHELP_MESSAGE);
        }
    }

    public static class HelpRules extends HelpCommand {
        public HelpRules(){
            super("rules");

            setDescription("Shows you the server's rules");
            setPermission(Permissions.HELP);

            register();
        }

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            writer.write(ServerRules.display());
        }
    }

    public static class HelpSpawn extends FtcCommand {

        public HelpSpawn(){
            super("spawn");

            setPermission(Permissions.HELP);
            setDescription("Shows info about spawn");

            register();
        }

        /*
         * ----------------------------------------
         * 			Command description:
         * ----------------------------------------
         * Explains how to get to spawn.
         *
         *
         * Valid usages of command:
         * - /spawn
         *
         * Author: Wout
         */

        public static final Component MESSAGE = Component.text()
                .append(Messages.FTC_PREFIX)
                .append(Component.text("Info about spawn: ").color(NamedTextColor.YELLOW))

                .append(Component.newline())
                .append(Component.text("Spawn is called Hazelguard, you can tp to it using region poles."))

                .append(Component.newline())
                .append(Component.text("Use"))
                .append(Component.text(" /findpole ")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Click me :D"))
                        .clickEvent(ClickEvent.runCommand("/findpole"))
                )

                .append(Component.text("to find the closest pole."))

                .append(Component.newline())
                .append(Component.text("Then use"))
                .append(Component.text(" /visit Hazelguard or /spawn ")
                        .color(NamedTextColor.YELLOW)
                        .hoverEvent(Component.text("Click Me :D"))
                        .clickEvent(ClickEvent.runCommand("/vr Hazelguard"))
                )

                .append(Component.newline())
                .append(Component.text("[For more help, click here!]")
                        .clickEvent(ClickEvent.runCommand("/posthelp"))
                        .hoverEvent(Component.text("Click me for region pole info :D"))
                        .color(NamedTextColor.GRAY)
                )
                .build();

        @Override
        protected void createCommand(BrigadierCommand command) {
            command.executes(c ->{
                User sender = getUserSender(c);

                var spawnWaypoint = WaypointManager.getInstance()
                                .get(WaypointConfig.spawnWaypoint);

                var nearest = Waypoints.getColliding(sender.getPlayer());

                if (spawnWaypoint != null
                        && nearest != null
                        && nearest.getBounds().contains(sender.getPlayer())
                ) {
                    WaypointVisit.visit(sender, spawnWaypoint);
                    return 0;
                }

                sender.sendMessage(MESSAGE);
                return 0;
            });
        }
    }

    public static class HelpShop extends HelpCommand {

        public HelpShop(){
            super("shophelp");

            setAliases("helpshop");
            setPermission(Permissions.HELP);
            setDescription("Shows info on creating shops");

            register();
        }

        @Override
        public void writeDisplay(TextWriter writer, User user) {
            Component editMessage = Component.text("[editshop]").clickEvent(ClickEvent.runCommand("/editshop"));

            Component message = Component.text()
                    .append(Component.text("Sign Shop info:").color(NamedTextColor.YELLOW))
                    .append(Component.newline())

                    .append(Component.text("Sign shops can be created anywhere by anyone :D"))
                    .append(Component.newline())

                    .append(Component.text("To create a shop:"))
                    .append(Component.newline())

                    .append(lineText(1,
                            Component.text("[buy]")
                                    .color(NamedTextColor.YELLOW)
                                    .append(Component.text(" or ").color(NamedTextColor.GRAY))
                                    .append(Component.text("[sell]"))
                    ))
                    .append(lineText(2, Component.text("Can be anything").color(NamedTextColor.GRAY)))
                    .append(lineText(3, Component.text("Can be anything").color(NamedTextColor.GRAY)))
                    .append(lineText(4, Component.text("Item Price").color(NamedTextColor.YELLOW)))

                    .append(Component.text("If you're a Tier-1 Donator, you can use "))
                    .append((user.hasPermission(Permissions.SHOP_EDIT) ? editMessage : Component.text("/editshop", NamedTextColor.YELLOW)))
                    .append(Component.text(" to make changes to your shop."))

                    .build();

            writer.write(message);
        }

        private static Component lineText(int line, Component info){
            return Component.text()
                    .append(Component.text("Line " + line + ": ").color(NamedTextColor.GOLD))
                    .append(info)
                    .append(Component.newline())
                    .build();
        }
    }
}