package net.forthecrown.mayevent.guns;

import com.destroystokyo.paper.ParticleBuilder;
import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.mayevent.BlockDamageTracker;
import net.forthecrown.mayevent.MayUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

public class StandardRifle extends HitScanWeapon {
    public StandardRifle() {
        super(120, 2, 8, "Assault Rifle",
                (remaining, max) -> new ChatComponentText("Assault Rifle")
                        .a(EnumChatFormat.BOLD, EnumChatFormat.DARK_AQUA)
                        .addSibling(new ChatComponentText(": " + remaining + "/" + max + " Rounds").a(EnumChatFormat.WHITE)),

                () -> new ItemStackBuilder(Material.NETHERITE_HOE, 1)
                        .setName(Component.text("Assault Rifle").color(NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD))
                        .addLore(Component.text("The generic rifle of all action movie heroes").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)),

                () -> new ItemStackBuilder(Material.GRAY_GLAZED_TERRACOTTA, 30)
                        .setName(Component.text("30 Assault rifle rounds"))
        );
    }

    @Override
    protected boolean onUse(HitScanShot shot) {
        WeaponEffect effect;
        WeaponEffect.GUNSHOT.play(shot, this);

        if(shot.hasHitEntity){
            if(!(shot.target instanceof LivingEntity)) return true;
            LivingEntity ent = (LivingEntity) shot.target;

            ent.damage(damage * 2);
            effect = WeaponEffect.BULLET_HIT_MOB;
        } else {
            Block hit = shot.hitScan.getHitBlock();
            if(hit == null) return true;

            effect = WeaponEffect.BULLET_HIT_WALL;
            BlockDamageTracker.damage(hit, damage);
        }

        effect.play(shot, this);
        MayUtils.drawLine(shot.eyeLoc, shot.endLoc, 0.5, new ParticleBuilder(Particle.WHITE_ASH).allPlayers().count(5));
        return true;
    }
}
