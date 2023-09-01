package net.forthecrown.text.placeholder;

public interface PlaceholderSource {

  TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx);
}
