package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownUtils;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.forthecrown.core.commands.brigadier.exceptions.NonPlayerSenderException;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public abstract class CrownCommandBuilder implements Predicate<CommandListenerWrapper> {

    private final String name;
    private final Plugin plugin;
    private final String fallbackPrefix;

    private List<String> aliases = new ArrayList<>();
    private String description;
    private String usage;
    private String permission;
    private String permissionMessage;
    private LiteralArgumentBuilder<CommandListenerWrapper> command;
    private boolean registered;
    private VanillaCommandWrapper wrapper;

    //private final PluginCommand pluginCommand = new PluginCommand(this);

    protected CrownCommandBuilder(@NotNull String name, @NotNull Plugin plugin) {
        this.name = name;
        this.fallbackPrefix = plugin.getDescription().getName();
        this.plugin = plugin;

        command = LiteralArgumentBuilder.literal(name);
        permission = "ftc.commands." + name;
        permissionMessage = ChatColor.GRAY + "You do not have permission to use this";
    }

    protected abstract void registerCommand(LiteralArgumentBuilder<CommandListenerWrapper> command);

    public final void register(){
        if(registered) return;

        command.requires(this);
        registerCommand(command);

        CommandDispatcher<CommandListenerWrapper> dispatcher = RoyalBrigadier.getDispatcher().a();
        dispatcher.register(command);

        wrapper = new VanillaCommandWrapper(RoyalBrigadier.getDispatcher(), command.build());
        wrapper.setPermission(permission);
        wrapper.setAliases(aliases);
        wrapper.setDescription(description);
        wrapper.setPermissionMessage(permissionMessage);
        wrapper.setUsage(usage);
        wrapper.setLabel(name);
        wrapper.setName(name);

        CommandMap map = plugin.getServer().getCommandMap();
        map.register(getName(), fallbackPrefix, wrapper);
        wrapper.register(map);

        registered = true;
    }

    protected LiteralArgumentBuilder<CommandListenerWrapper> argument(String name){
        return LiteralArgumentBuilder.literal(name);
    }

    protected RequiredArgumentBuilder<CommandListenerWrapper, ?> argument(String name, ArgumentType<?> type){
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected SuggestionsBuilder getPlayerList(SuggestionsBuilder builder){
        List<String> pNames = new ArrayList<>();
        for (Player p: Bukkit.getOnlinePlayers()){
            pNames.add(p.getName());
        }

        return listCompletions(builder, pNames);
    }

    protected boolean testPlayerSenderSilent(CommandListenerWrapper source){
        return source.getBukkitSender() instanceof Player;
    }

    protected void testPlayerSender(CommandListenerWrapper source) throws NonPlayerSenderException {
        if(!testPlayerSenderSilent(source)) throw new NonPlayerSenderException();
    }

    protected Player getPlayerSender(CommandContext<CommandListenerWrapper> c) throws NonPlayerSenderException {
        testPlayerSender(c.getSource());
        return (Player) c.getSource().getBukkitSender();
    }

    protected CrownUser getUserSender(CommandContext<CommandListenerWrapper> c) throws NonPlayerSenderException {
        return FtcCore.getUser(getPlayerSender(c));
    }

    protected CommandSender getSender(CommandContext<CommandListenerWrapper> c){
        return c.getSource().getBukkitSender();
    }

    protected SuggestionsBuilder listCompletions( SuggestionsBuilder b, String... suggestions){
        return listCompletions(b, Arrays.asList(suggestions));
    }

    protected SuggestionsBuilder listCompletions(SuggestionsBuilder b, List<String> suggestions){
        String input = b.getInput();
        String token = input.substring(input.lastIndexOf(' ')).trim();
        for (String s: suggestions){
            if(token.isBlank() || s.regionMatches(true, 0, token, 0, token.length())) b.suggest(s);
        }
        return b;
    }

    protected SuggestionsBuilder listCompletions(SuggestionsBuilder b, Map<String, String> completionsAndTips){
        String input = b.getInput();
        String token = input.substring(input.lastIndexOf(' ')).trim();
        for (String s: completionsAndTips.keySet()){
            if(token.isBlank() || s.regionMatches(true, 0, token, 0, token.length())) b.suggest(s, new LiteralMessage(completionsAndTips.get(s)));
        }
        return b;
    }

    protected UUID getUUID(@NotNull String name) throws InvalidPlayerArgumentException {
        Validate.notNull(name, "The name cannot be null");
        UUID toReturn = FtcCore.getOffOnUUID(name);
        if(toReturn == null) throw new InvalidPlayerArgumentException(name);
        return toReturn;
    }

    protected String getLastArgument(String input){
        int lastIndex = input.lastIndexOf(' ');
        if(lastIndex == -1) return null;

        return input.substring(lastIndex);
    }

    @Override
    public boolean test(CommandListenerWrapper sender) {
        return testPermissionSilent(sender.getBukkitSender());
    }

    public boolean testPermissionSilent(CommandSender sender){
        if(permission != null && !permission.isBlank()) return sender.hasPermission(getPermission());
        return true;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setAliases(String... aliases) {
        if(isRegistered()) return;
        this.aliases = Arrays.asList(aliases);
    }

    public String getDescription() {
        return CrownUtils.translateHexCodes(description);
    }

    public String getPermissionMessage() {
        return CrownUtils.translateHexCodes(permissionMessage);
    }

    public String getPermission(){
        return permission;
    }

    public String getFallbackPrefix() {
        return fallbackPrefix;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setUsage(String usage) {
        if(isRegistered()) return;
        this.usage = usage;
    }

    public void setDescription(String description) {
        if(isRegistered()) return;
        this.description = description;
    }

    public void setPermission(String permission) {
        if(isRegistered()) return;
        this.permission = permission;
    }

    public void setPermissionMessage(String permissionMessage) {
        if(isRegistered()) return;
        this.permissionMessage = permissionMessage;
    }

    public VanillaCommandWrapper getWrapper() {
        return wrapper;
    }

    public String getUsage() {
        return CrownUtils.translateHexCodes(usage);
    }
}
