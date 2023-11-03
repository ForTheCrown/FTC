package net.forthecrown.leaderboards;

import static net.forthecrown.leaderboards.Leaderboard.DEFAULT_MAX_SIZE;

import com.google.common.base.Strings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.forthecrown.leaderboards.Leaderboard.Order;
import net.forthecrown.leaderboards.commands.LeaderboardCommands;
import net.forthecrown.registry.Holder;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.utils.io.FtcCodecs;
import org.bukkit.Location;

public final class LeaderboardCodecs {
  private LeaderboardCodecs() {}

  static final Codec<Holder<LeaderboardSource>> sourceCodec
      = Codec.STRING.comapFlatMap(LeaderboardSources::get, Holder::getKey);

  static final Codec<ScoreFilter> FILTER_CODEC = Codec.STRING
      .comapFlatMap(LeaderboardCommands::parseFilter, ScoreFilter::toString);

  static final Codec<BoardImpl> CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            sourceCodec.optionalFieldOf("source")
                .forGetter(board -> Optional.ofNullable(board.source)),

            FtcCodecs.LOCATION_CODEC
                .optionalFieldOf("location")
                .forGetter(board -> Optional.ofNullable(board.location)),

            optionalTextField("footer", BoardImpl::getFooter),
            optionalTextField("header", BoardImpl::getHeader),
            optionalTextField("format", BoardImpl::getFormat),
            optionalTextField("you_format", BoardImpl::getYouFormat),

            FtcCodecs.enumCodec(Order.class)
                .optionalFieldOf("order", Order.DESCENDING)
                .forGetter(BoardImpl::getOrder),

            FILTER_CODEC.optionalFieldOf("filter")
                .forGetter(board -> Optional.ofNullable(board.filter)),

            Codec.INT.optionalFieldOf("max_entries", DEFAULT_MAX_SIZE)
                .forGetter(BoardImpl::getMaxEntries),

            Codec.BOOL.optionalFieldOf("fill_empty", false)
                .forGetter(BoardImpl::fillMissingSlots),

            TextDisplayMeta.CODEC.fieldOf("display_meta")
                .forGetter(BoardImpl::getDisplayMeta),

            Codec.BOOL.optionalFieldOf("spawned", false)
                .forGetter(BoardImpl::isSpawned),

            Codec.BOOL.optionalFieldOf("include_you", true)
                .forGetter(BoardImpl::isIncludeYou)
        )
        .apply(instance, LeaderboardCodecs::loadBoard);
  });

  private static RecordCodecBuilder<BoardImpl, Optional<PlayerMessage>> optionalTextField(
      String fieldName,
      Function<BoardImpl, PlayerMessage> getter
  ) {
    MapCodec<Optional<PlayerMessage>> mapCodec = PlayerMessage.CODEC.optionalFieldOf(fieldName);
    return mapCodec.forGetter(board -> toOpt(getter.apply(board)));
  }

  private static Optional<PlayerMessage> toOpt(PlayerMessage component) {
    if (component == null || Strings.isNullOrEmpty(component.getMessage())) {
      return Optional.empty();
    }
    return Optional.of(component);
  }

  private static BoardImpl loadBoard(
      Optional<Holder<LeaderboardSource>> source,
      Optional<Location> location,
      Optional<PlayerMessage> footer,
      Optional<PlayerMessage> header,
      Optional<PlayerMessage> format,
      Optional<PlayerMessage> youFormat,
      Order order,
      Optional<ScoreFilter> scoreFilter,
      Integer maxEntries,
      Boolean fillMissingSlots,
      TextDisplayMeta displayMeta,
      Boolean spawned,
      Boolean includeYou
  ) {
    BoardImpl board = new BoardImpl(null);
    board.setSource(source.orElse(null));
    board.setLocation(location.orElse(null));
    board.setFooter(footer.orElse(null));
    board.setHeader(header.orElse(null));
    board.setFormat(format.orElse(null));
    board.setYouFormat(youFormat.orElse(null));
    board.setOrder(order);
    board.setFilter(scoreFilter.orElse(null));
    board.setMaxEntries(maxEntries);
    board.setFillMissingSlots(fillMissingSlots);
    board.setSpawned(spawned);
    board.setIncludeYou(includeYou);

    if (displayMeta != null) {
      board.setDisplayMeta(displayMeta);
    }

    return board;
  }
}
