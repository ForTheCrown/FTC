package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.v1_16_R3.ArgumentParticle;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_16_R3.CraftParticle;

public class ParticleType {
    public static ArgumentParticle particle(){
        return ArgumentParticle.a();
    }

    public static Particle getParticle(CommandContext<CommandListenerWrapper> c, String argument){
        return CraftParticle.toBukkit(ArgumentParticle.a(c, argument));
    }
}
