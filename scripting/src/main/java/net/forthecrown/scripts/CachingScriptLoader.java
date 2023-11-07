package net.forthecrown.scripts;

import net.forthecrown.utils.io.source.Source;

public interface CachingScriptLoader extends ScriptLoader {

  Script remove(Source source);

  void close();
}
