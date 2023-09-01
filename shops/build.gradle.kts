plugins {
  java
}

repositories {

  // WorldGuard
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":menus"))
  compileOnly(project(":mail"))

  compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.6.4")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
}

pluginYml {
  name = "FTC-Shops"
  main = "net.forthecrown.economy.ShopsPlugin"

  depends {
    required("WorldGuard")
  }
}