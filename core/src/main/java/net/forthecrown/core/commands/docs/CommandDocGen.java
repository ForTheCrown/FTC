package net.forthecrown.core.commands.docs;

import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.Component;

public class CommandDocGen extends FtcCommand {

  private static final FlagOption GEN_HEADER = Options.flag("add-wiki-header");

  private static final Map<String, Boolean> TYPE_MAP
      = Map.of("singleton", true, "separated", false);

  private static final ArgumentOption<Boolean> TYPE
      = Options.argument(ArgumentTypes.map(TYPE_MAP), "type");

  private static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addFlag(GEN_HEADER)
      .addRequired(TYPE)
      .build();

  public CommandDocGen() {
    super("DocGen");

    setDescription("Generates documentation of all FTC commands");
    register();
  }

  @Override
  public void createCommand(GrenadierCommand command) {
    command
        .then(argument("options", OPTIONS)
            .executes(c -> {
              var opts = c.getArgument("options", ParsedOptions.class);
              opts.checkAccess(c.getSource());

              genDocs(c, opts);
              return 0;
            })
        );
  }

  private void genDocs(CommandContext<CommandSource> context, ParsedOptions options) {
    boolean singleton = options.getValueOptional(TYPE).orElse(true);
    boolean genHeader = options.has(GEN_HEADER);

    CommandDocs docs = new CommandDocs();
    docs.setGenerateWikiHeader(genHeader);

    Path pluginDir = PathUtil.pluginPath();

    Path output = singleton
        ? pluginDir.resolve("cmd-docs").resolve("singleton.md")
        : pluginDir.resolve("cmd-docs");

    try {
      docs.fill();

      if (singleton) {
        docs.writeSingleton(output);
      } else {
        docs.writeSeparated(output);
      }
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }

    context.getSource().sendSuccess(Component.text("Command documentation generated"));
  }
}