import net.forthecrown.gradle.MC_VERSION

plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {
  mavenCentral()

  // AncientGates
  maven("https://repo.codemc.org/repository/maven-public/")

  // WorldGuard
  maven("https://maven.enginehub.org/repo/")

  // Discord SRV, required by project :discord
  maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":structures"))
  compileOnly(project(":usables"))
  compileOnly(project(":discord"))

  compileOnly("org.mcteam.ancientgates:ancientgates:2.6")

  compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.5.2")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")

  paperweight.paperDevBundle("$MC_VERSION-R0.1-SNAPSHOT")
}

pluginYml {
  name = "FTC-ResourceWorld"
  main = "net.forthecrown.resourceworld.RwPlugin"

  depends {
    required("AncientGates")
    optional("project:discord")
  }
}