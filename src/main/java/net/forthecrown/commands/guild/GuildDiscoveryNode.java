package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.menu.GuildMenus;
import net.forthecrown.utils.text.writer.TextWriter;

class GuildDiscoveryNode extends GuildCommandNode {
    protected GuildDiscoveryNode() {
        super("guilddiscover", "discover");
        setAliases("discoverguilds", "guilddiscovery");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("discover", "Opens a menu to find guilds to join!");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command.executes(c -> {
            var user = getUserSender(c);

            GuildMenus.open(
                    GuildMenus.DISCOVERY_MENU,
                    user,
                    null
            );
            return 0;
        });
    }
}