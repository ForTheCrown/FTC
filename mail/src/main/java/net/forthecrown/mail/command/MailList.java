package net.forthecrown.mail.command;

import java.util.List;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.mail.Mail;
import net.forthecrown.text.TextWriters;
import net.forthecrown.text.page.Footer;
import net.forthecrown.text.page.Header;
import net.forthecrown.text.page.PageEntry;
import net.forthecrown.text.page.PageFormat;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

class MailList {

  public static final ContextSet SET = ContextSet.create();

  public static final ContextOption<User> MAIL_USER = SET.newOption();
  public static final ContextOption<Page> PAGE = SET.newOption();
  public static final ContextOption<Boolean> SELF = SET.newOption();

  static final PageFormat<Mail> PAGE_FORMAT;

  static {
    PageFormat<Mail> format = PageFormat.create();

    format.setHeader(
        Header.<Mail>create()
            .title((it, writer, context) -> {

              writer.write(
                  context.getOrThrow(SELF)
                      ? MailMessages.MAIL_HEADER_SELF
                      : MailMessages.mailHeader(context.getOrThrow(MAIL_USER))
              );
            })
    );

    format.setEntry(PageEntry.of((writer, entry, viewerIndex, context, it) -> {
      Page p = context.getOrThrow(PAGE);
      var display = entry.displayText(writer.viewer(), p);
      writer.write(display);
    }));

    format.setFooter(
        Footer.create().setPageButton((viewerPage, pageSize, context) -> {
          String cmdFormat;
          boolean self = context.getOrThrow(SELF);
          User user = context.getOrThrow(MAIL_USER);

          if (self) {
            cmdFormat = "/mail %s %s";
          } else {
            cmdFormat = "/mail read_other " + user.getName() + " %s %s";
          }

          return ClickEvent.runCommand(
              cmdFormat.formatted(viewerPage, pageSize)
          );
        })
    );

    PAGE_FORMAT = format;
  }

  static Component formatMail(CommandSource viewer, Page page, List<Mail> mailList) {
    var target = page.player();

    boolean self = viewer.textName().equals(target.getName());
    PagedIterator<Mail> it = PagedIterator.reversed(mailList, page.page() - 1, page.pageSize());

    Context ctx = SET.createContext();
    ctx.set(MAIL_USER, target);
    ctx.set(SELF, self);
    ctx.set(PAGE, page);

    var writer = TextWriters.newWriter();
    writer.viewer(viewer);

    PAGE_FORMAT.write(it, writer, ctx);

    return writer.asComponent();
  }

}
