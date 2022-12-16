package net.forthecrown.commands.guild;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcCommand;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.command.BrigadierCommand;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildPermission;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.writer.TextWriter;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

@Getter
abstract class GuildCommandNode extends FtcCommand {
    private final String[] argumentName;

    protected GuildCommandNode(@NotNull String name, String... argumentName) {
        super(name);
        this.argumentName = Validate.notEmpty(argumentName);

        setPermission(Permissions.GUILD);
    }

    @Override
    protected void createCommand(BrigadierCommand command) {
        create(command);
    }

    protected abstract void writeHelpInfo(TextWriter writer, CommandSource source);
    protected abstract <T extends ArgumentBuilder<CommandSource, T>> void create(T command);

    protected static RequiredArgumentBuilder<CommandSource, Guild> guildArgument() {
        return argument("guild", Arguments.GUILD);
    }

    protected static GuildProvider providerForArgument() {
        return GuildProvider.argument("guild");
    }

    protected void testPermission(User user,
                                  Guild guild,
                                  GuildPermission permission,
                                  CommandSyntaxException exception
    ) throws CommandSyntaxException {
        var member = guild.getMember(user.getUniqueId());

        if (member == null || member.hasLeft()) {
            if (!user.hasPermission(Permissions.GUILD_ADMIN)) {
                throw Exceptions.NOT_IN_GUILD;
            }

            return;
        }

        if (!member.hasPermission(permission)) {
            throw exception;
        }
    }

    protected LiteralArgumentBuilder<CommandSource> createGuildCommand(String name, GuildCommand cmd) {
        var literal = literal(name);
        addGuildCommand(literal, cmd);
        return literal;
    }

    protected <T extends ArgumentBuilder<CommandSource, T>> void addGuildCommand(T command, GuildCommand cmd) {
        command
                .executes(c -> cmd.run(c, GuildProvider.SENDERS_GUILD))

                .then(guildArgument()
                        .requires(source -> source.hasPermission(Permissions.GUILD_ADMIN))
                        .executes(c -> cmd.run(c, providerForArgument()))
                );
    }

    protected static interface GuildCommand {
        int run(CommandContext<CommandSource> c, GuildProvider provider) throws CommandSyntaxException;
    }
}