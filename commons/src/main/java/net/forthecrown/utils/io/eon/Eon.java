package net.forthecrown.utils.io.eon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.StringWriter;
import net.forthecrown.utils.io.parse.CharReader;

public final class Eon {
  private Eon() {}

  public static JsonObject parse(CharReader reader) {
    Lexer lexer = new Lexer(reader);
    TokenList tokens = lexer.lex();
    Parser parser = new Parser(tokens, reader.factory());
    return parser.parse();
  }

  public static String toString(JsonElement element) {
    try {
      StringWriter writer = new StringWriter();
      write(element, writer);
      return writer.toString();
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  public static void write(JsonElement element, Appendable writer) throws IOException {
    EonWriter eonWriter = new EonWriter(writer);
    eonWriter.setIndent("  ");
    eonWriter.write(element);
  }
}