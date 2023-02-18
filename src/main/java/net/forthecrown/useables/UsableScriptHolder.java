package net.forthecrown.useables;

import com.google.common.base.Strings;
import net.forthecrown.core.script2.Script;
import net.forthecrown.useables.util.UsablesScripts;
import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.internal.objects.NativeJSON;
import org.openjdk.nashorn.internal.runtime.JSONFunctions;

public interface UsableScriptHolder {

  @Nullable String getDataString();

  void setDataString(@Nullable String dataString);

  default @Nullable Object getDataObject() {
    String dataString = getDataString();

    if (Strings.isNullOrEmpty(dataString)) {
      return null;
    }

    return JSONFunctions.parse(dataString, null);
  }

  default void setDataObject(Object mirror) {
    if (mirror == null) {
      setDataString(null);
      return;
    }

    var string = NativeJSON.stringify(null, mirror, null, null).toString();
    setDataString(string);
  }

  String getScriptName();

  String[] getArgs();

  default Script compileScript(UsageTypeHolder holder) {
    return UsablesScripts.getScript(holder, this);
  }
}