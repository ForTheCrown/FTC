plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
}

pluginYml {
  name = "FTC-Usables"
  main = "net.forthecrown.usables.UsablesPlugin"
}