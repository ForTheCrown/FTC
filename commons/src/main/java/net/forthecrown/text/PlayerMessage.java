package net.forthecrown.text;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.ListCodec;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.parse.ChatParser;
import net.forthecrown.text.parse.TextContext;
import net.forthecrown.user.User;
import net.forthecrown.utils.io.FtcCodecs;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode
public class PlayerMessage implements ViewerAwareMessage {

  private static final String KEY_MESSAGE = "message";
  private static final String KEY_FLAGS = "flags";

  private static final ListCodec<ChatParseFlag> FLAG_CODEC
      = new ListCodec<>(FtcCodecs.enumCodec(ChatParseFlag.class));

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

  public static ViewerAwareMessage allFlags(String message) {
    return new PlayerMessage(message, ChatParseFlag.all());
  }

  @Override
  public Component create(@Nullable Audience viewer) {
    ChatParser parser = ChatParser.parser();
    TextContext context = TextContext.of(flags, viewer);
    return parser.parse(message, context);
  }

  public <S> DataResult<S> save(DynamicOps<S> ops) {
    var map = ops.mapBuilder();
    map.add(KEY_MESSAGE, ops.createString(message));
    map.add(KEY_FLAGS, FLAG_CODEC.encodeStart(ops, Lists.newArrayList(flags)));
    return map.build(ops.empty());
  }

  public static <S> DataResult<PlayerMessage> load(Dynamic<S> dynamic) {
    var stringRes = dynamic.asString();

    if (stringRes.result().isPresent()) {
      return stringRes.map(PlayerMessage::plain);
    }

    var message = dynamic.get(KEY_MESSAGE).asString();
    var flags = dynamic.get(KEY_FLAGS).decode(FLAG_CODEC).map(Pair::getFirst);

    return message.apply2stable((s, o) -> new PlayerMessage(s, new HashSet<>(o)), flags);
  }

  @Override
  public String toString() {
    return "PlayerMessage{" +
        "message='" + message + '\'' +
        ", flags=" + flags +
        '}';
  }
}
