package net.forthecrown.core.commands.docs;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.forthecrown.command.FtcCommand;
import net.forthecrown.core.commands.HelpArgument;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.GrenadierCommand;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.options.ArgumentOption;
import net.forthecrown.grenadier.types.options.FlagOption;
import net.forthecrown.grenadier.types.options.Options;
import net.forthecrown.grenadier.types.options.OptionsArgument;
import net.forthecrown.grenadier.types.options.ParsedOptions;
import net.forthecrown.text.Text;
import net.forthecrown.utils.io.PathUtil;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandDocGen extends FtcCommand {

  private static final FlagOption REMOVE_SQUARE_BRACKETS = Options.flag("remove-square-brackets");
  private static final FlagOption GEN_HEADER = Options.flag("add-wiki-header");
  private static final FlagOption GEN_ID_TAGS = Options.flag("add-id-html-tags");
  private static final FlagOption GEN_TOC = Options.flag("add-table-of-contents");

  private static final Map<String, Boolean> TYPE_MAP
      = Map.of("singleton", true, "separated", false);

  private static final ArgumentOption<Boolean> TYPE
      = Options.argument(ArgumentTypes.map(TYPE_MAP), "type");

  private static final ArgumentOption<String> OUTPUT_FILE
      = Options.argument(StringArgumentType.string())
      .setLabel("output-file")
      .setDefaultValue("singleton.md")
      .setSuggester((context, builder) -> {
        return Completions.suggest(builder, "'singleton.md'", "'_index.md'");
      })
      .build();

  private static final ArgumentOption<List<String>> EXCLUDED
      = Options.argument(ArgumentTypes.array(new HelpArgument()))
      .setLabel("excluded")
      .setDefaultValue(List.of())
      .build();

  private static final ArgumentOption<List<String>> INCLUDED
      = Options.argument(ArgumentTypes.array(new HelpArgument()))
      .setLabel("included")
      .setDefaultValue(List.of())
      .build();

  private static final OptionsArgument OPTIONS = OptionsArgument.builder()
      .addFlag(GEN_HEADER)
      .addFlag(REMOVE_SQUARE_BRACKETS)
      .addFlag(GEN_ID_TAGS)
      .addFlag(GEN_TOC)
      .addRequired(TYPE)
      .addOptional(OUTPUT_FILE)
      .addOptional(EXCLUDED)
      .addOptional(INCLUDED)
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
    boolean singleton     = options.getValueOptional(TYPE).orElse(true);
    String outFileName    = options.getValue(OUTPUT_FILE);

    assert outFileName != null;

    CommandDocs docs = new CommandDocs();
    docs.setRemoveSquareBrackets(options.has(REMOVE_SQUARE_BRACKETS));
    docs.setGenWikiHeader(options.has(GEN_HEADER));
    docs.setGenIdTags(options.has(GEN_ID_TAGS));
    docs.setGenContentTable(options.has(GEN_TOC));
    docs.setExcluded(options.getValue(EXCLUDED));
    docs.setIncluded(options.getValue(INCLUDED));

    docs.fill();

    Path pluginDir = PathUtil.pluginPath();
    Path docDir = pluginDir.resolve("cmd-docs");

    Path output = singleton
        ? docDir.resolve(outFileName)
        : docDir;

    try {

      if (singleton) {
        docs.writeSingleton(output);
      } else {
        docs.writeSeparated(output);
      }
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }

    context.getSource().sendSuccess(
        Text.format("Generated command documentation in folder '&f{0}&r'",
            NamedTextColor.GRAY,
            docDir
        )
    );
  }
}