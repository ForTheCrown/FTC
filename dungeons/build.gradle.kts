plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":structures"))
  compileOnly(project(":user-titles"))

  paperweight.paperDevBundle("1.20-R0.1-SNAPSHOT")
}

pluginYml {
  name = "FTC-Dungeons"
  main = "net.forthecrown.dungeons.DungeonsPlugin"

  depends {
    optional("project:user-titles")
  }
}