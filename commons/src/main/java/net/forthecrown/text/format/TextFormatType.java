package net.forthecrown.text.format;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface TextFormatType {

  @NotNull
  Component resolveArgument(@NotNull Object value, @NotNull String style);
}