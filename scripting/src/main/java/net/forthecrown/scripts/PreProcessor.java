package net.forthecrown.scripts;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.brigadier.context.StringRange;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;
import org.mozilla.javascript.NativeJavaClass;

class PreProcessor {

  static final Processor[] PROCESSORS = {
      new SimpleImportProcessor(),
      new ImportFromProcessor(),
      new SelectiveImportProcessor()
  };

  private final StringBuffer source;
  private final List<PreProcessorCallback> callbacks = new ArrayList<>();

  public PreProcessor(StringBuffer source) {
    this.source = source;
  }

  public String run() {
    for (Processor processor : PROCESSORS) {
      processor.run(source, callbacks);
    }

    return source.toString();
  }

  public void runCallbacks(ScriptImpl script) {
    for (int i = 0; i < callbacks.size(); i++) {
      PreProcessorCallback callback = callbacks.get(i);
      callback.postProcess(script);
    }
  }
}

interface PreProcessorCallback {

  void postProcess(ScriptImpl script);
}

interface Processor {

  void run(StringBuffer buffer, List<PreProcessorCallback> callbacks);
}

@RequiredArgsConstructor
abstract
class RegexProcessor implements Processor {
  private final Pattern pattern;

  @Override
  public void run(StringBuffer buffer, List<PreProcessorCallback> callbacks) {
    Matcher matcher = pattern.matcher(buffer);

    while (matcher.find()) {
      MatchResult result = matcher.toMatchResult();

      int start = result.start();
      int end = result.end();

      StringRange range = StringRange.between(start, end);
      SubWriter writer = new SubWriter(range, buffer);

      process(writer, result, callbacks);
      matcher = pattern.matcher(buffer);
    }
  }

  protected abstract void process(SubWriter writer,
                                  MatchResult result,
                                  List<PreProcessorCallback> callbacks
  );
}

class SimpleImportProcessor extends RegexProcessor {

  public SimpleImportProcessor() {
    super(Pattern.compile("import +['\"]([^'\"]+)['\"];?"));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String importName = result.group(1);
    writer.deleteInput();

    if (importName.endsWith(".js")) {
      FileImport fImport = new FileImport(importName, null, List.of());
      callbacks.add(fImport);
    } else {
      Class<?> c = ClassImport.getClassFrom(importName);
      ClassImport jsImport = new ClassImport(c, null, List.of());
      callbacks.add(jsImport);
    }
  }
}

class ImportFromProcessor extends RegexProcessor {

  public ImportFromProcessor() {
    super(Pattern.compile(
        "import(?: +\\* +as)? +([a-zA-Z_$][a-zA-Z0-9_$]*) +from +['\"]([^'\"]+)['\"];?"
    ));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String alias = result.group(1);
    String path = result.group(2);

    writer.deleteInput();

    if (path.endsWith(".js")) {
      FileImport fImport = new FileImport(path, alias, List.of());
      callbacks.add(fImport);
    } else {
      Class<?> c = ClassImport.getClassFrom(path);
      ClassImport jsImport = new ClassImport(c, alias, List.of());
      callbacks.add(jsImport);
    }
  }
}

class SelectiveImportProcessor extends RegexProcessor {

  public SelectiveImportProcessor() {
    super(Pattern.compile(
        "import +\\{([a-zA-Z0-9_$, ]+)} +from +\"([^\"]+)\";?"
    ));
  }

  @Override
  protected void process(
      SubWriter writer,
      MatchResult result,
      List<PreProcessorCallback> callbacks
  ) {
    String path = result.group(2);
    String rawValues = result.group(1);

    String[] values = rawValues.trim().split("\\s*,+\\s*");
    var list = toBindings(result.group(), values);

    writer.deleteInput();

    if (path.endsWith(".js")) {
      FileImport jsImport = new FileImport(path, null, list);
      callbacks.add(jsImport);
    } else {
      Class<?> clazz = ClassImport.getClassFrom(path);
      ClassImport jsImport = new ClassImport(clazz, null, list);
      callbacks.add(jsImport);
    }
  }

  List<BindingImport> toBindings(String fullImport, String[] values) {
    List<BindingImport> imports = new ArrayList<>(values.length);

    for (String s: values) {
      BindingImport bind;

      if (s.contains(" as ")) {
        String[] split = s.split(" +as +");
        Preconditions.checkState(split.length == 2, "Invalid import: '%s'", fullImport);
        bind = new BindingImport(split[0], split[1]);
      } else {
        bind = new BindingImport(s, s);
      }

      imports.add(bind);
    }

    return imports;
  }
}

record BindingImport(String label, String alias) {
}

@RequiredArgsConstructor
class FileImport implements PreProcessorCallback {

  private final String path;
  private final String alias;
  private final List<BindingImport> importedValues;

  String getBindingName() {
    if (!Strings.isNullOrEmpty(alias)) {
      return alias;
    }

    String res = path.substring(0, path.length()-3);
    int slashIndex = path.lastIndexOf('/');

    if (slashIndex != -1) {
      res = res.substring(slashIndex + 1);
    }

    return res;
  }

  @Override
  public void postProcess(ScriptImpl script) {
    String bindingName = getBindingName();

    Path dir = script.getWorkingDirectory();
    Path file = dir.resolve(path);

    Source source = Sources.fromPath(file, dir);
    Script loaded = Scripts.newScript(source);

    loaded.compile();
    loaded.evaluate().throwIfError();

    if (importedValues.isEmpty()) {
      script.putConst(bindingName, loaded);
    } else {
      for (int i = 0; i < importedValues.size(); i++) {
        BindingImport name = importedValues.get(i);
        Object value = loaded.get(name.label());

        if (value == Script.NOT_FOUND) {
          throw new IllegalStateException(
              "Value '" + name + "' not found in script " + loaded.getName()
          );
        }

        script.put(name.alias(), value);
      }
    }
  }
}

@Getter
@RequiredArgsConstructor
class ClassImport implements PreProcessorCallback {

  private final Class<?> importedClass;
  private final String alias;
  private final List<BindingImport> methodNames;

  static Class<?> getClassFrom(String name) throws RuntimeException {
    try {
      return Class.forName(name, true, ClassImport.class.getClassLoader());
    } catch (ClassNotFoundException exc) {
      throw new RuntimeException(exc);
    }
  }

  String getBindingName() {
    return Strings.isNullOrEmpty(alias) ? importedClass.getSimpleName() : alias;
  }

  @Override
  public void postProcess(ScriptImpl script) {
    if (methodNames.isEmpty()) {
      String binding = getBindingName();
      script.runtimeImport(binding, importedClass);
      return;
    }

    NativeJavaClass njc = new NativeJavaClass(script.scriptObject, importedClass);

    for (BindingImport value: methodNames) {
      Object gotten = njc.get(value.label(), njc);
      script.put(value.alias(), gotten);
    }
  }
}

class SubWriter implements Appendable {

  final StringRange range;
  final StringBuffer buffer;

  int cursor;
  boolean inputErased = false;

  SubWriter(StringRange range, StringBuffer buffer) {
    this.range = range;
    this.buffer = buffer;
  }

  public void deleteInput() {
    int start = range.getStart();
    int end = Math.max(cursor, range.getEnd());

    buffer.delete(start, end);
    cursor = range.getStart();
    inputErased = true;
  }

  @Override
  public SubWriter append(CharSequence csq) {
    boolean insert = inputErased || cursor >= range.getEnd();
    int length = csq.length();
    int newCursor = cursor + length;

    if (insert) {
      buffer.insert(cursor, csq);
    } else {
      buffer.replace(cursor, newCursor, csq.toString());
    }

    cursor = newCursor;
    return this;
  }

  @Override
  public SubWriter append(CharSequence csq, int start, int end) {
    return append(csq.subSequence(start, end));
  }

  @Override
  public SubWriter append(char c) {
    boolean insert = inputErased || cursor >= range.getEnd();

    if (insert) {
      buffer.insert(cursor++, c);
    } else {
      buffer.setCharAt(cursor++, c);
    }

    return this;
  }
}