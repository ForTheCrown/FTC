package net.forthecrown.core.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.comvars.ComVar;
import net.forthecrown.core.comvars.ComVars;
import net.forthecrown.core.comvars.types.ComVarType;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownItems;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CrownBroadcaster extends AbstractJsonSerializer<FtcCore> implements Announcer {

    private List<Component> announcements = new ArrayList<>();
    private final GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
    private final ComVar<Short> delay = ComVars.set("sv_broadcast_delay", ComVarType.SHORT, (short) 12000);
    private BukkitRunnable broadcaster;

    public CrownBroadcaster(){
        super("announcer", FtcCore.getInstance());

        reload();
        doBroadcasts();

        delay.setOnUpdate(val -> start());
    }


    @Override
    protected void save(final JsonObject json) {
        json.addProperty("delay", delay.getValue());

        JsonArray array = new JsonArray();
        for (Component c: announcements){
            array.add(serializer.serializeToTree(c));
        }
        json.add("announcements", array);
    }

    @Override
    protected void reload(final JsonObject json) {
        if(!fileExists) return;
        delay.setValue(json.get("delay").getAsShort());

        JsonArray array = json.getAsJsonArray("announcements");
        announcements = new ArrayList<>();
        for (JsonElement j: array){
            announcements.add(serializer.deserializeFromTree(j));
        }
    }

    private void doBroadcasts(){
        broadcaster = new BukkitRunnable() {
            int counter = 0;

            @Override
            public void run() {
                Component broadcast = Component.text()
                        .append(FtcCore.prefix())
                        .append(CrownUtils.getRandomNumberInRange(0, 1000) == 1? secretAnnouncement() : getAnnouncements().get(counter))
                        .build();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Don't broadcast info messages to players in the Senate.
                    if (player.getWorld().getName().contains("senate")) continue;

                    player.sendMessage(broadcast);
                    if (player.getWorld().getName().equalsIgnoreCase("world_resource")){
                        player.sendMessage(Component.text("You're in the resource world! To get back to the normal survival world, do /warp portal.").color(NamedTextColor.GRAY));
                    }
                }

                if (getAnnouncements().size() == counter+1) counter = 0;
                else counter++;
            }
        };
        broadcaster.runTaskTimer(FtcCore.getInstance(), 500, delay.getValue());
    }

    @Override
    public short getDelay() {
        return delay.getValue((short) 12000);
    }

    @Override
    public void setDelay(short delay) {
        this.delay.setValue(delay);
    }

    @Override
    public List<Component> getAnnouncements() {
        return announcements;
    }

    @Override
    public void setAnnouncements(List<Component> announcements) {
        this.announcements = announcements;
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

    public boolean wasStopped(){
        return broadcaster.isCancelled();
    }

    @Override
    public void announceToAll(String message) {
        announceToAll(ComponentUtils.convertString(message));
    }

    @Override
    public void announceToAll(Component component){
        Bukkit.getServer().sendMessage(Component.text());
    }

    @Override
    public void announce(String message) {
        announce(ComponentUtils.convertString(message), null);
    }

    @Override
    public void announce(Component message){
        announce(message, null);
    }

    @Override
    public void announce(Component message, String permission){
        Component formatted = formatMessage(message);
        for (Player p: Bukkit.getOnlinePlayers()){
            if(permission != null && !p.hasPermission(permission)) continue;

            p.sendMessage(formatted);
        }
    }

    @Override
    public void announce(String message, @Nullable String permission) {
        Component c_message = ComponentUtils.convertString(message, true);
        announce(c_message, permission);
    }

    @Override
    public Component formatMessage(Component message){
        return Component.text()
                .append(FtcCore.prefix())
                .append(message)
                .build();
    }


    @Override
    protected JsonObject createDefaults(final JsonObject json) {
        json.addProperty("delay", 12000);

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
                        .append(Rank.KNIGHT.prefix())
                        .append(Component.text("tag and a "))
                        .append(CrownItems.BASE_ROYAL_SWORD.getItemMeta().displayName().hoverEvent(CrownItems.BASE_ROYAL_SWORD.asHoverEvent()))
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
                                .hoverEvent(CrownItems.VOTE_TICKET.asHoverEvent())
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

        return json;
    }

    private JsonElement deser(String json){
        return ser(serializer.deserialize(json));
    }

    private JsonElement ser(Component component){
        return serializer.serializeToTree(component);
    }

    public static Component secretAnnouncement(){
        return Component.text("Marriage plugin when!").hoverEvent(Component.text("Whenever people see this ;)"));
    }
}
