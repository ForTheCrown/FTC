plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly("me.clip:placeholderapi:2.11.4")
}

pluginYml {
  name = "FTC-PlaceholderAPI-hook"
  main = "net.forthecrown.placeholderapi.HookPlugin"

  depends {
    optional("PlaceholderAPI")
  }
}