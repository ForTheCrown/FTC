package net.forthecrown.text.placeholder;

import java.util.List;

public interface PlaceholderService {

  void addDefaultSource(PlaceholderSource source);

  void removeDefaultSource(PlaceholderSource source);

  PlaceholderList getDefaults();

  List<PlaceholderSource> getDefaultSources();

  PlaceholderList newList();

  PlaceholderRenderer newRenderer();
}
