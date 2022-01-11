package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public interface UserMail extends UserAttachment {
    boolean canSee(UUID id);

    default boolean canSee(CommandSender sender) {
        if(sender instanceof Player) {
            return canSee(((Player) sender).getUniqueId());
        }

        if(sender instanceof CrownUser) {
            return canSee(((CrownUser) sender).getUniqueId());
        }

        return true;
    }

    default void add(Component component) {
        add(component, null);
    }

    void add(Component message, @Nullable UUID sender);

    void remove(int index);

    void readAll();
    void clear();

    int size();

    ObjectList<MailMessage> getUnread();
    ObjectList<MailMessage> getMail();

    default int unreadSize() {
        return getUnread().size();
    }

    MailMessage get(int index) throws IndexOutOfBoundsException;
    int indexOf(MailMessage message);

    default boolean isValidIndex(int index) {
        return index >= 0 && index <= size();
    }

    default void informOfUnread() {
        int unreadSize = unreadSize();
        if(unreadSize < 1) return;
        CrownUser user = getUser();

        user.sendMessage(
                Component.translatable("mail.new")
                        .color(NamedTextColor.YELLOW)

                        .hoverEvent(Component.text("Click to read mail"))
                        .clickEvent(ClickEvent.runCommand("/mail"))

                        .append(Component.space())
                        .append(Component.translatable("mail.new2", Component.text(unreadSize)).color(NamedTextColor.GRAY))
        );
    }

    class MailMessage implements Struct {
        public final Component message;
        public final UUID sender;
        public final long sent;

        public boolean read;

        public MailMessage(Component message, UUID sender, long sent) {
            this(message, sender, sent, false);
        }

        public MailMessage(Component message, UUID sender, long sent, boolean read) {
            this.message = message;
            this.sender = sender;
            this.sent = sent;
            this.read = read;
        }
    }
}
