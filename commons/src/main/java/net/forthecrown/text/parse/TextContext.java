package net.forthecrown.text.parse;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.kyori.adventure.audience.Audience;
import org.bukkit.permissions.Permissible;

public class TextContext {

  private static final TextContext TOTAL_RENDER
      = new TextContext(null, EnumSet.allOf(ChatParseFlag.class));

  private final Audience audience;

  private final Set<ChatParseFlag> flags;

  public TextContext(Audience audience, Set<ChatParseFlag> flags) {
    this.audience = audience;
    this.flags = flags;
  }

  public static TextContext totalRender() {
    return TOTAL_RENDER;
  }

  public static TextContext totalRender(Audience audience) {
    return new TextContext(audience, TOTAL_RENDER.flags);
  }

  public static TextContext create(Permissible textSource, Audience viewer) {
    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(textSource);
    return of(flags, viewer);
  }

  public static TextContext of(Set<ChatParseFlag> flags, Audience viewer) {
    return new TextContext(viewer, flags);
  }

  public Set<ChatParseFlag> flags() {
    return Collections.unmodifiableSet(flags);
  }

  public boolean has(ChatParseFlag flag) {
    return flags.contains(flag);
  }

  public Audience viewer() {
    return audience;
  }
}