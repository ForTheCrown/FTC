package net.forthecrown.mayevent;

import net.forthecrown.core.crownevents.entries.PlayerEntry;
import net.forthecrown.core.nbt.NBT;
import net.forthecrown.core.nbt.NbtGetter;
import net.forthecrown.mayevent.arena.ArenaBuilder;
import net.forthecrown.mayevent.arena.EventArena;
import net.forthecrown.mayevent.events.ArenaListener;
import net.forthecrown.mayevent.events.GunListener;
import net.forthecrown.mayevent.guns.GunHolder;
import net.forthecrown.mayevent.guns.HitScanWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

public class ArenaEntry extends PlayerEntry<ArenaEntry> implements GunHolder {

    private final GunListener gunListener;
    private final PlayerInventory inventory;
    private final EventArena arena;

    public final Map<String, HitScanWeapon> guns;

    public ArenaEntry(Player entry) {
        super(entry, new ArenaListener());

        this.inEventListener.setEntry(this);
        this.inventory = entry.getInventory();
        this.gunListener = new GunListener(entry, this);
        this.guns = new HashMap<>();
        this.arena = new ArenaBuilder(this).build();

        ((ArenaListener) inEventListener).arena = arena;
    }

    public void regEvents(MayMain main){
        inEventListener.register(main);
        Bukkit.getPluginManager().registerEvents(gunListener, main);
    }

    public void unregEvents(){
        HandlerList.unregisterAll(gunListener);
        inEventListener.unregister();
    }

    public HitScanWeapon fromName(String name){
        if(guns.containsKey(name)) return guns.get(name);
        return null;
    }

    public void pickUp(HitScanWeapon gun){
        if(guns.containsKey(gun.name())) return;

        guns.put(gun.name(), gun);
        entry.sendMessage(Component.text("Picked up: ")
                .append(Component.text(gun.name()).color(NamedTextColor.GOLD))
                .color(NamedTextColor.GRAY)
        );
    }

    public HitScanWeapon getHeldGun(){
        ItemStack item = inventory.getItemInMainHand();
        if(item == null) return null;
        NBT nbt = NbtGetter.ofItemTags(item);
        if(!nbt.has("gun")) return null;

        return fromName(nbt.getString("gun"));
    }

    public HitScanWeapon getLowestAmmoGun(){
        HitScanWeapon gun = null;

        for (HitScanWeapon g: guns.values()){
            if(gun == null){
                gun = g;
                continue;
            }
            if(g.remainingAmmo() < gun.remainingAmmo()) gun = g;
        }

        return gun;
    }

    @Override
    public void setGun(HitScanWeapon gun) {
        pickUp(gun);
    }

    @Override
    public boolean fireGun() {
        HitScanWeapon gun = getHeldGun();

        if(gun == null) return false;
        if(entry.hasCooldown(gun.item().getType())) return false;

        boolean result = gun.attemptUse(this);

        if(result){
            entry.setCooldown(gun.item().getType(), gun.tickFiringDelay());
            sendGunMessage();
        }
        return result;
    }

    @Override
    public Player getHoldingEntity() {
        return entry;
    }

    @Override
    public Location getLocation() {
        return entry.getEyeLocation();
    }

    @Override
    public int getWave() {
        return arena.wave();
    }

    @Override
    public boolean ignoreAmmo() {
        return false;
    }

    public void sendGunMessage(){
        HitScanWeapon held = getHeldGun();
        if(held == null) return;

        user.sendMessage(held.message(), ChatMessageType.GAME_INFO);
    }

    public GunListener gunListener() {
        return gunListener;
    }

    public EventArena arena() {
        return arena;
    }

    public PlayerInventory inventory() {
        return inventory;
    }
}
