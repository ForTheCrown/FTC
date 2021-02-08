package net.forthecrown.core.enums;

import net.forthecrown.core.FtcCore;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

public enum Rank {
    //royals
    KNIGHT ( "&8[&7Knight&8] &r", Branch.ROYALS),
    BARON ("&8[&7Baron&8] &r", Branch.ROYALS),
    BARONESS( "&8[&7Baroness&8] &r", Branch.ROYALS),
    LORD ("&#959595[&6Lord&#959595] &r", Branch.ROYALS),
    LADY( "&#959595[&6Lady&#959595] &r", Branch.ROYALS),
    DUKE ("&7[&#ffbf15Duke&7] &r", Branch.ROYALS),
    DUCHESS( "&7[&#ffbf15Duchess&7] &r", Branch.ROYALS),
    PRINCE ("[&#FBFF0FPrince&r] &r", Branch.ROYALS),
    PRINCESS ( "[&#FBFF0FPrincess&r] &r", Branch.ROYALS),

    //pirates
    SAILOR ("&8&l{&7Sailor&8&l} &r", Branch.PIRATES),
    PIRATE ("&8&l{&7Pirate&8&l} &r", Branch.PIRATES),
    CAPTAIN ("&7{&6Captain&7} &r", Branch.PIRATES),
    ADMIRAL ("{&eAdmiral&r} &r", Branch.PIRATES),

    //vikings
    VIKING ("<Viking>", Branch.VIKINGS),
    BERSERKER ("<Berserker>", Branch.VIKINGS),
    WARRIOR ("<Warrior>",  Branch.VIKINGS),
    SHIELD_MAIDEN ("<Shield-maiden>", Branch.VIKINGS),
    HERSIR ("<Hersir>", Branch.VIKINGS),
    JARL ("<Jarl>", Branch.VIKINGS),

    //non branch ranks
    DEFAULT ("Default", Branch.DEFAULT),
    LEGEND("&#dfdfdf[&#fff147Legend&#dfdfdf] &r", Branch.DEFAULT);

    private final String tabPrefix;
    private final Branch rankBranch;
    Rank(String tabPrefix, Branch rankBranch){
        this.tabPrefix = tabPrefix;
        this.rankBranch = rankBranch;
    }

    public String getPrefix(){
        return FtcCore.translateHexCodes(tabPrefix);
    }

    public String getColorlessPrefix(){
        return tabPrefix;
    }

    public Branch getRankBranch() {
        return rankBranch;
    }

    public static List<Rank> getFreeRanks(){
        return Arrays.asList(Rank.KNIGHT, Rank.BARON, Rank.BARONESS, Rank.SAILOR, Rank.PIRATE, Rank.VIKING, Rank.BERSERKER);
    }

    public static Rank fromPrefix(String s){
        String s1 = ChatColor.stripColor(s).replaceAll("}", "").replaceAll("\\{", "").replaceAll("]", "").replaceAll("\\[", "");
        if(s1.toLowerCase().contains("Default")) return Rank.DEFAULT;
        for (Rank r : values()){
            if(ChatColor.stripColor(r.getPrefix()).replaceAll("}", "").replaceAll("\\{", "").replaceAll("]", "").replaceAll("\\[", "").contains(s1)) return r;
        }
        return null;
    }
}
