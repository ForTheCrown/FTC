package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a user's rank
 */
public enum Rank implements JsonSerializable {
    //royals
    KNIGHT (        "&8[&7Knight&8] &f",            Faction.ROYALS,  "free-rank",         RankTier.FREE),
    BARON (         "&8[&7Baron&8] &f",             Faction.ROYALS,  "free-rank",         RankTier.FREE),
    BARONESS(       "&8[&7Baroness&8] &f",          Faction.ROYALS,  "free-rank",         RankTier.FREE),
    LORD (          "&#959595[&6Lord&#959595] &r",  Faction.ROYALS,  "donator-tier-1",    RankTier.TIER_1),
    LADY(           "&#959595[&6Lady&#959595] &r",  Faction.ROYALS,  "donator-tier-1",    RankTier.TIER_1),
    DUKE (          "&7[&#ffbf15Duke&7] &r",        Faction.ROYALS,  "donator-tier-2",    RankTier.TIER_2),
    DUCHESS(        "&7[&#ffbf15Duchess&7] &r",     Faction.ROYALS,  "donator-tier-2",    RankTier.TIER_2),
    PRINCE (        "[&#FBFF0FPrince&f] &r",        Faction.ROYALS,  "donator-tier-3",    RankTier.TIER_3),
    PRINCESS (      "[&#FBFF0FPrincess&f] &r",      Faction.ROYALS,  "donator-tier-3",    RankTier.TIER_3),

    //pirates
    SAILOR (        "&8&l{&7Sailor&8&l} &r",        Faction.PIRATES, "free-rank",        RankTier.FREE),
    PIRATE (        "&8&l{&7Pirate&8&l} &r",        Faction.PIRATES, "donator-tier-1",   RankTier.TIER_1),
    CAPTAIN (       "&7{&6Captain&7} &r",           Faction.PIRATES, "donator-tier-2",   RankTier.TIER_2),
    ADMIRAL (       "{&eAdmiral&f} &r",             Faction.PIRATES, "donator-tier-3",   RankTier.TIER_3),

    //vikings
    VIKING (        "<Viking>",                     Faction.VIKINGS, "free-rank",        RankTier.FREE),
    BERSERKER (     "<Berserker>",                  Faction.VIKINGS, "free-rank",        RankTier.FREE),
    WARRIOR (       "<Warrior>",                    Faction.VIKINGS, "donator-tier-1",   RankTier.TIER_1),
    SHIELD_MAIDEN ( "<Shield-maiden>",              Faction.VIKINGS, "donator-tier-1",   RankTier.TIER_1),
    HERSIR (        "<Hersir>",                     Faction.VIKINGS, "donator-tier-2",   RankTier.TIER_2),
    JARL (          "<Jarl>",                       Faction.VIKINGS, "donator-tier-3",   RankTier.TIER_3),

    //non branch ranks
    DEFAULT(        "DEFAULT",                      Faction.DEFAULT, "default",          RankTier.NONE),
    LEGEND(  "&#dfdfdf[&#fff147Legend&#dfdfdf] &r", Faction.DEFAULT, "legend",           RankTier.TIER_3);

    //TODO get rid of string variables, switch to components completely
    private final String tabPrefix;
    private final Faction rankFaction;
    private final String lpRank;
    public final RankTier tier;
    Rank(String tabPrefix, Faction rankFaction, String lpRank, RankTier tier){
        this.tabPrefix = tabPrefix;
        this.rankFaction = rankFaction;
        this.lpRank = lpRank;
        this.tier = tier;
    }

    public String getPrefix(){
        return FtcFormatter.translateHexCodes(tabPrefix);
    }

    public String getColorlessPrefix(){
        return tabPrefix;
    }

    public Component prefix(){
        return ChatUtils.convertString(tabPrefix);
    }

    public Component noEndSpacePrefix(){
        return ChatUtils.convertString(tabPrefix.replaceAll(" ", ""));
    }

    public Faction getRankBranch() {
        return rankFaction;
    }

    public String getLpRank() {
        return lpRank;
    }

    public RankTier getTier() {
        return tier;
    }

    public static Rank fromPrefix(Component component){
        return fromPrefix(ChatUtils.getString(component));
    }

    public static List<Rank> allFromTier(RankTier tier){
        List<Rank> ranks = new ArrayList<>();

        for (Rank r: values()){
            if(r.getTier() == tier) ranks.add(r);
        }

        return ranks;
    }

    public static List<Rank> freeRanks(){
        return Arrays.asList(Rank.KNIGHT, Rank.BARON, Rank.BARONESS, Rank.SAILOR, Rank.PIRATE, Rank.VIKING, Rank.BERSERKER);
    }

    public static Rank fromPrefix(String s){
        String s1 = ChatColor.stripColor(s).replaceAll("\\p{P}", "").toUpperCase().trim(); //removes all punctuation marks, including brackets and stuff
        try {
            return valueOf(s1);
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeEnum(this);
    }
}
