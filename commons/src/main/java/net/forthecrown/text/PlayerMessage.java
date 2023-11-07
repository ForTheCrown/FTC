package net.forthecrown.text;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.parse.ChatParser;
import net.forthecrown.text.parse.TextContext;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.FtcCodecs;
import net.forthecrown.utils.io.Results;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode
public class PlayerMessage implements ViewerAwareMessage {

  private static final String KEY_MESSAGE = "message";
  private static final String KEY_FLAGS = "flags";

  private static final Codec<ChatParseFlag> SINGLE_FLAG_CODEC
      = FtcCodecs.enumCodec(ChatParseFlag.class);

  private static final Codec<Set<ChatParseFlag>> FLAG_SET_CODEC
      = SINGLE_FLAG_CODEC.listOf().xmap(ObjectOpenHashSet::new, ArrayList::new);

  private static final Codec<Set<ChatParseFlag>> FLAG_STRING_CODEC
      = Codec.STRING.flatXmap(
          s -> {
            if (s.equalsIgnoreCase("all")) {
              return Results.success(ChatParseFlag.all());
            }

            if (s.equalsIgnoreCase("none")) {
              return Results.success(Set.of());
            }

            try {
              return Results.success(Set.of(ChatParseFlag.valueOf(s.toUpperCase())));
            } catch (IllegalArgumentException exc) {
              return Results.error("Not 'all', 'none' or a specific flag");
            }
          },
          flags -> {
            if (flags.size() == 1) {
              return Results.success(flags.iterator().next().name().toLowerCase());
            }
            if (flags.isEmpty()) {
              return Results.success("none");
            }
            if (flags.size() >= ChatParseFlag.values().length) {
              return Results.success("all");
            }
            return Results.error("Cannot encode multiple values in string");
          }
        );

  private static final Codec<Set<ChatParseFlag>> FLAG_CODEC
      = new EitherCodec<>(FLAG_SET_CODEC, FLAG_STRING_CODEC)
      .xmap(
          setSetEither -> {
            return setSetEither.map(Function.identity(), Function.identity());
          },

          chatParseFlags -> {
            if (chatParseFlags.isEmpty()) {
              return Either.left(chatParseFlags);
            }
            if (chatParseFlags.size() >= ChatParseFlag.values().length) {
              return Either.right(chatParseFlags);
            }
            if (chatParseFlags.size() > 2) {
              return Either.left(chatParseFlags);
            }
            return Either.right(chatParseFlags);
          }
      );

  private static final Codec<PlayerMessage> RECORD_CODEC = RecordCodecBuilder.create(instance -> {
    return instance
        .group(
            Codec.STRING.fieldOf("message").forGetter(o -> o.message),

            FLAG_CODEC.optionalFieldOf("flags").forGetter(o -> {
              return o.flags.isEmpty() ? Optional.empty() : Optional.of(o.flags);
            })
        )
        .apply(instance, (message, flagsOpt) -> {
          return new PlayerMessage(message, flagsOpt.orElse(null));
        });
  });

  public static final Codec<PlayerMessage> CODEC = new EitherCodec<>(RECORD_CODEC, Codec.STRING)
      .xmap(
          either -> either.map(message1 -> message1, PlayerMessage::plain),
          message1 -> {
            if (message1.flags.isEmpty()) {
              return Either.right(message1.message);
            }
            return Either.left(message1);
          }
      );

  private final String message;
  private final Set<ChatParseFlag> flags;

  public PlayerMessage(String message, @Nullable Set<ChatParseFlag> flags) {
    Objects.requireNonNull(message, "Null message");

    this.message = message;

    if (flags == null || flags.isEmpty()) {
      this.flags = Set.of();
    } else {
      this.flags = Collections.unmodifiableSet(flags);
    }
  }

  public PlayerMessage edit(UnaryOperator<String> editor) {
    String newMessage = editor.apply(message);
    return new PlayerMessage(newMessage, flags);
  }

  public static PlayerMessage plain(String string) {
    return new PlayerMessage(string, null);
  }

  public static PlayerMessage of(String string, User user) {
    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(user);
    return new PlayerMessage(string, flags);
  }

  public static PlayerMessage allFlags(String message) {
    return new PlayerMessage(message, ChatParseFlag.all());
  }

  @Override
  public Component create(@Nullable Audience viewer) {
    ChatParser parser = ChatParser.parser();
    TextContext context = TextContext.of(flags, viewer);
    return parser.parse(message, context);
  }

  public <S> DataResult<S> save(DynamicOps<S> ops) {
    return CODEC.encodeStart(ops, this);
  }

  public static <S> DataResult<PlayerMessage> load(Dynamic<S> dynamic) {
    return CODEC.parse(dynamic);
  }

  @Override
  public String toString() {
    return "PlayerMessage{" +
        "message='" + message + '\'' +
        ", flags=" + flags +
        '}';
  }
}
