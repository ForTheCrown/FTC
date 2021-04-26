package net.forthecrown.core.commands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidSenderException;
import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * The class used to create, build and register commands
 * <p>
 * stuff like usage and descriptions are basically worthless and exist
 * because I can't be arsed to remove them from commands that already have them
 * </p>
 */
public abstract class CrownCommandBuilder implements Predicate<CommandListenerWrapper> {

    private final String name;
    private final Plugin plugin;
    private final String fallbackPrefix;

    private List<String> aliases = new ArrayList<>();
    private String description;
    private String usage;
    private String permission;
    private String permissionMessage;
    private final BrigadierCommand command;
    private boolean registered;
    private VanillaCommandWrapper wrapper;

    protected CrownCommandBuilder(@NotNull String name, @NotNull Plugin plugin) {
        Validate.notNull(name, "Name was null");
        Validate.notNull(plugin, "Plugin was null");

        this.name = name;
        this.fallbackPrefix = plugin.getDescription().getName();
        this.plugin = plugin;

        command = new BrigadierCommand(name);
        permission = "ftc.commands." + name.toLowerCase();
        permissionMessage = ChatColor.GRAY + "You do not have permission to use this";
        description = "";
    }

    protected abstract void registerCommand(BrigadierCommand command);

    public final void register(){
        if(registered) return;

        command.requires(this);
        registerCommand(command);

        CommandDispatcher<CommandListenerWrapper> dispatcher = RoyalBrigadier.getServerCommands().a();
        dispatcher.register(command);

        initializeWrapper();
        CommandMap map = plugin.getServer().getCommandMap();
        map.register(getName(), fallbackPrefix, wrapper);
        wrapper.register(map);

        registered = true;
    }

    protected final void initializeWrapper(){
        wrapper = new VanillaCommandWrapper(RoyalBrigadier.getServerCommands(), command.build());
        wrapper.setPermission(permission);
        wrapper.setAliases(aliases);
        wrapper.setDescription(description);
        wrapper.setPermissionMessage(permissionMessage);
        wrapper.setUsage(usage);
        wrapper.setLabel(name);
        wrapper.setName(name);
    }

    protected LiteralArgument argument(String name){
        return new LiteralArgument(name);
    }

    protected <T> RequiredArgument<T> argument(String name, ArgumentType<T> type){
        return new RequiredArgument<>(name, type);
    }

    protected boolean testPlayerSenderSilent(CommandListenerWrapper source){
        return source.getBukkitSender() instanceof Player;
    }

    protected void testPlayerSender(CommandListenerWrapper source) throws InvalidSenderException {
        if(!testPlayerSenderSilent(source)) throw new InvalidSenderException();
    }

    protected Player getPlayerSender(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        return c.getSource().h().getBukkitEntity();
    }

    protected CrownUser getUserSender(CommandContext<CommandListenerWrapper> c) throws CommandSyntaxException {
        return UserManager.getUser(getPlayerSender(c));
    }

    protected CommandSender getSender(CommandContext<CommandListenerWrapper> c){
        return c.getSource().getBukkitSender();
    }

    //Untested
    protected <T extends CommandSender> T getAs(CommandContext<CommandListenerWrapper> c, Class<T> clazz) throws InvalidSenderException {
        if(!clazz.isAssignableFrom(c.getSource().getBukkitSender().getClass())) throw new InvalidSenderException(clazz);
        return (T) c.getSource().getBukkitSender();
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

    public void setAliases(List<String> list){
        if(isRegistered()) return;
        this.aliases = list;
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

    protected void adminMessage(CommandContext<CommandListenerWrapper> c, String message){
        adminMessage(c, ComponentUtils.stringToVanilla(message));
    }

    protected void adminMessage(CommandContext<CommandListenerWrapper> c, Component message){
        adminMessage(c, IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(message)));
    }

    protected void adminMessage(CommandContext<CommandListenerWrapper> c, IChatBaseComponent message){
        if(!c.getSource().base.shouldBroadcastCommands()) return;
        IChatMutableComponent cMessage = (new ChatMessage("chat.type.admin", new Object[]{c.getSource().getScoreboardDisplayName(), message})).a(new EnumChatFormat[]{EnumChatFormat.GRAY, EnumChatFormat.ITALIC});

        for (CrownUser u: UserManager.getOnlineUsers()){
            if(testPermissionSilent(u)) u.sendMessage(cMessage);
        }
    }

    public static void broadcastAdmin(CommandListenerWrapper sender, String message){
        broadcastAdmin(sender, ComponentUtils.stringToVanilla(message), true);
    }

    public static void broadcastAdmin(CommandListenerWrapper sender, IChatBaseComponent message, boolean senderSees){
        sender.sendMessage(message, senderSees);
    }

    public static void broadcastAdmin(CommandSender sender, String message){
        if(sender instanceof CrownUser) sender = ((CrownUser) sender).getPlayer();
        broadcastAdmin(VanillaCommandWrapper.getListener(sender), ComponentUtils.stringToVanilla(message), true);
    }

    //Copied from CommandSource.java... in the damn FabricMC API
    public static CompletableFuture<Suggestions> suggestMatching(SuggestionsBuilder suggestionsBuilder, String... strings) {
        return suggestMatching(suggestionsBuilder, Arrays.asList(strings));
    }

    public static CompletableFuture<Suggestions> suggestMatching(SuggestionsBuilder suggestionsBuilder, Collection<String> strings){
        String token = suggestionsBuilder.getRemaining().toLowerCase();

        for (String s: strings){
            if(s.toLowerCase().startsWith(token)) suggestionsBuilder.suggest(s);
        }

        return suggestionsBuilder.buildFuture();
    }

    public static SuggestionProvider<CommandListenerWrapper> suggest(String... suggestions){
        return suggest(Arrays.asList(suggestions));
    }

    public static SuggestionProvider<CommandListenerWrapper> suggest(Collection<String> suggestions){
        return (c, b) -> suggestMatching(b, suggestions);
    }

    public static SuggestionProvider<CommandListenerWrapper> suggest(Map<String, Message> suggestions){
        return (c, b) -> suggestMatching(b, suggestions);
    }

    public static CompletableFuture<Suggestions> suggestMatching(SuggestionsBuilder suggestionsBuilder, Map<String, Message> strings){
        String token = suggestionsBuilder.getRemaining().toLowerCase();

        for (String s: strings.keySet()){
            if(s.toLowerCase().startsWith(token)) suggestionsBuilder.suggest(s, strings.get(s));
        }

        return suggestionsBuilder.buildFuture();
    }
}
