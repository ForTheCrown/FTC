package net.forthecrown.user.actions;

import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.events.dynamic.RegionVisitListener;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class FtcUserActionHandler implements UserActionHandler {
    @Override
    public void handleDirectMessage(DirectMessage message) {
        EavesDropper.reportDM(message);

        UUID senderID = null;
        UUID receiverID = null;

        //If no mute what so ever
        if(message.getMuteStatus().maySpeak) {
            if(message.getTarget().isPlayer()){
                CrownUser user = UserManager.getUser(message.getTarget().asOrNull(Player.class));
                user.setLastMessage(message.getSender());

                receiverID = user.getUniqueId();

                if(message.getSender().isPlayer()){
                    CrownUser senderUser = UserManager.getUser(message.getSender().asOrNull(Player.class));
                    senderUser.setLastMessage(message.getTarget());

                    senderID = senderUser.getUniqueId();

                    //Blocked lol
                    if(user.getInteractions().isBlockedPlayer(senderUser.getUniqueId())){
                        message.getSender().sendMessage(Component.translatable("user.message.cannot", NamedTextColor.GRAY, user.nickDisplayName().color(NamedTextColor.YELLOW)));
                        return;
                    }
                }

                //If AFK, warn sender target might not see
                if(user.isAfk()) message.getSender().sendMessage(Component.translatable("user.message.afk").color(NamedTextColor.YELLOW));
            }

            Component receiverMessage = Component.text()
                    .append(message.getReceiverHeader())
                    .append(Component.text(" "))
                    .append(message.getFormattedText())
                    .build();
            message.getTarget().sendMessage(receiverMessage, senderID);
        }

        //If soft or no mute, send to sender
        if(message.getMuteStatus().senderMaySee){
            Component senderMessage = Component.text()
                    .append(message.getSenderHeader())
                    .append(Component.text(" "))
                    .append(message.getFormattedText())
                    .build();

            message.getSender().sendMessage(senderMessage, receiverID);
        }

        //Remove from command source tracker
        message.getTarget().onCommandComplete(null, message.getMuteStatus().maySpeak, 0);
    }

    @Override
    public void handleMarriageMessage(MarriageMessage message) {
        Component formatted = MarriageMessage.format(message.getSender().nickDisplayName(), message.getFormatted());
        MuteStatus status = message.getMuteStatus();

        EavesDropper.reportMarriageDM(message);

        if(message.getSender().getInteractions().isBlockedPlayer(message.getTarget().getUniqueId())){

            //lmao, blocked by your spouse
            if(message.getSender().getInteractions().isOnlyBlocked(message.getTarget().getUniqueId())) {
                message.getSender().sendMessage(
                        Component.translatable("marriage.ignored", NamedTextColor.GOLD)
                );
            }

            //Ignored or seperated, don't send
            return;
        }

        //If the sender may send, aka if soft or no mute, send
        //If not muted at all, send to target
        if(status.senderMaySee) message.getSender().sendBlockableMessage(message.getTarget().getUniqueId(), formatted);
        if(status.maySpeak) message.getTarget().sendBlockableMessage(message.getSender().getUniqueId(), formatted);
    }

    @Override
    public void handleRegionInvite(InviteToRegion invite) {
        CrownUser sender = invite.getSender();
        CrownUser target = invite.getTarget();

        //Add the invites
        sender.getInteractions().addInvite(invite.getTarget().getUniqueId());
        target.getInteractions().addInvitedTo(invite.getSender().getUniqueId());

        //Tell sender they invited
        sender.sendMessage(
                Component.translatable("regions.invite.sender",
                        NamedTextColor.GOLD,
                        target.nickDisplayName()
                                .color(NamedTextColor.YELLOW))
        );

        //Tell target they were invited
        target.sendMessage(
                Component.translatable("regions.invite.target",
                        NamedTextColor.GOLD,
                        sender.nickDisplayName()
                                .color(NamedTextColor.YELLOW)
                )
        );
    }

    @Override
    public void handleTeleport(TeleportRequest request) {
        //Add the request
        request.getSender().getInteractions().addOutgoing(request);
        request.getTarget().getInteractions().addIncoming(request);

        //Start the countdown
        request.startCountdown();
    }

    @Override
    public void handleVisit(RegionVisit visit) {
        Player player = visit.getVisitor().getPlayer();
        if(!ListUtils.isNullOrEmpty(player.getPassengers())) {
            player.sendMessage(
                    Component.translatable("commands.teleport.error.passengers", NamedTextColor.GRAY)
            );
        }

        CrownUser user = visit.getVisitor();
        CosmeticData cosmetics = user.getCosmeticData();

        World world = visit.getRegion().getWorld();
        BlockVector2 poleCords = visit.getRegion().getPolePosition();
        Location loc = player.getLocation();

        //For entities which should be teleported along with the player
        List<Entity> toTeleport = new ObjectArrayList<>();
        FtcBoundingBox box = FtcBoundingBox.of(world, visit.getRegion().getPoleBoundingBox());
        box.expand(2.5);

        for (Entity e: box.getEntities()) {
            //If the entity is tameable and has been tamed
            //by the visitor
            if(e instanceof Tameable) {
                Tameable tameable = (Tameable) e;
                if(!tameable.isTamed()) continue;

                if(tameable.getOwnerUniqueId().equals(user.getUniqueId())) {
                    toTeleport.add(e);
                }
            }

            e.removePassenger(player);

            //If they're leashed by the visitor
            if(e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) e;

                try {
                    Entity leashHolder = living.getLeashHolder();
                    if(leashHolder.getUniqueId().equals(user.getUniqueId())) {
                        toTeleport.add(e);
                    }

                } catch (IllegalStateException e1) {
                }
            }
        }

        //Declare the teleportation location
        Location teleportLoc = new Location(
                world,
                poleCords.getX() + 0.5D,
                world.getHighestBlockYAt(poleCords.getX(), poleCords.getZ()) + 1,
                poleCords.getZ() + 0.5D,
                loc.getYaw(),
                loc.getPitch()
        );

        //Teleport all entities there
        toTeleport.forEach(e -> e.teleport(teleportLoc));

        if(ComVars.shouldHulkSmashPoles() && user.hulkSmashesPoles()) {
            //Move TP loc to sky since hulk smash
            teleportLoc.setY(256);

            //Rocket vector
            Vector vector = new Vector(0, 20, 0);

            //Rocket them into the sky
            user.setVelocity(vector);

            //If they have cosmetic effect, execute it
            if(cosmetics.hasActiveTravel()) {
                cosmetics.getActiveTravel().onHulkStart(loc);
            }

            //Tick task for them going up
            new GoingUp(teleportLoc, user);
        } else {
            //Execute travel effect, if they have one
            if(cosmetics.hasActiveTravel()) {
                Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
                    cosmetics.getActiveTravel().onPoleTeleport(user, loc, teleportLoc.clone());
                }, 2);
            }

            //Just TP them to pole... boring
            player.teleport(teleportLoc);
        }
    }

    private static class GoingUp implements Runnable {
        private final Location tp;
        private final CrownUser user;
        private final CosmeticData cosmetics;

        private final BukkitTask task;

        private GoingUp(Location tp, CrownUser user) {
            this.tp = tp;
            this.user = user;
            cosmetics = user.getCosmeticData();

            task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this,
                    RegionVisitListener.TICKS_PER_TICK, RegionVisitListener.TICKS_PER_TICK
            );
        }

        byte tick = (byte) (0.75 * (20 / RegionVisitListener.TICKS_PER_TICK));

        @Override
        public void run() {
            //If they have travel effect, run it
            if(cosmetics.hasActiveTravel()) {
                cosmetics.getActiveTravel().onHulkTickUp(user.getLocation());
            }

            tick--;

            //If we're below the tick limit, stop and move on to fall listener
            if(tick < 0) {
                task.cancel();

                user.getPlayer().teleport(tp);

                RegionVisitListener listener = new RegionVisitListener(user);
                listener.beginListening();
            }
        }
    }
}
