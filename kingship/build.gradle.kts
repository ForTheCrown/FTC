plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":special-items"))
  compileOnly(project(":user-titles"))
}

pluginYml {
  name = "FTC-Kingship"
  main = "net.forthecrown.king.KingshipPlugin"
}