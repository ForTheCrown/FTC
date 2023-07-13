plugins {
  java

  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":dungeons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":menus"))
  compileOnly(project(":user-titles"))

  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")

  paperweight.paperDevBundle("1.20-R0.1-SNAPSHOT")
}

pluginYml {
  name = "FTC-ExtendedItems"
  main = "net.forthecrown.inventory.ItemsPlugin"

  depends {
    required("WorldGuard")
  }
}

tasks {
  assemble {
    dependsOn(reobfJar)
  }
}