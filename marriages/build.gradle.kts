plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":mail"))
  compileOnly(project(":cosmetics"))
}

pluginYml {
  name = "FTC-Marriages"
  main = "net.forthecrown.marriages.MarriagePlugin"
}