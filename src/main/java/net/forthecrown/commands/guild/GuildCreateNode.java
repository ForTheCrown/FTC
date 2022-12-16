package net.forthecrown.commands.guild;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;

class GuildCreateNode extends GuildCommandNode {
    public GuildCreateNode() {
        super("guildcreate", "create");
        setAliases("createguild");
    }

    @Override
    protected void writeHelpInfo(TextWriter writer, CommandSource source) {
        writer.field("create", "Creates a new guild");
    }

    @Override
    protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
        command
                .then(argument("string", StringArgumentType.word())
                        .executes(c -> {
                            var user = getUserSender(c);

                            if (user.getGuild() != null) {
                                throw Exceptions.ALREADY_IN_GUILD;
                            }

                            var name = c.getArgument("string", String.class);
                            Guilds.validateName(name);

                            GuildManager.get()
                                    .createGuild(user, name);

                            user.sendMessage(
                                    Text.renderString(
                                            "&eGuild created!&6" +
                                            "\nDo, &e/guild help&6 to get info on how guilds work."
                                    )
                            );
                            return 0;
                        })
                );
    }
}