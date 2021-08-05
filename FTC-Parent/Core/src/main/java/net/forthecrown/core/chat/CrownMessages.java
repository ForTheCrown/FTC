package net.forthecrown.core.chat;

import com.google.common.io.Files;
import net.forthecrown.core.ForTheCrown;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;

public class CrownMessages implements Keyed {

    private TranslationRegistry registry;
    private final Key key;

    public CrownMessages(){
        key = Key.key(ForTheCrown.inst(), "messages");
        registry = TranslationRegistry.create(key);
        registry.defaultLocale(Locale.ENGLISH);

        ensureDirectoryExists();
        ensureDefaultExists();
    }

    private void ensureDirectoryExists(){
        File f = new File(ForTheCrown.dataFolder().getPath() + File.separator + "translations");

        if(!f.exists()) f.mkdir();
        if(!f.isDirectory()){
            f.delete();
            f.mkdir();
        }
    }

    private void ensureDefaultExists(){
        File f = new File(ForTheCrown.dataFolder().getPath() + File.separator + "translations" + File.separator + "en_US.properties");

        if(!f.exists()){
            try {
                f.createNewFile();
                Files.write(ForTheCrown.resource("en_US.properties").readAllBytes(), f);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void load(){
        File dir = new File(ForTheCrown.dataFolder().getPath() + File.separator + "translations");

        for (File f: dir.listFiles()){
            try {
                PropertyResourceBundle bundle = new PropertyResourceBundle(new FileReader(f));
                Locale locale = Locale.forLanguageTag(bundle.getString("language.code"));

                registry.registerAll(locale, bundle, true);
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        GlobalTranslator.get().addSource(registry);
    }

    public void reload(){
        GlobalTranslator.get().removeSource(registry);

        registry = TranslationRegistry.create(key);
        registry.defaultLocale(Locale.ENGLISH);

        ensureDirectoryExists();
        ensureDefaultExists();

        load();
    }

    public TranslationRegistry getRegistry() {
        return registry;
    }

    @Override
    public @NonNull Key key() {
        return key;
    }
}
