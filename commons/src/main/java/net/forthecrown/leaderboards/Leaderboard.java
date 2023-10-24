package net.forthecrown.leaderboards;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Leaderboard {

  int DEFAULT_MAX_SIZE = 10;

  boolean update();

  boolean kill();

  boolean spawn();

  boolean isSpawned();

  String getName();

  LeaderboardSource getSource();

  void setSource(LeaderboardSource source);

  @Nullable Location getLocation();

  void setLocation(@Nullable Location location);

  @Nullable
  Component getFooter();

  void setFooter(@Nullable Component footer);

  @Nullable
  Component getHeader();

  void setHeader(@Nullable Component header);

  @Nullable
  Component getFormat();

  void setFormat(@Nullable Component format);

  int getMaxEntries();

  void setMaxEntries(int maxEntries);

  boolean fillMissingSlots();

  void setFillMissingSlots(boolean fillMissingSlots);

  Order getOrder();

  void setOrder(@NotNull Order order);

  @Nullable
  ScoreFilter getFilter();

  void setFilter(@Nullable ScoreFilter filter);

  enum Order {
    ASCENDING,
    DESCENDING
  }
}
