plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
  maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly("us.dynmap:DynmapCoreAPI:3.5-beta-3")
  compileOnly("com.github.BlueMap-Minecraft:BlueMapAPI:v2.5.1")
}

pluginYml {
  name = "FTC-Webmap"
  main = "net.forthecrown.webmap.WebmapPlugin"

  depends {
    optional("dynmap")
    optional("BlueMap")
  }
}