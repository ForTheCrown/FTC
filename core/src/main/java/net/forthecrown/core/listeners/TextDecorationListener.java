package net.forthecrown.core.listeners;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.text.parse.ChatParseFlag;
import net.forthecrown.text.parse.ChatParser;
import net.forthecrown.text.parse.TextContext;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

public class TextDecorationListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onAsyncChatDecorate(AsyncChatDecorateEvent event) {
    ChatParser parser = ChatParser.parser();
    TextContext context = TextContext.create(event.player(), null);

    String str = Text.LEGACY.serialize(event.result());
    warnCase(event.player(), str);

    event.result(parser.parseBasic(str, context));
  }

  @EventHandler(ignoreCancelled = true)
  public void onSignChange(SignChangeEvent event) {
    User user = Users.get(event.getPlayer());

    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(user);
    flags.add(ChatParseFlag.IGNORE_CASE);

    ChatParser parser = ChatParser.parser();
    TextContext context = TextContext.of(flags, null);

    for (int i = 0; i < event.lines().size(); i++) {
      event.line(i, parser.parse(event.getLine(i), context));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerEditBook(PlayerEditBookEvent event) {
    BookMeta meta = event.getNewBookMeta();
    List<Component> pages = new ArrayList<>(meta.pages());

    if (pages.isEmpty()) {
      return;
    }

    User user = Users.get(event.getPlayer());

    Set<ChatParseFlag> flags = ChatParseFlag.allApplicable(user);
    flags.add(ChatParseFlag.IGNORE_CASE);

    ChatParser parser = ChatParser.parser();
    TextContext context = TextContext.of(flags, null);

    var it = pages.listIterator();
    while (it.hasNext()) {
      var n = it.next();
      it.set(parser.parse(Text.LEGACY.serialize(n), context));
    }
    
    meta.pages(pages); // BookMeta overrides the pages() method, return value can be ignored
    event.setNewBookMeta(meta);
  }

  /**
   * Tests if the given input <code>s</code> is more than half uppercase, if it is, then it warns
   * the source and tells them not to send all upper case messages.
   * <p>
   * If the <code>s</code> input is less than 8 characters long, if the sender is null or has the
   * {@link ChatParseFlag#IGNORE_CASE} permission, then this method won't check the string
   *
   * @param source The sender of the message
   * @param s      The input to test
   */
  public static void warnCase(@Nullable CommandSender source, String s) {
    if (s.length() <= 8
        || source == null
        || source.hasPermission(ChatParseFlag.IGNORE_CASE.getPermission())
    ) {
      return;
    }

    int upperCaseCount = 0;
    int half = s.length() / 2;

    for (int i = 0; i < s.length(); i++) {
      if (Character.isUpperCase(s.charAt(i))) {
        upperCaseCount++;
      }

      if (upperCaseCount > half) {
        //1 and a half minute cooldown
        if (!Cooldown.containsOrAdd(source, "uppercase_warning", (60 + 30) * 20)) {
          source.sendMessage(Messages.ALL_CAPS);
        }

        return;
      }
    }
  }
}
