package net.forthecrown.user.actions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Permissions;
import net.forthecrown.core.admin.EavesDropper;
import net.forthecrown.core.admin.MuteStatus;
import net.forthecrown.core.chat.Announcer;
import net.forthecrown.core.chat.BannedWords;
import net.forthecrown.events.dynamic.RegionVisitListener;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionConstants;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.RegionPoleGenerator;
import net.forthecrown.user.CosmeticData;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMail;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.ListUtils;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class FtcUserActionHandler implements UserActionHandler {
    @Override
    public void handleDirectMessage(DirectMessage message) {
        //Validate they didn't just use a slur or something lol
        if(BannedWords.checkAndWarn(message.getSender().asBukkit(), message.getFormattedText())) {
            message.setMuteStatus(MuteStatus.HARD);
        }

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

        //Validate they didn't just use a slur or something lol
        if(BannedWords.checkAndWarn(message.getSender(), formatted)) {
            message.setStatus(MuteStatus.HARD);
        }

        EavesDropper.reportMarriageDM(message);

        MuteStatus status = message.getMuteStatus();

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
    public void handleMailQuery(MailQuery query) throws CommandSyntaxException {
        if(!query.getSource().hasPermission(Permissions.MAIL_OTHERS) && !query.getMail().canSee(query.getSource().asBukkit())) {
            throw FtcExceptionProvider.translatable("mail.private", query.getUser().nickDisplayName());
        }

        boolean self = query.isSelfQuery();
        boolean onlyUnread = query.onlyUnread();

        List<UserMail.MailMessage> messages = onlyUnread ? query.getMail().getUnread() : query.getMail().getMail();

        if(messages.isEmpty()) {
            throw FtcExceptionProvider.translatable("mail.none." + (self ? "self" : "other") + (onlyUnread ? ".unread" : ""));
        }

        TextComponent.Builder builder = Component.text()
                .color(NamedTextColor.YELLOW)
                .append(
                        Component.translatable(
                                "mail.header." + (self ? "self" : "other"),
                                query.getUser().nickDisplayName()
                                        .color(NamedTextColor.GOLD)
                        )
                );

        int index = 0;
        for (UserMail.MailMessage m: messages) {
            index++;

            Component senderMetadata = m.sender == null ? Component.empty() :
                    Component.newline()
                            .append(Component.translatable("mail.metadata.sender", UserManager.getUser(m.sender).nickDisplayName()));

            builder.append(
                    Component.text()
                            .color(NamedTextColor.WHITE)
                            .append(
                                    Component.text(index + ")")
                                            .color(NamedTextColor.GOLD)
                                            .hoverEvent(
                                                    Component.translatable(
                                                            "mail.metadata.date",
                                                            Component.text(new Date(m.sent).toString())
                                                    )
                                                            .append(senderMetadata)
                                            )
                            )

                            .append(
                                    Component.text()
                                            .color(NamedTextColor.GRAY)
                                            .content(" [")
                                            .append(
                                                    Component.translatable(m.read ? "mail.read" : "m.unread")
                                                            .clickEvent(ClickEvent.runCommand("/mail read " + query.getMail().getMail().indexOf(m)))
                                                            .hoverEvent(Component.translatable(m.read ? "mail.read.hover" : "mail.unread.hover"))
                                            )
                                            .append(Component.text("] "))
                            )

                            .append(m.message)
            );
        }

        query.getSource().sendMessage(
                builder.build()
        );
    }

    @Override
    public void handleMailAdd(MailAddAction action) throws CommandSyntaxException {
        boolean online = action.getUser().isOnline();
        CrownUser user = action.getUser();
        UserMail mail = action.getMail();
        Component text = action.getText();
        Component senderDisplay = null;

        if(action.getSender() != null && action.shouldValidateSender()) {
            UUID sender = action.getSender();
            CrownUser senderUser = UserManager.getUser(sender);
            senderDisplay = senderUser.nickDisplayName().color(NamedTextColor.YELLOW);

            if(user.getInteractions().isBlockedPlayer(sender)) {
                throw FtcExceptionProvider.blockedPlayer(user);
            }

            MuteStatus status = senderUser.getInteractions().muteStatus();

            if(BannedWords.checkAndWarn(senderUser, text)) {
                status = MuteStatus.HARD;
            }

            if(status.senderMaySee) {
                senderUser.sendMessage(
                        Component.translatable("mail.sent",
                                NamedTextColor.YELLOW,
                                senderDisplay.color(NamedTextColor.GOLD),
                                action.getText().color(NamedTextColor.WHITE)
                        )
                );
            }

            senderUser.unloadIfOffline();
            if(!status.maySpeak) return;
        }

        mail.add(action.getText(), action.getSender());

        if(online) {
            if(action.hasSender()) {
                user.sendMessage(
                        Component.translatable("mail.received.humanSender", NamedTextColor.GRAY, senderDisplay)
                );
            } else {
                user.sendMessage(
                        Component.translatable("mail.received", NamedTextColor.GRAY)
                );
            }
        }
    }

    @Override
    public void handleVisit(RegionVisit visit) {
        Player player = visit.getVisitor().getPlayer();
        if(!ListUtils.isNullOrEmpty(player.getPassengers())) {
            player.sendMessage(
                    Component.translatable("commands.teleport.error.passengers", NamedTextColor.GRAY)
            );

            return;
        }

        CrownUser user = visit.getVisitor();
        CosmeticData cosmetics = user.getCosmeticData();

        World world = visit.getRegion().getWorld();
        BlockVector2 poleCords = visit.getRegion().getPolePosition();
        Location loc = player.getLocation();

        //Declare the teleportation location
        Location teleportLoc = new Location(
                world,
                poleCords.getX() + 0.5D,
                world.getHighestBlockYAt(poleCords.getX(), poleCords.getZ(), HeightMap.WORLD_SURFACE) + 1,
                poleCords.getZ() + 0.5D,
                loc.getYaw(),
                loc.getPitch()
        );

        //Generate pole in case it doesn't exist
        RegionManager manager = Crown.getRegionManager();
        RegionPoleGenerator generator = manager.getGenerator();
        generator.generate(visit.getRegion());

        PopulationRegion localRegion = manager.get(user.getRegionCords());
        BlockVector2 localPole = localRegion.getPolePosition();

        List<Entity> toTeleport = new ObjectArrayList<>();
        AtomicBoolean hasLeashed = new AtomicBoolean(false);

        //If near the pole, teleport tamed and/or owned entities at the pole
        if(user.get2DLocation().distance(localPole) <= RegionConstants.DISTANCE_TO_POLE) {

            //For entities which should be teleported along with the player
            FtcBoundingBox box = FtcBoundingBox.of(world, localRegion.getPoleBoundingBox());
            box.expand(2);

            toTeleport.addAll(
                    world.getNearbyEntities(box.toBukkit(), e -> {
                        Announcer.debug("entity: " + e);

                        //Skip players
                        if(e.getType() == EntityType.PLAYER) return false;

                        //If the entity is tameable and has been tamed
                        //by the visitor
                        if(e instanceof Tameable) {
                            Tameable tameable = (Tameable) e;
                            if(!tameable.isTamed()) return false;

                            if(tameable.getOwnerUniqueId().equals(user.getUniqueId())) {
                                return true;
                            }
                        }

                        //If entity is being ridden by player, and is not a player, tp it
                        if(e.getPassengers().contains(player)) {
                            //Remove player as passenger so it could be teleported
                            e.removePassenger(player);

                            return true;
                        }

                        //If they're leashed by the visitor
                        if(e instanceof LivingEntity) {
                            LivingEntity living = (LivingEntity) e;

                            try {
                                Entity leashHolder = living.getLeashHolder();
                                if(leashHolder.getUniqueId().equals(user.getUniqueId())) {
                                    hasLeashed.set(true);
                                    return true;
                                }

                            } catch (IllegalStateException e1) {
                            }
                        }

                        return false;
                    })
            );
        }

        //If the comvar to allow hulk smashing is on, the user allows it and the sky above the destination
        //is clear then do a hulk smash, else just teleport
        if(!hasLeashed.get() && ComVars.shouldHulkSmashPoles() && user.hulkSmashesPoles() && FtcUtils.hasOnlyAirAbove(WorldVec3i.of(teleportLoc))) {
            Location entityLoc = teleportLoc.clone();

            //Move TP loc to sky since hulk smash
            teleportLoc.setY(256);
            teleportLoc.setPitch(90f);

            //Rocket vector
            Vector vector = new Vector(0, 20, 0);

            //Rocket them into the sky
            user.setVelocity(vector);

            //If they have cosmetic effect, execute it
            if(cosmetics.hasActiveTravel()) {
                cosmetics.getActiveTravel().onHulkStart(user, loc);
            }

            //Tick task for them going up
            new GoingUp(teleportLoc, user, toTeleport, entityLoc);
        } else {
            //Execute travel effect, if they have one
            if(cosmetics.hasActiveTravel()) {
                Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
                    cosmetics.getActiveTravel().onPoleTeleport(user, loc, teleportLoc.clone());
                }, 2);
            }

            //Just TP them to pole... boring
            player.teleport(teleportLoc);

            //Teleport all entities there
            tpDelayed(toTeleport, teleportLoc);
        }
    }

    static void tpDelayed(List<Entity> entities, Location location) {
        Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
            entities.forEach(e -> e.teleport(location));
        }, 10);
    }

    private static class GoingUp implements Runnable {
        private final Location tp;
        private final CrownUser user;
        private final CosmeticData cosmetics;

        private final List<Entity> toTeleport;
        private final Location entityTP;

        private final BukkitTask task;

        private GoingUp(Location tp, CrownUser user, List<Entity> toTeleport, Location entityTP) {
            this.tp = tp;
            this.user = user;
            cosmetics = user.getCosmeticData();

            this.toTeleport = toTeleport;
            this.entityTP = entityTP;

            task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this,
                    RegionVisitListener.TICKS_PER_TICK, RegionVisitListener.TICKS_PER_TICK
            );
        }

        byte tick = (byte) (0.75 * (20 / RegionVisitListener.TICKS_PER_TICK));

        @Override
        public void run() {
            try {
                //If they have travel effect, run it
                if(cosmetics.hasActiveTravel()) {
                    cosmetics.getActiveTravel().onHulkTickUp(user, user.getLocation());
                }

                tick--;

                //If we're below the tick limit, stop and move on to fall listener
                if(tick < 0) {
                    task.cancel();

                    user.getPlayer().teleport(tp);
                    tpDelayed(toTeleport, entityTP);

                    RegionVisitListener listener = new RegionVisitListener(user);
                    listener.beginListening();
                }
            } catch (Exception e) {
                task.cancel();
            }
        }
    }
}
