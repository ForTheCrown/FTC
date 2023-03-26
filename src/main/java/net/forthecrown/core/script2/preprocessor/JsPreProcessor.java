package net.forthecrown.core.script2.preprocessor;

import java.io.IOException;
import java.io.Reader;
import org.apache.commons.io.IOUtils;

public class JsPreProcessor {

  private static final PreProcessor[] PROCESSORS = {
      new AliasImportProcessor(),
      new SimpleImportPreProcessor()
  };

  public static String preprocess(Reader reader) throws IOException {
    return preprocess(IOUtils.toString(reader));
  }

  public static void onScriptsReload() {
    JsImport.placeHolders = null;
    JsImport.importsAreConst = true;
  }

  private static String preprocess(String script) {
    StringBuffer buffer = new StringBuffer(script);

    for (var p: PROCESSORS) {
      p.process(buffer);
    }

    return buffer.toString();
  }
}