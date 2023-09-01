plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
  mavenCentral()

  // WorldGuard
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  compileOnly(project(":commons"))
  testImplementation(project(":commons"))

  implementation(project(":commons", "reobf"))
  implementation(project(":class-loader-tools"))
  compileOnly("net.luckperms:api:5.4")

  compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.6.4")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
}

pluginYml {
  name = "FTC-Core"
  main = "net.forthecrown.core.CorePlugin"
  loader = "net.forthecrown.core.CoreLoader"

  depends {
    required("LuckPerms")
    required("FastAsyncWorldEdit")
    required("WorldGuard")
  }

  loadBefore {
    regular("GriefPrevention")
    regular("OpenInv")
  }
}

tasks {
  buildAndCopyToRoot {
    dependsOn(shadowJar)
  }
}