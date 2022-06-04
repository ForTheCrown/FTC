package net.forthecrown.user.packet;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import org.bukkit.entity.Player;

public class PacketCall {
    @Getter @Setter
    private boolean cancelled = false;

    @Getter
    private final Player player;
    @Getter
    private final CrownUser user;

    public PacketCall(Player player) {
        this.player = player;
        this.user = UserManager.getUser(player);
    }
}