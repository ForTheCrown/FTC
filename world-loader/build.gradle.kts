plugins {
  java
}

repositories {
  maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
  compileOnly("org.popcraft:chunky-common:1.3.92")
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-ChunkLoader"
  main = "net.forthecrown.worldloader.WorldLoaderPlugin"

  depends {
    optional("Chunky")
  }
}