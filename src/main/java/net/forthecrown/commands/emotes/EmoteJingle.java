package net.forthecrown.commands.emotes;

import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.Tasks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

import java.time.Month;
import java.time.ZonedDateTime;

import static net.forthecrown.text.Messages.*;
import static net.forthecrown.commands.emotes.EmoteJingle.JingleNote.BASS;
import static net.forthecrown.commands.emotes.EmoteJingle.JingleNote.SNARE;

public class EmoteJingle extends CommandEmote {

    public EmoteJingle(){
        super("jingle", 6*20, EMOTE_JINGLE_COOLDOWN);

        setDescription("Plays a christmas song. :)");
        setPermission(Permissions.EMOTE_JINGLE);

        register();
    }

    /*
     * ----------------------------------------
     * 			Command description:
     * ----------------------------------------
     * Command that allows players to vibe on Jingle Bells.
     * Only works if they both have emotes enabled.
     *
     * Valid usages of command:
     * - /jingle
     *
     * Main Author: Wout
     * Edit: Julie
     */

    @Override
    public boolean test(CommandSource source) {
        var time = ZonedDateTime.now();

        if (time.getMonth() == Month.DECEMBER) {
            return true;
        }

        return super.test(source);
    }

    @Override
    public int execute(User sender, User target) {
        sender.sendMessage(jingleSender(target));
        target.sendMessage(jingleTarget(sender, test(target.getCommandSource(this))));

        jingle(target);
        return 0;
    }

    @Override
    public int executeSelf(User user) {
        Cooldown.add(user, cooldownCategory, cooldownTime);
        jingle(user);
        return 0;
    }

    //Jingle, by the illustrious composer Woutzart xD
    private void jingle(User user) {
        Location loc = user.getPlayer().getLocation();
        loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL,loc, 25, 0.1, 0, 0.1, 1);
        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 50, 0.1, 0, 0.1, 0.1);

        new JinglePlayer(loc).next();
    }

    @RequiredArgsConstructor(staticName = "note")
    static class JingleNote {
        static final byte
                SNARE =  0,
                BASS  =  1,
                NONE  = -1;

        static final float
                MID_TONE = 1.5f,
                HIGH_TONE = 1.7f;

        private static final JingleNote[] NOTES = {
                note(8,  MID_TONE,  BASS),
                note(4,  MID_TONE, SNARE),
                note(4,  MID_TONE,  BASS),

                note(8,  MID_TONE,  BASS),
                note(4,  MID_TONE, SNARE),
                note(4,  MID_TONE,  BASS),

                note(8,  MID_TONE,  BASS),
                note(4,      1.8f, SNARE),
                note(4,      1.2f,  BASS),
                note(4,  MID_TONE, SNARE),

                note(4,  MID_TONE,  BASS),
                note(4,  MID_TONE, SNARE),
                note(4,  MID_TONE,  BASS),

                note(8, HIGH_TONE,  BASS),
                note(4, HIGH_TONE, SNARE),
                note(4, HIGH_TONE,  BASS),

                note(6, HIGH_TONE, SNARE),
                note(2, HIGH_TONE,  BASS),
                note(4,  MID_TONE, SNARE),
                note(4,  MID_TONE,  BASS),

                note(8,  MID_TONE,  BASS),
                note(4,      1.3f, SNARE),
                note(4,      1.3f, SNARE),
                note(4, HIGH_TONE, SNARE),
                note(4,  MID_TONE, SNARE),

                note(8,      2.0f,  NONE),
        };

        private final int delay;
        private final float pitch;
        private final byte instrument;
    }

    @RequiredArgsConstructor
    static class JinglePlayer implements Runnable {
        private final Location location;
        private int index = 0;

        @Override
        public void run() {
            var note = JingleNote.NOTES[index];
            play(note);

            index++;

            if (index < JingleNote.NOTES.length) {
                next();
            }
        }

        void next() {
            Tasks.runLater(this, JingleNote.NOTES[index].delay);
        }

        void play(JingleNote note) {
            if (note.instrument == BASS) {
                location.getWorld().playSound(
                        location,
                        Sound.BLOCK_NOTE_BLOCK_BASEDRUM,
                        SoundCategory.MASTER,
                        0.2F, 1F
                );
            } else if (note.instrument == SNARE) {
                location.getWorld().playSound(
                        location,
                        Sound.BLOCK_NOTE_BLOCK_SNARE,
                        SoundCategory.MASTER,
                        1F, 1F
                );
            }

            location.getWorld().playSound(
                    location,
                    Sound.BLOCK_NOTE_BLOCK_BELL,
                    SoundCategory.MASTER,
                    1F, note.pitch
            );
        }
    }
}