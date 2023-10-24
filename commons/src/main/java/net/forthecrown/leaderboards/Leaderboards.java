package net.forthecrown.leaderboards;

import static net.forthecrown.BukkitServices.loadOrThrow;

import net.forthecrown.registry.Registry;

public final class Leaderboards {
  private Leaderboards() {}

  static LeaderboardService service;

  public static LeaderboardService getService() {
    return service == null
        ? (service = loadOrThrow(LeaderboardService.class))
        : service;
  }

  public static void setService(LeaderboardService service) {
    Leaderboards.service = service;
  }

  public static Registry<LeaderboardSource> getSources() {
    return getService().getSources();
  }

  public static void registerSource(String key, LeaderboardSource source) {
    getSources().register(key, source);
  }
}
