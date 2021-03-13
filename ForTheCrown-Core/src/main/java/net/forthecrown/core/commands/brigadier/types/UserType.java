package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.commands.brigadier.exceptions.CrownCommandException;
import net.forthecrown.core.commands.brigadier.exceptions.InvalidPlayerArgumentException;
import net.minecraft.server.v1_16_R3.ArgumentEntity;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserType {

    public static ArgumentEntity onlinePlayer(){
        return EntityType.player();
    }

    public static StringArgumentType user(){
        return StringArgumentType.word();
    }

    public static CrownUser getUser(CommandContext<CommandListenerWrapper> c, String argument) throws CrownCommandException {
        String id = c.getArgument(argument, String.class);
        UUID uuid = FtcCore.getOffOnUUID(id);
        if(uuid == null) throw new InvalidPlayerArgumentException(id);

        return FtcCore.getUser(uuid);
    }

    public static CrownUser getOnlineUser(CommandContext<CommandListenerWrapper> c, String argument) throws CommandSyntaxException {
        return FtcCore.getUser(EntityType.getPlayer(c, argument));
    }

    public static CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder b){
        List<String> pNames = new ArrayList<>();
        for (Player p: Bukkit.getOnlinePlayers()){
            pNames.add(p.getName());
        }
        return CrownCommandBuilder.suggestMatching(b, pNames);
    }

    /**
     * Yo, don't use this lmao, it takes a solid second to load all the players' names
     * @param b
     * @return
     */
    public static CompletableFuture<Suggestions> listALLplayers(SuggestionsBuilder b){
        List<String> pNames = new ArrayList<>();
        for (OfflinePlayer p: Bukkit.getOfflinePlayers()){
            pNames.add(p.getName());
        }
        return CrownCommandBuilder.suggestMatching(b, pNames);
    }

    //CAN'T USE: Forces client to disconnect because of mismatch between client arguments and server arguments

   /*public static class Serializer implements ArgumentSerializer<UserArgumentType> {
       //toPacket
       @Override
       public void a(UserArgumentType userArgumentType, PacketDataSerializer packetDataSerializer) {
           packetDataSerializer.writeBoolean(userArgumentType.acceptsOffline);
       }

       //fromPacket
       @Override
       public UserArgumentType b(PacketDataSerializer packetDataSerializer) {
           return new UserArgumentType(packetDataSerializer.readBoolean());
       }

       //toJson
       @Override
       public void a(UserArgumentType userArgumentType, JsonObject jsonObject) {
           Announcer.ac("toPacket " + jsonObject + " SEP " + userArgumentType.toString());
           jsonObject.addProperty("acceptsOffline", userArgumentType.acceptsOffline ? "true" : "false");
           Announcer.ac("toPacket " + jsonObject + " SEP " + userArgumentType.toString());
       }
   }*/
}
