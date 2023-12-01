package net.forthecrown.leaderboards.commands;

import static net.forthecrown.text.Text.format;
import static net.forthecrown.text.Text.formatNumber;
import static net.kyori.adventure.text.Component.text;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.forthecrown.command.Commands;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.chat.MessageSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.annotations.Argument;
import net.forthecrown.grenadier.annotations.CommandFile;
import net.forthecrown.grenadier.annotations.VariableInitializer;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ParsedPosition;
import net.forthecrown.leaderboards.BoardImpl;
import net.forthecrown.leaderboards.Leaderboard.Order;
import net.forthecrown.leaderboards.LeaderboardPlugin;
import net.forthecrown.leaderboards.LeaderboardSource;
import net.forthecrown.leaderboards.LeaderboardSources;
import net.forthecrown.leaderboards.ScoreFilter;
import net.forthecrown.leaderboards.ServiceImpl;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.text.page.PagedIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.TextDisplay.TextAlignment;
import org.spongepowered.math.vector.Vector3f;

@CommandFile("leaderboards.gcn")
public class CommandLeaderboard {

  private final PageFormat<BoardImpl> pageFormat;

  private final LeaderboardPlugin plugin;
  private final ServiceImpl service;

  public CommandLeaderboard(LeaderboardPlugin plugin) {
    this.service = plugin.getService();
    this.plugin = plugin;

    pageFormat = PageFormat.create();
    pageFormat.setHeader(text("Leaderboards"));
    pageFormat.setFooter(Footer.ofButton("/lb list %s %s"));

    pageFormat.setEntry((writer, entry, viewerIndex, context, it) -> {
      writer.write(entry.displayName());
    });
  }

  @VariableInitializer
  void initVars(Map<String, Object> vars) {
    vars.put("lb", new LeaderboardArgument(service));
    vars.put("source", new SourceArgument(service));
    vars.put("color", new ColorArgument());
    vars.put("filter", new FilterArgument());
    vars.put("alignment", ArgumentTypes.enumType(TextAlignment.class));
    vars.put("billboard", ArgumentTypes.enumType(Billboard.class));
    vars.put("order", ArgumentTypes.enumType(Order.class));
  }

  void reloadConfig(CommandSource source) {
    plugin.reloadConfig();
    source.sendSuccess(text("Reloaded Leaderboards config"));
  }

  void reloadBoards(CommandSource source) {
    service.load();
    source.sendSuccess(text("Reloaded Leaderboards"));
  }

  void saveBoards(CommandSource source) {
    service.save();
    source.sendSuccess(text("Saved leaderboards"));
  }

  void listBoards(
      CommandSource source,
      @Argument(value = "page", optional = true) Integer pageArg,
      @Argument(value = "pageSize", optional = true) Integer pageSizeArg
  ) throws CommandSyntaxException {
    List<BoardImpl> boards = new ArrayList<>(service.getBoards().values());
    boards.sort(Comparator.comparing(BoardImpl::getName));

    int page = pageArg == null ? 0 : (pageArg - 1);
    int pageSize = pageSizeArg == null ? 10 : pageSizeArg;

    Commands.ensurePageValid(page, pageSize, boards.size());

    PagedIterator<BoardImpl> it = PagedIterator.of(boards, page, pageSize);
    Component pageDisplay = pageFormat.format(it);

    source.sendMessage(pageDisplay);
  }

  void showInfo(CommandSource source, @Argument("board") BoardImpl board) {
    source.sendMessage(board.infoText());
  }

  void updateBoard(CommandSource source, @Argument("board") BoardImpl board)
      throws CommandSyntaxException
  {
    if (!board.isSpawned()) {
      throw Exceptions.create("Leaderboard has not been spawned");
    }

    board.update();
    source.sendSuccess(
        format("Updated leaderboard {0}.", NamedTextColor.GRAY, board.displayName())
    );
  }

  void createBoard(CommandSource source, @Argument("name") String name)
      throws CommandSyntaxException
  {
    var opt = service.getLeaderboard(name);
    if (opt.isPresent()) {
      throw Exceptions.alreadyExists("Leaderboard", name);
    }

    BoardImpl board = new BoardImpl(name);
    board.setSource(LeaderboardSources.DUMMY_HOLDER);

    service.addLeaderboard(board);

    source.sendSuccess(format("Created leaderboard named '&e{0}&r'", NamedTextColor.GRAY, name));
  }

  void updateAll(CommandSource source) {
    int updated = 0;

    for (BoardImpl board : service.getBoards().values()) {
      if (!board.isSpawned()) {
        continue;
      }

      updated++;
      board.update();
    }

    source.sendSuccess(
        format("Updated &e{0, number}&r Leaderboards.", NamedTextColor.GRAY, updated)
    );
  }

  void killBoard(CommandSource source, @Argument("board") BoardImpl board)
      throws CommandSyntaxException
  {
    if (!board.isSpawned()) {
      throw Exceptions.create("Leaderboard is already inactive");
    }

    board.kill();
    source.sendSuccess(
        format("Killed leaderboard {0}.", NamedTextColor.GRAY, board.displayName())
    );
  }

  void removeBoard(CommandSource source, @Argument("board") BoardImpl board)
      throws CommandSyntaxException
  {
    if (board.isSpawned()) {
      board.kill();
    }

    Component name = board.displayName();

    service.removeLeaderboard(board.getName());
    source.sendSuccess(format("Removed leaderboard {0}.", NamedTextColor.GRAY, name));
  }

  void spawnBoard(CommandSource source, @Argument("board") BoardImpl board)
      throws CommandSyntaxException
  {
    if (board.isSpawned()) {
      throw Exceptions.create("Leaderboard is already spawned");
    }

    board.spawn();

    source.sendSuccess(
        format("Spawned leaderboard {0}.",
            NamedTextColor.GRAY,
            board.displayName()
        )
    );
  }

  void setLocation(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "pos", optional = true) Location locationArg
  ) {
    Location location;

    if (locationArg == null) {
      location = source.getLocation();
    } else {
      location = locationArg;
    }

    board.setLocation(location);

    source.sendSuccess(
        format("Moved leaderboard {0} to &e{1, location, -c -w}&r",
            NamedTextColor.GRAY,
            board.displayName(),
            location
        )
    );
  }

  void copyBoard(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("source") BoardImpl copySource
  ) throws CommandSyntaxException {
    if (Objects.equals(board, copySource)) {
      throw Exceptions.create("Cannot copy from self");
    }

    board.copyFrom(copySource);
    board.update();

    source.sendSuccess(
        format("Copied all style data from {0} into {1}",
            NamedTextColor.GRAY,
            copySource.displayName(),
            board.displayName()
        )
    );
  }

  void setTextField(
      BoardImpl board,
      CommandSource source,
      String name,
      BiConsumer<BoardImpl, PlayerMessage> setter,
      String text
  ) {
    if (text == null) {
      setter.accept(board, null);

      source.sendSuccess(
          format("{0}: Removed {1}.", NamedTextColor.GRAY, board.displayName(), name)
      );
    } else {
      setter.accept(board, BoardImpl.makeTextFieldMessage(text));

      source.sendSuccess(
          format("{0}: Set {1} to '&f{2}&r'",
              NamedTextColor.GRAY,
              board.displayName(), name, text
          )
      );
    }

    board.update();
  }

  CompletableFuture<Suggestions> suggestTextField(
      PlayerMessage message,
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    if (message == null) {
      return MessageSuggestions.get(context, builder, true);
    }

    return MessageSuggestions.get(context, builder, true, (builder1, source) -> {
      builder1.suggest(message.getMessage());
    });
  }

  CompletableFuture<Suggestions> suggestHeader(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder,
      @Argument("board") BoardImpl board
  ) {
    return suggestTextField(board.getHeader(), context, builder);
  }

  CompletableFuture<Suggestions> suggestFormat(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder,
      @Argument("board") BoardImpl board
  ) {
    return suggestTextField(board.getFormat(), context, builder);
  }

  CompletableFuture<Suggestions> suggestFooter(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder,
      @Argument("board") BoardImpl board
  ) {
    return suggestTextField(board.getFooter(), context, builder);
  }

  void setFooter(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "text", optional = true) String text
  ) {
    setTextField(board, source, "footer", BoardImpl::setFooter, text);
  }

  void setHeader(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "text", optional = true) String text
  ) {
    setTextField(board, source, "header", BoardImpl::setHeader, text);
  }

  void setFormat(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "text", optional = true) String text
  ) {
    setTextField(board, source, "format", BoardImpl::setFormat, text);
  }

  void setYouFormat(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "text", optional = true) String text
  ) {
    setTextField(board, source, "you-format", BoardImpl::setYouFormat, text);
  }

  void setIncludeYou(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") boolean include
  ) {
    board.setIncludeYou(include);
    board.update();

    source.sendSuccess(
        format("{0}: Set 'include-you' to &e{1}&r.", NamedTextColor.GRAY,
            board.displayName(), include
        )
    );
  }

  void setOrder(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("order") Order order
  ) {
    board.setOrder(order);
    board.update();

    source.sendSuccess(
        format("Set {0} order to &e{1}&r.",
            NamedTextColor.GRAY,
            board.displayName(), order.name().toLowerCase())
    );
  }


  void setSource(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("source") Holder<LeaderboardSource> holder
  ) {
    board.setSource(holder);
    board.update();

    source.sendSuccess(
        format("Set {0} source to &e{1}&r.",
            NamedTextColor.GRAY,
            board.displayName(), holder.getKey()
        )
    );
  }

  void setMaxSize(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("size") int maxSize
  ) {
    board.setMaxEntries(maxSize);
    board.update();

    source.sendSuccess(
        format("Set {0} max-size to &e{1, number}&r.",
            NamedTextColor.GRAY,
            board.displayName(), maxSize
        )
    );
  }

  void setFillEmpty(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") boolean value
  ) {
    board.setFillMissingSlots(value);
    board.update();

    source.sendSuccess(
        format("Set {0} fill-empty-slots to &e{1}&r.",
            NamedTextColor.GRAY,
            board.displayName(), value
        )
    );
  }

  void setFilter(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "filter", optional = true) ScoreFilter filter
  ) {
    board.setFilter(filter);
    board.update();

    if (filter == null) {
      source.sendSuccess(
          format("Removed filter from {0}",
              NamedTextColor.GRAY,
              board.displayName()
          )
      );
    } else {
      source.sendSuccess(
          format("Set {0} filter to '&f{1}&r'",
              NamedTextColor.GRAY,
              board.displayName(), filter
          )
      );
    }
  }

  void setYaw(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") float value
  ) {
    board.getDisplayMeta().setYaw(value);
    board.update();
    source.sendSuccess(setEntityProperty(board, "yaw", formatNumber(value)));
  }

  void setPitch(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") float value
  ) {
    board.getDisplayMeta().setPitch(value);
    board.update();
    source.sendSuccess(setEntityProperty(board, "pitch", formatNumber(value)));
  }

  Vector3f toVec(ParsedPosition position) throws CommandSyntaxException {
    if (position.getXCoordinate().relative()
        || position.getYCoordinate().relative()
        || position.getZCoordinate().relative()
    ) {
      throw Exceptions.create("No relative coordinates ('~' or '^') not allowed here");
    }

    return new Vector3f(
        position.getXCoordinate().value(),
        position.getYCoordinate().value(),
        position.getZCoordinate().value()
    );
  }

  void setScale(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "value", optional = true) ParsedPosition position
  ) throws CommandSyntaxException {
    if (position == null) {
      board.getDisplayMeta().setScale(Vector3f.ONE);
      source.sendSuccess(setEntityProperty(board, "scale", null));
    } else {
      Vector3f vector = toVec(position);
      board.getDisplayMeta().setScale(vector);
      source.sendSuccess(setEntityProperty(board, "scale", format("{0, vector}", vector)));
    }

    board.update();
  }

  void setTranslation(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "value", optional = true) ParsedPosition position
  ) throws CommandSyntaxException {
    if (position == null) {
      board.getDisplayMeta().setTranslation(Vector3f.ONE);
      source.sendSuccess(setEntityProperty(board, "offset", null));
    } else {
      Vector3f vector = toVec(position);
      board.getDisplayMeta().setTranslation(vector);
      source.sendSuccess(setEntityProperty(board, "offset", format("{0, vector}", vector)));
    }

    board.update();
  }

  void setBillboard(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") Billboard billboard
  ) throws CommandSyntaxException {
    board.getDisplayMeta().setBillboard(billboard);
    board.update();

    source.sendSuccess(setEntityProperty(board, "billboard", format("{0, enum}", billboard)));
  }

  void setAlign(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") TextAlignment alignment
  ) throws CommandSyntaxException {
    board.getDisplayMeta().setAlign(alignment);
    board.update();

    source.sendSuccess(
        setEntityProperty(board, "text-alignment", format("{0, number}", alignment))
    );
  }

  void setBackColor(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "value", optional = true) Color color
  ) {
    board.getDisplayMeta().setBackgroundColor(color);
    board.update();
    source.sendSuccess(setEntityProperty(board, "background-color", color));
  }

  void setBrightness(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "skylight", optional = true) Integer skylight,
      @Argument(value = "blocklight", optional = true) Integer blocklight
  ) {
    if (skylight == null || blocklight == null) {
      board.getDisplayMeta().setBrightness(null);
      source.sendSuccess(setEntityProperty(board, "brightness", null));
    } else {
      Brightness brightness = new Brightness(blocklight, skylight);
      board.getDisplayMeta().setBrightness(brightness);
      source.sendSuccess(setEntityProperty(board, "brightness", brightness));
    }

    board.update();
  }

  void setShadowed(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") boolean value
  ) {
    board.getDisplayMeta().setShadowed(value);
    board.update();
    source.sendSuccess(setEntityProperty(board, "shadowed", value));
  }

  void setSeeThrough(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument("value") boolean value
  ) {
    board.getDisplayMeta().setSeeThrough(value);
    board.update();
    source.sendSuccess(setEntityProperty(board, "see-through", value));
  }

  void setLineWidth(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value = "value", optional = true) Integer lineWidth
  ) {
    if (lineWidth == null) {
      board.getDisplayMeta().setLineWidth(-1);
    } else {
      board.getDisplayMeta().setLineWidth(lineWidth);
    }

    board.update();
    source.sendSuccess(setEntityProperty(board, "line-width", lineWidth));
  }

  void setOpacity(
      CommandSource source,
      @Argument("board") BoardImpl board,
      @Argument(value="value", optional = true) Integer opacity
  ) {
    if (opacity != null) {
      board.getDisplayMeta().setOpacity(opacity.byteValue());
    } else {
      board.getDisplayMeta().setOpacity((byte) -1);
    }

    board.update();
    source.sendSuccess(setEntityProperty(board, "opacity", opacity));
  }

  Component setEntityProperty(BoardImpl board, String name, Object value) {
    if (value == null) {
      return format("{0}: Unset entity value '&f{1}&r'",
          NamedTextColor.GRAY,
          board.displayName(),
          name
      );
    }

    return format("{0}: Set entity value '&f{1}&r' to &e{2}",
        NamedTextColor.GRAY,
        board.displayName(),
        name,
        value
    );
  }
}
