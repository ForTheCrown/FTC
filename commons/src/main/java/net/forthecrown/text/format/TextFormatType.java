package net.forthecrown.text.format;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TextFormatType {

  @NotNull
  Component resolve(@NotNull Object value, @NotNull String style, @Nullable Audience viewer);
}