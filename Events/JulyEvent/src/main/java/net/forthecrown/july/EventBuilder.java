package net.forthecrown.july;

import net.forthecrown.core.crownevents.EventTimer;
import net.forthecrown.core.utils.Cooldown;
import net.forthecrown.july.items.EventItems;
import net.forthecrown.july.items.GemItems;
import net.forthecrown.july.listener.OnTrackListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static net.forthecrown.july.EventConstants.*;

public class EventBuilder {

    private final Player player;
    private final ParkourEntry entry;
    private final Location minLoc;
    private final Location barrierLoc;

    public EventBuilder(Player player) {
        this.player = player;

        EventTimer timer = new EventTimer(player, p -> {
            p.sendMessage(Component.text("You took too long lol").color(NamedTextColor.GRAY));
            JulyMain.event.end(ParkourEvent.ENTRIES.get(p));
        });

        int xCord = 0;
        while(ParkourEvent.IN_USE_TRACKS.get(xCord)){
            xCord++;
        }

        this.minLoc = new Location(EVENT_WORLD, xCord * DISTANCE_BETWEEN, 70, 1500, PRACTISE.getYaw(), PRACTISE.getPitch());
        this.barrierLoc = BARRIER_OFFSET.apply(minLoc);
        this.entry = new ParkourEntry(player, timer, xCord);

        OnTrackListener listener = entry.inEventListener();
        listener.register(JulyMain.inst);
        listener.calculateOffsets(minLoc);

        ParkourEvent.WAITING_FOR_START.put(player, this);

        changeBarriers(Material.GOLD_BLOCK, null);
        player.teleport(entry.inEventListener().start);
        player.getInventory().removeItemAnySlot(EventItems.ticket());

        EventItems.giveStarter(player);
        EventUtils.clearEffects(player);
    }

    /* Instead of generating the arena, let's pre-make like 10 copies lol
    public EventBuilder buildTrack(){
        new BukkitRunnable() {
            @Override
            public void run() {
                BlockVector3 origMin = BukkitAdapter.asBlockVector(ParkourEvent.REGION.getMinLocation());
                BlockVector3 origMax = BukkitAdapter.asBlockVector(ParkourEvent.REGION.getMaxLocation());

                CuboidRegion region = new CuboidRegion(origMin, origMax);
                BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

                try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(ParkourEvent.EVENT_WORLD)).build()) {
                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                            editSession, region, clipboard, region.getMinimumPoint()
                    );
                    Operations.complete(forwardExtentCopy);
                }

                try (EditSession editSession = new EditSessionBuilder(BukkitAdapter.adapt(ParkourEvent.EVENT_WORLD)).build()) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BukkitAdapter.asBlockVector(minLoc))
                            .ignoreAirBlocks(true)
                            .copyEntities(true)
                            .copyBiomes(true)
                            .build();
                    Operations.complete(operation);
                }
            }
        }.runTaskAsynchronously(JulyMain.inst);

        return this;
    }*/

    public EventBuilder placeGems(){
        boolean onCooldown = Cooldown.contains(player, "Event_GemCooldown");
        boolean onSecretCooldown = Cooldown.contains(player, "Event_GemCooldown_Secret");

        List<Item> items = new ArrayList<>();

        GEM_SPAWN_DATA.forEach(d -> {
            if(onCooldown && !d.spawnsAlways()) return;
            if(onSecretCooldown && d.isSecret()) return;

            int worth = d.getWorth();

            items.add(GemItems.placeGemItem(d.getOffset().apply(minLoc).toCenterLocation(), worth, player.getUniqueId()));
        });

        entry.inEventListener().items = items;
        return this;
    }

    public void build(){ countdownID = countdown(); }

    private int countdownID;
    private byte secondOn = 5;

    private int countdown(){
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(JulyMain.inst, () -> {
            boolean last = secondOn == 0;
            if(last){
                changeBarriers(Material.AIR, mat -> mat == Material.GOLD_BLOCK);
                ParkourEvent.ENTRIES.put(player, entry);
                ParkourEvent.WAITING_FOR_START.remove(player);

                entry.startTime = System.currentTimeMillis();

                entry.timer().start(5);
                EventUtils.clearEffects(player);
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);

                Bukkit.getScheduler().cancelTask(countdownID);
            }

            Title.Times times = Title.Times.of(Duration.ofMillis(250), Duration.ofMillis(750), Duration.ZERO);

            Component subtitle = Component.text(last ? "" : "Get ready!").color(NamedTextColor.YELLOW);
            Component bigTitle = Component.text(last ? "Go!" : secondOn + "").color(last ? NamedTextColor.YELLOW : NamedTextColor.GOLD);

            Title title = Title.title(bigTitle, subtitle, times);

            player.showTitle(title);
            secondOn--;
        }, 0, 20);
    }

    public void changeBarriers(Material material, @Nullable Predicate<Material> predicate){
        Location temp = barrierLoc.clone();

        for (int i = 0; i < 9; i++){
            Location mod = temp.clone().add(i, 0, 0);

            if(predicate == null || predicate.test(mod.getBlock().getType())){
                mod.getBlock().setType(material);
            }
        }
    }

    public void cancel(){
        Bukkit.getScheduler().cancelTask(countdownID);
        ParkourEvent.WAITING_FOR_START.remove(player);

        player.getInventory().clear();
        player.teleport(LOBBY);
    }
}
