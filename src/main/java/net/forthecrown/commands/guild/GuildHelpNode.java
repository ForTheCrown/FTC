package net.forthecrown.commands.guild;

import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

class GuildHelpNode extends GuildCommandNode {

  public GuildHelpNode() {
    super("guildhelp", "help", "?");
    setAliases("ghelp");
  }

  @Override
  public void populateUsages(UsageFactory factory) {
    factory.usage("").addInfo("Shows help information");
  }

  @Override
  protected <T extends ArgumentBuilder<CommandSource, T>> void create(T command) {
    command.executes(c -> {
      var writer = TextWriters.newWriter();
      writer.setFieldSeparator(text(" - "));
      writer.setFieldStyle(Style.style(NamedTextColor.GOLD));
      writer.setFieldValueStyle(Style.empty());

      for (var n: GuildCommands.NODES) {
        n.writeUsages(writer, c.getSource(), false);
      }

      c.getSource().sendMessage(writer);
      return 0;
    });
  }
}