plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-AutoAfk"
  main = "net.forthecrown.afk.AfkPlugin"
}