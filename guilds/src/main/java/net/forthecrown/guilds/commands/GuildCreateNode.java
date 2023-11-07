package net.forthecrown.guilds.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.command.help.UsageFactory;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.text.Text;

class GuildCreateNode extends GuildCommandNode {

  public GuildCreateNode() {
    super("guildcreate", "create");
    setAliases("createguild", "gcreate");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("<name>", "Creates a new guild with the name");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command
        .then(argument("string", StringArgumentType.word())
            .executes(c -> {
              var user = getUserSender(c);

              if (Guilds.getGuild(user) != null) {
                throw GuildExceptions.ALREADY_IN_GUILD;
              }

              var name = c.getArgument("string", String.class);
              Guilds.validateName(name);

              Guilds.getManager()
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