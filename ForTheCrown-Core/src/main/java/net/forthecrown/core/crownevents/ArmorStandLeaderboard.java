package net.forthecrown.core.crownevents;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.FtcCore;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.*;

public class ArmorStandLeaderboard {

    private String title;
    private Order order;
    private Map<String, Integer> list;
    private int size;
    private final Location location;
    private List<UUID> stands;
    private String border;
    private boolean isTimerScore;

    /**
     * %pos for position
     * %name for name
     * %score for score lol
     */
    private String format;

    public ArmorStandLeaderboard(String title, Map<String, Integer> list, Location location) {
        this.title = title;
        this.list = list;
        this.location = location;

        setOrder(Order.HIGH_TO_LOW);
        setSize(10);
        setBorder("&e-----=o=O=o=-----");
        setFormat("%pos. %name: %score");
        setTimerScore(false);

        stands = new ArrayList<>();
        FtcCore.LEADERBOARDS.add(this);
    }

    public void update(){
        destroy();
        create();
    }

    public void create(){
        Location loc = getLocation().clone();
        createStand(getTitle(), loc);
        createStand(getBorder(), loc.subtract(0, 0.25, 0));

        Map<String, Integer> sorted = getSortedMap();
        List<String> stringList = new ArrayList<>(sorted.keySet());

        for (int i = 0; i < getSize(); i++){
            int toGet = i;
            if(getOrder() != Order.LOW_TO_HIGH) toGet = sorted.size() - i-1;
            if(toGet >= stringList.size() || toGet <= -1) break;

            String name = stringList.get(toGet);
            createStand(formatString(i+1, name, sorted.get(name)), loc.subtract(0, 0.25, 0));
        }
        createStand(getBorder(), loc.subtract(0, 0.25, 0));
    }

    private String formatString(int pos, String name, int score){
        String scoreS = score + "";
        if(isTimerScore()) scoreS = EventTimer.getTimerCounter(score).toString();

        return getFormat()
                .replaceAll("%pos", pos + "")
                .replaceAll("%name", name)
                .replaceAll("%score", scoreS);
    }

    private Map<String, Integer> getSortedMap(){
        List<Map.Entry<String, Integer>> list = new ArrayList<>(getList().entrySet());
        list.sort(Map.Entry.comparingByValue());
        
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void createStand(String name, Location loc){
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.customName(ComponentUtils.convertString(name));
        stand.setCustomNameVisible(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setCanMove(false);

        stands.add(stand.getUniqueId());
    }

    public void destroy(){
        for (UUID id: stands){
            try {
                Bukkit.getEntity(id).remove();
            } catch (NullPointerException ignored) {}
        }
        stands.clear();
    }

    public Location getLocation() {
        return location;
    }

    public int getSize() {
        return size;
    }

    public void setSize(@Nonnegative int size) {
        this.size = size;
    }

    public List<UUID> getStands() {
        return stands;
    }

    public String getTitle() {
        return CrownUtils.translateHexCodes(title);
    }

    public void setTitle(@NotNull String title) {
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

    public String getFormat() {
        return CrownUtils.translateHexCodes(format);
    }

    public void setFormat(@NotNull String format) {
        Validate.notNull(format, "Format cannot be null");
        this.format = format;
    }

    public String getBorder() {
        return CrownUtils.translateHexCodes(border);
    }

    public void setBorder(@NotNull String border) {
        Validate.notNull(border, "Border cannot be null");
        this.border = border;
    }

    public boolean isTimerScore() {
        return isTimerScore;
    }

    public void setTimerScore(boolean timerScore) {
        isTimerScore = timerScore;
    }

    public enum Order{
        LOW_TO_HIGH,
        HIGH_TO_LOW
    }
}
