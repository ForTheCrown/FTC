package net.forthecrown.text.format;

import net.kyori.adventure.text.Component;

public interface TextFormatType {

  Component resolveArgument(Object value, String style);
}