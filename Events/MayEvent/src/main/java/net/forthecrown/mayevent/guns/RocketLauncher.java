package net.forthecrown.mayevent.guns;

import net.forthecrown.core.utils.ItemStackBuilder;
import net.forthecrown.mayevent.MayMain;
import net.forthecrown.mayevent.MayUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class RocketLauncher extends HitScanWeapon {
    public RocketLauncher() {
        super(30, 20, 7.5, "Rocket Launcher",
                (remaining, max) -> new ChatComponentText("Rocket Launcher").a(EnumChatFormat.YELLOW, EnumChatFormat.BOLD)
                        .addSibling(new ChatComponentText(": " + remaining + "/" + max).a(EnumChatFormat.WHITE)),

                () -> new ItemStackBuilder(Material.CROSSBOW, 1)
                        .setName(Component.text("Rocket Launcher")
                                .color(NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
                        .addLore(Component.text("Shoots a rocket :)")
                                .color(NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
                ,

                () -> new ItemStackBuilder(Material.GREEN_TERRACOTTA, 7)
                        .setName(Component.text("7 pack of Rockets")
                                .color(NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        )
        );
    }

    @Override
    public boolean attemptUse(Location eyeLoc, Vector direction, boolean ignoreAmmo, LivingEntity source, int wave) {
        if(testCooldownAndAdd(source)) return false;

        if(remainingAmmo <= 0 && !ignoreAmmo) return false;
        if(!ignoreAmmo) remainingAmmo--;

        Location loc = eyeLoc.add(direction).add(source.getVelocity());

        Fireball ball = MayUtils.spawn(loc, Fireball.class, fireball -> fireball.setDirection(direction));
        new RocketTracker(ball, this);

        makeMessage();
        return true;
    }

    @Override
    protected boolean onUse(HitScanShot shot) {
        return false;
    }

    public static class RocketTracker implements Runnable {

        private final Fireball fireball;
        private final RocketLauncher launcher;
        private int untilDestroy = 150; //15 seconds
        private final int id;

        public RocketTracker(Fireball fireball, RocketLauncher launcher){
            this.fireball = fireball;
            this.launcher = launcher;
            this.id = createTracker();
            fireball.setVelocity(fireball.getDirection().clone().multiply(5));
        }

        public int createTracker(){
            return Bukkit.getScheduler().scheduleSyncRepeatingTask(MayMain.inst, this, 2, 2);
        }

        @Override
        public void run() {
            untilDestroy--;
            if(untilDestroy < 0) fireball.remove();
            else if(!fireball.isDead()) return;

            MayUtils.damageInRadius(fireball.getLocation(), 3, launcher.damage() * 3);
            MayUtils.attemptDestruction(fireball.getLocation(), 4);
            Bukkit.getScheduler().cancelTask(id);
        }
    }
}
