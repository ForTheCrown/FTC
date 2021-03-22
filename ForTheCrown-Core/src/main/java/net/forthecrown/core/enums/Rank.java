package net.forthecrown.core.enums;

import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

public enum Rank {
    //royals
    KNIGHT ( "&8[&7Knight&8] &f", Branch.ROYALS),
    BARON ("&8[&7Baron&8] &f", Branch.ROYALS),
    BARONESS( "&8[&7Baroness&8] &f", Branch.ROYALS),
    LORD ("&#959595[&6Lord&#959595] &r", Branch.ROYALS),
    LADY( "&#959595[&6Lady&#959595] &r", Branch.ROYALS),
    DUKE ("&7[&#ffbf15Duke&7] &r", Branch.ROYALS),
    DUCHESS( "&7[&#ffbf15Duchess&7] &r", Branch.ROYALS),
    PRINCE ("[&#FBFF0FPrince&f] &r", Branch.ROYALS),
    PRINCESS ( "[&#FBFF0FPrincess&f] &r", Branch.ROYALS),

    //pirates
    SAILOR ("&8&l{&7Sailor&8&l} &r", Branch.PIRATES),
    PIRATE ("&8&l{&7Pirate&8&l} &r", Branch.PIRATES),
    CAPTAIN ("&7{&6Captain&7} &r", Branch.PIRATES),
    ADMIRAL ("{&eAdmiral&f} &r", Branch.PIRATES),

    //vikings
    VIKING ("<Viking>", Branch.VIKINGS),
    BERSERKER ("<Berserker>", Branch.VIKINGS),
    WARRIOR ("<Warrior>",  Branch.VIKINGS),
    SHIELD_MAIDEN ("<Shield-maiden>", Branch.VIKINGS),
    HERSIR ("<Hersir>", Branch.VIKINGS),
    JARL ("<Jarl>", Branch.VIKINGS),

    //non branch ranks
    DEFAULT ("", Branch.DEFAULT),
    LEGEND("&#dfdfdf[&#fff147Legend&#dfdfdf] &r", Branch.DEFAULT);

    private final String tabPrefix;
    private final Branch rankBranch;
    Rank(String tabPrefix, Branch rankBranch){
        this.tabPrefix = tabPrefix;
        this.rankBranch = rankBranch;
    }

    public String getPrefix(){
        return CrownUtils.translateHexCodes(tabPrefix);
    }

    public String getColorlessPrefix(){
        return tabPrefix;
    }

    public Component prefix(){
        return ComponentUtils.convertString(tabPrefix);
    }

    public Component noEndSpacePrefix(){
        return ComponentUtils.convertString(tabPrefix.replaceAll(" ", ""));
    }

    public Branch getRankBranch() {
        return rankBranch;
    }

    public static List<Rank> freeRanks(){
        return Arrays.asList(Rank.KNIGHT, Rank.BARON, Rank.BARONESS, Rank.SAILOR, Rank.PIRATE, Rank.VIKING, Rank.BERSERKER);
    }

    public static Rank fromPrefix(String s){
        String s1 = ChatColor.stripColor(s).replaceAll("\\p{P}", "").toLowerCase(); //removes all punctuation marks, including brackets and stuff

        if(s1.toLowerCase().contains("default") || s1.isBlank()) return DEFAULT;
        for (Rank r : values()){
            String s2 = ChatColor.stripColor(r.getPrefix()).replaceAll("\\p{P}", "").toLowerCase();

            if(s2.contains(s1)) return r;
        }
        return null;
    }
}
