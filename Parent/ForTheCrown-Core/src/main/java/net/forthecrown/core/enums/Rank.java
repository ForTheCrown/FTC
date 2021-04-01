package net.forthecrown.core.enums;

import net.forthecrown.core.utils.ComponentUtils;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;

public enum Rank {
    //royals
    KNIGHT ( "&8[&7Knight&8] &f", Branch.ROYALS, "free-rank"),
    BARON ("&8[&7Baron&8] &f", Branch.ROYALS, "free-rank"),
    BARONESS( "&8[&7Baroness&8] &f", Branch.ROYALS, "free-rank"),
    LORD ("&#959595[&6Lord&#959595] &r", Branch.ROYALS, "donator-tier-1"),
    LADY( "&#959595[&6Lady&#959595] &r", Branch.ROYALS, "donator-tier-1"),
    DUKE ("&7[&#ffbf15Duke&7] &r", Branch.ROYALS, "donator-tier-2"),
    DUCHESS( "&7[&#ffbf15Duchess&7] &r", Branch.ROYALS, "donator-tier-2"),
    PRINCE ("[&#FBFF0FPrince&f] &r", Branch.ROYALS, "donator-tier-3"),
    PRINCESS ( "[&#FBFF0FPrincess&f] &r", Branch.ROYALS, "donator-tier-3"),

    //pirates
    SAILOR ("&8&l{&7Sailor&8&l} &r", Branch.PIRATES, "free-rank"),
    PIRATE ("&8&l{&7Pirate&8&l} &r", Branch.PIRATES, "donator-tier-1"),
    CAPTAIN ("&7{&6Captain&7} &r", Branch.PIRATES, "donator-tier-2"),
    ADMIRAL ("{&eAdmiral&f} &r", Branch.PIRATES, "donator-tier-3"),

    //vikings
    VIKING ("<Viking>", Branch.VIKINGS, "free-rank"),
    BERSERKER ("<Berserker>", Branch.VIKINGS, "free-rank"),
    WARRIOR ("<Warrior>",  Branch.VIKINGS, "donator-tier-1"),
    SHIELD_MAIDEN ("<Shield-maiden>", Branch.VIKINGS, "donator-tier-1"),
    HERSIR ("<Hersir>", Branch.VIKINGS, "donator-tier-2"),
    JARL ("<Jarl>", Branch.VIKINGS, "donator-tier-3"),

    //non branch ranks
    DEFAULT ("DEFAULT", Branch.DEFAULT, "default"),
    LEGEND("&#dfdfdf[&#fff147Legend&#dfdfdf] &r", Branch.DEFAULT, "legend");

    private final String tabPrefix;
    private final Branch rankBranch;
    private final String lpRank;
    Rank(String tabPrefix, Branch rankBranch, String lpRank){
        this.tabPrefix = tabPrefix;
        this.rankBranch = rankBranch;
        this.lpRank = lpRank;
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

    public String getLpRank() {
        return lpRank;
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

    public static Rank fromPrefix(Component component){
        return fromPrefix(ComponentUtils.getString(component));
    }
}
