package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.utils.Struct;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public interface UserMail {
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

    ObjectList<MailMessage> getUnread();
    ObjectList<MailMessage> getMail();

    MailMessage get(int index) throws IndexOutOfBoundsException;
    int indexOf(MailMessage message);

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
