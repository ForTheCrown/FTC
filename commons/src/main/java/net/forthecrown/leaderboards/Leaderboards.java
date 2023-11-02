package net.forthecrown.leaderboards;

import static net.forthecrown.BukkitServices.load;
import static net.forthecrown.BukkitServices.loadOrThrow;

import java.util.function.Consumer;
import net.forthecrown.registry.Registry;

public final class Leaderboards {
  private Leaderboards() {}

  static LeaderboardService service;

  public static LeaderboardService getService() {
    return service == null
        ? (service = loadOrThrow(LeaderboardService.class))
        : service;
  }

  public static void updateWithSource(String sourceKey) {
    ifLoaded(service -> {
      var sources = service.getSources();
      var opt = sources.getHolder(sourceKey);

      if (opt.isEmpty()) {
        return;
      }

      service.updateWithSource(opt.get());
    });
  }

  public static void ifLoaded(Consumer<LeaderboardService> consumer) {
    if (service != null) {
      consumer.accept(service);
      return;
    }

    load(LeaderboardService.class).ifPresent(leaderboardService -> {
      service = leaderboardService;
      consumer.accept(service);
    });
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
