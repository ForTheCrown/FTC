package net.forthecrown.utils.io.parse;

public final class ErrorFactories {
  private ErrorFactories() {}

  public static ParseErrorFactory named(StringBuffer input, String fileName) {
    return new ErrorFactoryImpl(fileName, input);
  }

  public static ParseErrorFactory unnamed(StringBuffer input) {
    return new ErrorFactoryImpl(null, input);
  }
}