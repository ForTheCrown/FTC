package net.forthecrown.mayevent.guns;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.mayevent.MayUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;

public class GaussCannon extends HitScanWeapon {

    public GaussCannon(){
        super(100, 60, 3, "Gauss Cannon",
                //Actionbar message
                (remaining, max) -> new ChatComponentText("Gauss Cannon").a(EnumChatFormat.YELLOW, EnumChatFormat.BOLD)
                        .addSibling(new ChatComponentText(": " + remaining + "/" + max).a(EnumChatFormat.WHITE)),

                //Weapon item
                () -> new ItemStackBuilder(Material.CROSSBOW, 1)
                        .setName(Component.text("Gauss Cannon ").style(Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)))
                        .addLore(Component.text("A powerful laser rifle that creates an explosion where it ends"))
                        .addEnchant(Enchantment.ARROW_FIRE, 1)
                        .addEnchant(Enchantment.CHANNELING, 1),

                //Ammo pickup
                () -> new ItemStackBuilder(Material.GREEN_TERRACOTTA, 16)
                        .setName(Component.text("16 pack of Gauss Ammo")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
        );
    }

    @Override
    protected boolean onUse(HitScanShot shot) {
        WeaponEffect.EXPLOSION.play(shot, this);
        MayUtils.drawLine(shot.eyeLoc, shot.endLoc, 0.5, new ParticleBuilder(Particle.REDSTONE).color(Color.YELLOW).allPlayers().count(5));
        return true;
    }
}
