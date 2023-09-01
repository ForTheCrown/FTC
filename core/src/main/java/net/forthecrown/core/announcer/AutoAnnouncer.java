package net.forthecrown.core.announcer;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.forthecrown.core.PrefsBook;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.text.Messages;
import net.forthecrown.text.ViewerAwareMessage;
import net.forthecrown.text.channel.MessageRenderer;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.forthecrown.text.placeholder.Placeholders;
import net.forthecrown.user.User;
import net.forthecrown.utils.Audiences;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.SerializationHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitTask;

public class AutoAnnouncer implements Runnable {

  private final Path path;
  private final List<ViewerAwareMessage> messages = new ArrayList<>();

  private Duration interval = Duration.ofMinutes(5);
  private Component format = textOfChildren(Messages.FTC_PREFIX, text(" ${message}"));
  private AnnouncementIterator iterator;
  private Order order = Order.INCREMENTING;

  private BukkitTask task;

  public AutoAnnouncer() {
    this.path = PathUtil.pluginPath("auto_announcer.toml");
  }

  public void load() {
    messages.clear();
    interval = Duration.ofMinutes(5);

    PluginJar.saveResources("auto_announcer.toml", path);
    SerializationHelper.readAsJson(path, this::loadFrom);
  }

  private void loadFrom(JsonWrapper json) {
    interval = json.get("interval", JsonUtils::readDuration);
    messages.addAll(json.getList("messages", JsonUtils::readMessage));
    format = json.getComponent("format");
    order = json.getEnum("order", Order.class, Order.INCREMENTING);
  }

  public void start() {
    stop();
    iterator = order.createIterator(messages);
    task = Tasks.runTimer(this, interval, interval);
  }

  public void stop() {
    Tasks.cancel(task);
    task = null;
    iterator = null;
  }

  @Override
  public void run() {
    if (messages.isEmpty()) {
      return;
    }

    if (iterator == null) {
      iterator = order.createIterator(messages);
    } else if (!iterator.hasNext()) {
      iterator.reset();
    }

    ViewerAwareMessage base = iterator.next();
    PlaceholderRenderer placeholders = Placeholders.newRenderer().useDefaults();

    ChannelledMessage.create(base)
        .setChannelName("auto_broadcast")
        .setBroadcast()

        .setRenderer((viewer, baseMessage) -> {
          var rendered = placeholders.render(baseMessage, viewer);

          return Placeholders.newRenderer()
              .useDefaults()
              .add("message", rendered)
              .render(format);
        })

        .filterTargets(audience -> {
          User user = Audiences.getUser(audience);

          // Null most likely means the viewer is the console.
          // Console doesn't need to see announcements
          if (user == null) {
            return false;
          }

          return !user.get(PrefsBook.IGNORE_AUTO_BROADCASTS);
        })
        .send();
  }

  public MessageRenderer renderer(PlaceholderRenderer placeholders) {
    return (viewer, baseMessage) -> {
      var rendered = placeholders.render(baseMessage, viewer);

      return Placeholders.newRenderer()
          .useDefaults()
          .add("message", rendered)
          .render(format);
    };
  }
}
