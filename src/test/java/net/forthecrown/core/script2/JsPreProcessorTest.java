package net.forthecrown.core.script2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JsPreProcessorTest {

  @Test
  void preprocess() {
    String replace = "import 'net.forthecrown.Foo';";

    assertEquals(
        "const Foo = Java.type(\"net.forthecrown.Foo\");",
        JsPreProcessor.preprocess(replace)
    );
  }
}