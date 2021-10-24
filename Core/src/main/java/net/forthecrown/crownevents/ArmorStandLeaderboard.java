package net.forthecrown.crownevents;

import net.forthecrown.commands.CommandHologram;
import net.forthecrown.core.Crown;
import net.forthecrown.core.Main;
import net.forthecrown.utils.math.FtcBoundingBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A leaderboard made of ArmorStands, used for display
 */
public class ArmorStandLeaderboard {

    private Component[] title;
    private Order order;
    private Map<String, Integer> list;
    private byte size;
    private final Location location;
    private Component border;

    private ScoreFormatter scoreFormatter;
    private LeaderboardFormatter format;

    public ArmorStandLeaderboard(Map<String, Integer> list, Location location, Component... title) {
        Validate.notNull(title);
        this.title = title;
        this.list = list;
        this.location = location;

        setOrder(Order.HIGH_TO_LOW);
        setSize((byte) 10);
        setBorder(Component.text("-----=o=O=o=-----").color(NamedTextColor.YELLOW));

        setFormat(LeaderboardFormatter.defaultFormat());
        setScoreFormatter(ScoreFormatter.defaultFormat());

        Main.LEADERBOARDS.add(this);
        getLocation().getChunk().addPluginChunkTicket(Crown.inst());
    }

    /**
     * Updates the map with the given entry 2 score map
     * @param scores New scores
     */
    public void update(Map<String, Integer> scores){
        setList(scores);
        create();
    }

    /**
     * Destroys any preexisting leaderboards at the specified location and then creates
     * a new leaderboard
     */
    public void create() {
        destroy();

        Location loc = getLocation().clone();

        createTitleStands(loc);
        createStand(getBorder(), loc.subtract(0, 0.25, 0));

        Map<String, Integer> sorted = getSortedMap();
        List<String> stringList = new ArrayList<>(sorted.keySet());

        for (int i = 0; i < getSize(); i++) {
            int toGet = i;
            if(getOrder() != Order.LOW_TO_HIGH) toGet = sorted.size() - i-1;
            if(toGet >= stringList.size() || toGet <= -1) break;

            String name = stringList.get(toGet);
            createStand(formatString(i+1, name, sorted.get(name)), loc.subtract(0, 0.25, 0));
        }

        createStand(getBorder(), loc.subtract(0, 0.25, 0));
    }

    protected void createTitleStands(Location location){
        location.add(0, 0.25, 0);
        for (Component c: title){
            location.subtract(0, 0.25, 0);
            createStand(c, location);
        }
    }

    protected Component formatString(int pos, String name, int score){
        return getFormat().formatName(pos, name, getScoreFormatter().format(score));
    }

    protected Map<String, Integer> getSortedMap(){
        List<Map.Entry<String, Integer>> list = new ArrayList<>(getList().entrySet());
        list.sort(Map.Entry.comparingByValue());
        
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    protected static void createStand(Component name, Location loc) {
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);
        stand.customName(name);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setRemoveWhenFarAway(false);
        stand.setPersistent(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCanMove(false);
        stand.getPersistentDataContainer().set(CommandHologram.HOLOGRAM_KEY, PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Destroys the leaderboard lol
     */
    public void destroy(){
        Location l = getLocation().clone();
        FtcBoundingBox area = new FtcBoundingBox(l.getWorld(), l.getX()+1, l.getY()+1, l.getZ()+1, l.getX()-1, l.getY()-(0.25*getSize()+1), l.getZ()-1);

        for (ArmorStand stand : area.getEntitiesByType(ArmorStand.class)){
            if(!stand.getPersistentDataContainer().has(CommandHologram.HOLOGRAM_KEY, PersistentDataType.BYTE)) continue;
            stand.remove();
        }
    }

    public Location getLocation() {
        return location;
    }

    public byte getSize() {
        return size;
    }

    public void setSize(@Nonnegative byte size) {
        this.size = size;
    }

    public Component[] getTitle() {
        return title;
    }

    public void setTitle(@NotNull Component... title) {
        Validate.notNull(title, "Title cannot be null");
        this.title = title;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(@NotNull Order order) {
        Validate.notNull(order, "Order cannot be null");
        this.order = order;
    }

    public Map<String, Integer> getList() {
        return list;
    }

    public void setList(@NotNull Map<String, Integer> list) {
        Validate.notNull(list, "Score list cannot be null");
        this.list = list;
    }

    public LeaderboardFormatter getFormat() {
        return format;
    }

    public void setFormat(@NotNull LeaderboardFormatter format) {
        Validate.notNull(format, "Format cannot be null");
        this.format = format;
    }

    public Component getBorder() {
        return border;
    }

    public void setBorder(@NotNull Component border) {
        Validate.notNull(border, "Border cannot be null");
        this.border = border;
    }

    public ScoreFormatter getScoreFormatter() {
        return scoreFormatter;
    }

    public void setScoreFormatter(@NotNull ScoreFormatter scoreFormatter) {
        Validate.notNull(scoreFormatter);
        this.scoreFormatter = scoreFormatter;
    }

    public enum Order{
        LOW_TO_HIGH,
        HIGH_TO_LOW
    }
}
