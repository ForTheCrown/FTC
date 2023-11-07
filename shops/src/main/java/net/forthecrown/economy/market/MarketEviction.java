package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.economy.ShopsPlugin;
import net.forthecrown.text.Text;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class MarketEviction implements Runnable {
  /* ----------------------------- CONSTANTS ------------------------------ */

  public static final String
      KEY_EVICT_DATE = "date",
      KEY_REASON = "reason",
      KEY_SOURCE = "source";

  public static final String SOURCE_AUTOMATIC = "AUTOMATED_EVICTION";
  public static final String SOURCE_UNKNOWN = "UNKNOWN";

  /* ----------------------------- INSTANCE FIELDS ------------------------------ */

  @Getter
  private final MarketShop market;
  @Getter
  private final long evictionTime;
  @Getter
  private final Component reason;
  @Getter
  private final String source;

  private BukkitTask task;

  /* ----------------------------- METHODS ------------------------------ */

  void start() {
    cancel();

    long execTime = Time.millisToTicks(Time.timeUntil(evictionTime));

    if (execTime <= 0) {
      run();
      return;
    }

    task = Tasks.runLater(this, execTime);
  }

  void cancel() {
    task = Tasks.cancel(task);
  }

  public void run() {
    var userService = Users.getService();
    if (!userService.userLoadingAllowed()) {
      ShopsPlugin.getPlugin().getMarkets().getAwaitingExecution().add(this);
      return;
    }

    var owner = market.ownerUser();
    var message = Text.format("You were evicted from your shop in Hazelguard, reason: '{0}'",
        NamedTextColor.GRAY,

        Component.text()
            .append(reason)
            .color(NamedTextColor.WHITE)
            .build()
    );

    owner.sendMessage(message);
    //owner.getMail().add(message);

    Loggers.getLogger().info(
        "Evicted owner of shop '{}': '{}', reason: `{}`",
        market.getName(),
        owner.getNickOrName(),
        Text.plain(reason)
    );

    market.unclaim(true);
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    json.addTimeStamp(KEY_EVICT_DATE, evictionTime);
    json.addComponent(KEY_REASON, reason);
    json.add(KEY_SOURCE, source);

    return json.getSource();
  }

  public static MarketEviction deserialize(JsonElement element, MarketShop shop) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return new MarketEviction(
        shop,
        json.getTimeStamp(KEY_EVICT_DATE),
        json.getComponent(KEY_REASON),
        json.getString(KEY_SOURCE, SOURCE_UNKNOWN)
    );
  }
}