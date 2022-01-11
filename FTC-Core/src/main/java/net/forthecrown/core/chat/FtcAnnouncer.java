package net.forthecrown.core.chat;

import com.google.common.base.Predicates;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Worlds;
import net.forthecrown.inventory.FtcItems;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.user.manager.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class FtcAnnouncer extends AbstractJsonSerializer implements Announcer {

    private final List<Component> announcements = new ArrayList<>();
    private final ComVar<Short> delay = ComVarRegistry.set("broadcastDelay", ComVarTypes.SHORT, (short) 12000);
    private BukkitRunnable broadcaster;

    public FtcAnnouncer(){
        super("announcer");

        reload();

        delay.setOnUpdate(val -> start());
        Crown.logger().info("Announcer loaded");
    }

    @Override
    protected void save(final JsonWrapper json) {
        json.add("delay", delay.getValue());

        JsonArray array = new JsonArray();
        for (Component c: announcements){
            array.add(ChatUtils.toJson(c));
        }

        json.add("announcements", array);
    }

    @Override
    protected void reload(final JsonWrapper json) {
        delay.setValue(json.get("delay").getAsShort());

        JsonArray array = json.getArray("announcements");
        announcements.clear();
        for (JsonElement j: array){
            announcements.add(ChatUtils.fromJson(j));
        }
    }

    public void doBroadcasts() {
        Crown.logger().info("Starting announcer");
        broadcaster = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                Component broadcast = Component.text()
                        .append(Crown.prefix())
                        .append(getAnnouncements().get(counter))
                        .build();

                for (CrownUser player : UserManager.getOnlineUsers()) {
                    // Don't broadcast info messages to players in the Senate.
                    if (player.ignoringBroadcasts()) continue;

                    player.sendMessage(broadcast);

                    if (player.getWorld().equals(Worlds.RESOURCE)){
                        player.sendMessage(Component.text("You're in the resource world! To get back to the normal survival world, do /warp portal.").color(NamedTextColor.GRAY));
                    }
                }

                if (getAnnouncements().size() == counter+1) counter = 0;
                else counter++;
            }
        };
        broadcaster.runTaskTimer(Crown.inst(), 500, getDelay());
    }

    @Override
    public short getDelay() {
        return delay.getValue((short) 12000);
    }

    @Override
    public void setDelay(short delay) {
        this.delay.update(delay);
    }

    @Override
    public List<Component> getAnnouncements() {
        return announcements;
    }

    @Override
    public void add(Component announcement) {
        announcements.add(announcement);
    }

    @Override
    public void remove(int acIndex) {
        announcements.remove(acIndex);
    }

    @Override
    public void stop() {
        broadcaster.cancel();
    }

    @Override
    public void start() {
        if(!broadcaster.isCancelled()) broadcaster.cancel();
        doBroadcasts();
    }

    @Override
    public void announceRaw(Component announcement, @Nullable Predicate<Player> predicate) {
        for (Player p: Bukkit.getOnlinePlayers()) {
            if(predicate != null && !predicate.test(p)) continue;
            p.sendMessage(announcement);
        }
    }

    @Override
    public void announceToAllRaw(Component announcement, @Nullable Predicate<CommandSender> predicate) {
        announceRaw(announcement, predicate == null ? Predicates.alwaysTrue() : predicate::test);
        if(predicate == null || predicate.test(Bukkit.getConsoleSender())) Bukkit.getConsoleSender().sendMessage(announcement);
    }

    public boolean wasStopped(){
        return broadcaster.isCancelled();
    }

    @Override
    public Component formatMessage(Component message){
        return Component.text()
                .append(Crown.prefix())
                .append(message)
                .build();
    }


    @Override
    protected void createDefaults(final JsonWrapper json) {
        json.add("delay", 12000);

        JsonArray array = new JsonArray();

        //Thanks Roro <3
        array.add(deser("[\"\",{\"text\":\"To visit a region, do \"},{\"text\":\"/visit\",\"color\":\"yellow\"},{\"text\":\" when you are near a region pole.\"}]"));
        array.add(deser("[{\"text\":\"The server's discord is available at \"},{\"text\":\"[discord].\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/discord\"}}]"));
        array.add(deser("[{\"text\":\"Remember, there are \"},{\"text\":\"player shops\",\"color\":\"yellow\"},{\"text\":\" in Hazelguard.\"}]"));
        array.add(deser("[{\"text\":\"If you like a challenge, you can try to complete \"},{\"text\":\"the Dungeons\",\"color\":\"yellow\"},{\"text\":\". Follow the signs in Hazelguard.\"}]"));
        array.add(deser("[{\"text\":\"Let others know you are afk with \"},{\"text\":\"/afk.\",\"color\":\"yellow\"}]"));
        array.add(deser("[{\"text\":\"More information about the \"},{\"text\":\"ranks\",\"color\":\"gold\"},{\"text\":\" can be found in \"},{\"text\":\"[shop].\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/shop\"}}]\n"));
        array.add(deser("[{\"text\":\"If you need to gather a lot of materials, please use the \"},{\"text\":\"Resource World.\",\"color\":\"green\"}]"));

        array.add(ser(
                Component.text("You can get the ")
                        .append(RankTitle.KNIGHT.prefix())
                        .append(Component.text("tag and a "))
                        .append(FtcItems.royalSword().getItemMeta().displayName().hoverEvent(FtcItems.royalSword()))
                        .append(Component.text(" for completing the Dungeons."))
        ));
        array.add(ser(Component.text("There is a skeleton farm in Hazelguard if you need xp or bones.")));
        array.add(ser(
                Component.text("You can find the closest region pole with ")
                        .append(Component.text("[findpole]")
                                .hoverEvent(HoverEvent.showText(Component.text("Shows you the closest region pole")))
                                .clickEvent(ClickEvent.runCommand("/findpole"))
                                .color(NamedTextColor.YELLOW)
                        )
        ));
        array.add(ser(
                Component.text("If you are having trouble understanding region poles, you can do ")
                        .append(Component.text("[polehelp]")
                                .color(NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Show some info about region poles")))
                                .clickEvent(ClickEvent.runCommand("/polehelp"))
                        )
        ));
        array.add(ser(
                Component.text("You can ")
                        .append(Component.text("[vote]")
                                .color(NamedTextColor.YELLOW)
                                .clickEvent(ClickEvent.runCommand("/vote"))
                                .hoverEvent(HoverEvent.showText(Component.text("Vote for the server! :D")))
                        )
                        .append(Component.text(" to receive "))
                        .append(Component.text("Bank Tickets ")
                                .color(NamedTextColor.GOLD)
                                .hoverEvent(FtcItems.voteTicket().asHoverEvent())
                        )
                        .append(Component.text("used in the "))
                        .append(Component.text("[bank]")
                                .color(NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Show's some info about the bank")))
                                .clickEvent(ClickEvent.runCommand("/bank"))
                        )
        ));
        array.add(ser(
                Component.text("The End ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text("is only open for the first week of every month. Afterward, it closes and resets").color(NamedTextColor.WHITE))
        ));
        array.add(ser(
                Component.text("The map of the world can be seen with ")
                        .append(Component.text("[map]")
                                .color(NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Show's the Dynmap link")))
                                .clickEvent(ClickEvent.runCommand("/map"))
                        )
        ));

        json.add("announcements", array);
    }

    private JsonElement deser(String json){
        return ser(ChatUtils.fromJsonText(json));
    }

    private JsonElement ser(Component component){
        return ChatUtils.toJson(component);
    }
}
