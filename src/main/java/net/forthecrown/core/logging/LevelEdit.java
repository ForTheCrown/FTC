package net.forthecrown.core.logging;

import com.sk89q.util.ReflectionUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AsyncAppender;
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.AppenderControlArraySet;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class LevelEdit {
  public static void changeLevels() {
    LoggerContext context = LoggerContext.getContext();
    context.getConfiguration().getRootLogger().setLevel(Level.ALL);
    context.updateLoggers();

    changeLevel(context.getConfiguration().getRootLogger());
  }

  public static void changeLevel(LoggerConfig config) {
    var it = config.getAppenderRefs().listIterator();
    while (it.hasNext()) {
      var n = it.next();
      it.set(changeLevel(n));
    }

    AppenderControlArraySet set = ReflectionUtil.getField(config, "appenders");
    AppenderControl[] control = set.get();

    for (int i = 0; i < control.length; i++) {
      control[i] = changeLevel(control[i]);
    }
  }

  private static Appender changeLevel(Appender appender) {
    if (appender instanceof AsyncAppender async) {
      AppenderRef[] refs = ReflectionUtil.getField(async, "appenderRefs");
      changeLevels(refs);
      async.start();
    }

    if (appender instanceof RewriteAppender rewrite) {
      AppenderRef[] refs = ReflectionUtil.getField(rewrite, "appenderRefs");
      changeLevels(refs);
      rewrite.start();
    }

    return appender;
  }

  private static void changeLevels(AppenderRef[] refs) {
    for (int i = 0; i < refs.length; i++) {
      refs[i] = changeLevel(refs[i]);
    }
  }

  private static AppenderControl changeLevel(AppenderControl control) {
    return new AppenderControl(
        changeLevel(control.getAppender()),
        Level.ALL,
        control.getFilter()
    );
  }

  private static AppenderRef changeLevel(AppenderRef ref) {
    return AppenderRef.createAppenderRef(ref.getRef(), Level.ALL, ref.getFilter());
  }
}