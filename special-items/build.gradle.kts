plugins {
  java
}

repositories {
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":dungeons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":menus"))

  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
}

pluginYml {
  name = "FTC-ExtendedItems"
  main = "net.forthecrown.inventory.ItemsPlugin"

  depends {
    required("WorldGuard")
  }
}