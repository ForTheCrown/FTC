package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.admin.record.PunishmentRecord;
import net.forthecrown.core.admin.record.PunishmentType;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.serializer.AbstractJsonSerializer;
import net.forthecrown.grenadier.CommandSource;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrownPunishmentManager extends AbstractJsonSerializer implements PunishmentManager {
    private final Map<UUID, PunishmentEntry> entries = new HashMap<>();

    public CrownPunishmentManager() {
        super("punishments");

        reload();
        ForTheCrown.logger().info("Punishment Manager loaded");
    }

    @Override
    protected void save(JsonObject json) {
        for (PunishmentEntry e: entries.values()){
            json.add(e.id.toString(), e.serialize());
        }
    }

    @Override
    protected void reload(JsonObject json) {
        entries.clear();
        for (Map.Entry<String, JsonElement> e: json.entrySet()){
            UUID id = UUID.fromString(e.getKey());
            setEntry(new PunishmentEntry(id, e.getValue()));
        }
    }

    @Override
    protected JsonObject createDefaults(JsonObject json) {
        return json;
    }

    @Override
    public MuteStatus checkMute(CommandSender sender) {
        if(!(sender instanceof Player)) return MuteStatus.NONE;

        Player player = (Player) sender;
        UUID id = player.getUniqueId();

        if(isSoftmuted(id)) return MuteStatus.SOFT;
        if(isMuted(id)){
            sender.sendMessage(Component.text("Your voice has been silenced").color(NamedTextColor.RED));
            return MuteStatus.HARD;
        }

        return MuteStatus.NONE;
    }

    @Override
    public MuteStatus checkMuteSilent(UUID id){
        if(isSoftmuted(id)) return MuteStatus.SOFT;
        if(isMuted(id)) return MuteStatus.HARD;

        return MuteStatus.NONE;
    }

    public boolean checkJailed(CommandSender sender){
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        UUID id = player.getUniqueId();

        return checkPunished(id, PunishmentType.JAIL);
    }

    @Override
    public boolean checkBanned(UUID id) {
        return checkPunished(id, PunishmentType.BAN);
    }

    public boolean checkPunished(UUID id, PunishmentType type){
        PunishmentEntry entry = getEntry(id);
        if(entry == null) return false;

        return entry.checkPunished(type);
    }

    @Override
    public PunishmentEntry getEntry(UUID id) {
        return entries.get(id);
    }

    @Override
    public PunishmentEntry getOrNew(UUID id) {
        return entries.getOrDefault(id, new PunishmentEntry(id));
    }

    @Override
    public void setEntry(PunishmentEntry entry) {
        entries.put(entry.id, entry);
    }

    @Override
    public boolean isSoftmuted(UUID id) {
        PunishmentEntry entry = getEntry(id);
        if(entry == null) return false;

        return entry.checkPunished(PunishmentType.SOFT_MUTE);
    }

    @Override
    public boolean isMuted(UUID id) {
        PunishmentEntry entry = getEntry(id);
        if (entry == null) return false;

        return entry.checkPunished(PunishmentType.MUTE);
    }

    @Override
    public Collection<PunishmentEntry> getEntries() {
        return entries.values();
    }

    @Override
    public PunishmentRecord punish(UUID id, PunishmentType type, CommandSource punisher, String reason, long length) {
        PunishmentRecord record = new PunishmentRecord(type, punisher, reason, System.currentTimeMillis(), length);
        PunishmentEntry entry = getOrNew(id);

        entry.punish(record);
        setEntry(entry);

        return record;
    }

    @Override
    public PunishmentRecord punish(UUID id, PunishmentType type, CommandSource source, String reason, long length, String extra){
        PunishmentRecord record = new PunishmentRecord(type, source, reason, extra, System.currentTimeMillis(), length);
        PunishmentEntry entry = getOrNew(id);

        entry.punish(record);
        setEntry(entry);

        return record;
    }

    @Override
    public void jail(Key key, Player player){
        Bukkit.getPluginManager().registerEvents(new JailListener(player, getJailManager().get(key)), ForTheCrown.inst());
    }

    @Override
    public void pardon(UUID id, PunishmentType type) throws CommandSyntaxException {
        PunishmentEntry entry = getEntry(id);
        if(entry == null) throw FtcExceptionProvider.create("Given user has no punishment record");

        entry.pardon(type);
    }
}
