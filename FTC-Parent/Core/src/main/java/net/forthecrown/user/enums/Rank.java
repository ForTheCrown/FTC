package net.forthecrown.user.enums;

import com.google.gson.JsonElement;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.core.chat.ChatFormatter;
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
    KNIGHT ( "&8[&7Knight&8] &f", Branch.ROYALS, "free-rank", RankTier.FREE),
    BARON ("&8[&7Baron&8] &f", Branch.ROYALS, "free-rank", RankTier.FREE),
    BARONESS( "&8[&7Baroness&8] &f", Branch.ROYALS, "free-rank", RankTier.FREE),
    LORD ("&#959595[&6Lord&#959595] &r", Branch.ROYALS, "donator-tier-1", RankTier.TIER_1),
    LADY( "&#959595[&6Lady&#959595] &r", Branch.ROYALS, "donator-tier-1", RankTier.TIER_1),
    DUKE ("&7[&#ffbf15Duke&7] &r", Branch.ROYALS, "donator-tier-2", RankTier.TIER_2),
    DUCHESS( "&7[&#ffbf15Duchess&7] &r", Branch.ROYALS, "donator-tier-2", RankTier.TIER_2),
    PRINCE ("[&#FBFF0FPrince&f] &r", Branch.ROYALS, "donator-tier-3", RankTier.TIER_3),
    PRINCESS ( "[&#FBFF0FPrincess&f] &r", Branch.ROYALS, "donator-tier-3", RankTier.TIER_3),

    //pirates
    SAILOR ("&8&l{&7Sailor&8&l} &r", Branch.PIRATES, "free-rank", RankTier.FREE),
    PIRATE ("&8&l{&7Pirate&8&l} &r", Branch.PIRATES, "donator-tier-1", RankTier.TIER_1),
    CAPTAIN ("&7{&6Captain&7} &r", Branch.PIRATES, "donator-tier-2", RankTier.TIER_2),
    ADMIRAL ("{&eAdmiral&f} &r", Branch.PIRATES, "donator-tier-3", RankTier.TIER_3),

    //vikings
    VIKING ("<Viking>", Branch.VIKINGS, "free-rank", RankTier.FREE),
    BERSERKER ("<Berserker>", Branch.VIKINGS, "free-rank", RankTier.FREE),
    WARRIOR ("<Warrior>",  Branch.VIKINGS, "donator-tier-1", RankTier.TIER_1),
    SHIELD_MAIDEN ("<Shield-maiden>", Branch.VIKINGS, "donator-tier-1", RankTier.TIER_1),
    HERSIR ("<Hersir>", Branch.VIKINGS, "donator-tier-2", RankTier.TIER_2),
    JARL ("<Jarl>", Branch.VIKINGS, "donator-tier-3", RankTier.TIER_3),

    //non branch ranks
    DEFAULT("DEFAULT", Branch.DEFAULT, "default", RankTier.NONE),
    LEGEND("&#dfdfdf[&#fff147Legend&#dfdfdf] &r", Branch.DEFAULT, "legend", RankTier.TIER_3);

    //TODO get rid of string variables, switch to components completely
    private final String tabPrefix;
    private final Branch rankBranch;
    private final String lpRank;
    public final RankTier tier;
    Rank(String tabPrefix, Branch rankBranch, String lpRank, RankTier tier){
        this.tabPrefix = tabPrefix;
        this.rankBranch = rankBranch;
        this.lpRank = lpRank;
        this.tier = tier;
    }

    public String getPrefix(){
        return ChatFormatter.translateHexCodes(tabPrefix);
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

    public Branch getRankBranch() {
        return rankBranch;
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
